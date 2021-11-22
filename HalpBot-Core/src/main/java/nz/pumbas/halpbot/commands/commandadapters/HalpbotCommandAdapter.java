package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.Getter;
import lombok.SneakyThrows;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.commandmethods.CommandContext;
import nz.pumbas.halpbot.commands.commandmethods.CommandContextFactory;
import nz.pumbas.halpbot.commands.commandmethods.parsing.MessageParsingContext;
import nz.pumbas.halpbot.commands.commandmethods.parsing.ParsingContext;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.exceptions.IllegalPrefixException;
import nz.pumbas.halpbot.commands.usage.VariableNameBuilder;
import nz.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.common.annotations.Bot;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.permissions.exceptions.InsufficientPermissionException;
import nz.pumbas.halpbot.utilities.ErrorManager;

@Service
@Binds(CommandAdapter.class)
public class HalpbotCommandAdapter implements CommandAdapter
{
    private final Map<String, CommandContext> registeredCommands = HartshornUtils.emptyMap();

    @Getter private final String prefix;
    @Getter private final UsageBuilder usageBuilder;

    @Inject @Getter private ApplicationContext applicationContext;
    @Inject @Getter private ParameterAnnotationService parameterAnnotationService;

    @Inject private HalpbotCore halpbotCore;
    @Inject private CommandContextFactory commandContextFactory;

    @SneakyThrows
    public HalpbotCommandAdapter() {
        this.prefix = this.applicationContext.activator(Bot.class).prefix();
        if (this.prefix.isBlank())
            throw new IllegalPrefixException("The prefix defined in @Bot cannot be blank");

        this.usageBuilder = this.determineUsageBuilder();
    }

    //TODO: Guild specific prefixes
    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // Remove all the additional whitespace
        String message = event.getMessage().getContentRaw().replaceAll("\\s+", " ");

        String[] splitText  = message.split(" ", 2);
        String commandAlias = splitText[0].toLowerCase();
        String content      = (2 == splitText.length) ? splitText[1] : "";

        HalpbotEvent halpbotEvent = new MessageEvent(event);

        if (commandAlias.startsWith(this.prefix())) {
            String alias = commandAlias.substring(this.prefix.length());
            this.commandContextSafely(alias)
                .absent(() -> this.halpbotCore.getDisplayConfiguration()
                    .displayTemporary(halpbotEvent,
                        "The command **" + commandAlias + "** doesn't seem to exist, you may want to check your spelling",
                        30))
                .flatMap(commandContext ->
                    this.handleCommandInvocation(halpbotEvent, commandContext, content))
                .present(output -> this.halpbotCore.getDisplayConfiguration().display(halpbotEvent, output))
                .caught(exception -> {
                    ErrorManager.handle(event, exception);
                    this.halpbotCore.getDisplayConfiguration()
                        .displayTemporary(
                            halpbotEvent,
                            "There was the following error trying to invoke this command: " + exception.getMessage(),
                            30);
                });
        }
    }

    
    private Exceptional<Object> handleCommandInvocation(HalpbotEvent event,
                                                        CommandContext commandContext,
                                                        String content)
    {
        if (!commandContext.hasPermission(event.getUser()))
            return Exceptional.of(new InsufficientPermissionException("You do not have permission to use this command"));

        InvocationContext invocationContext = new InvocationContext(
                this.applicationContext,
                content,
                event,
                commandContext.reflections());
        return commandContext.invoke(invocationContext, false);
    }

    private void hasMethodParameterNamesVerifier(String sampleParameter) { }

    private UsageBuilder determineUsageBuilder() {
        if ("arg0".equals(TypeContext.of(this)
                .method("hasMethodParameterNamesVerifier")
                .get()
                .parameters()
                .get(0)
                .name()))
        {
            this.applicationContext.log()
                    .info("Parameter names have not been preserved. Using a type usage builder");
            return new TypeUsageBuilder();
        }
        this.applicationContext.log()
                .info("Parameter names have been preserved. Using a variable name usage builder");
        return new VariableNameBuilder();
    }

    @Override
    public <T> void registerCommands(TypeContext<T> typeContext)     {
        T instance = this.applicationContext.get(typeContext);
        this.registerCommandContext(
            instance,
            typeContext.methods(Command.class));
    }

    @Override
    @Nullable
    public CommandContext commandContext(String alias) {
        return this.registeredCommands.get(alias);
    }

    private <T> void registerCommandContext(T instance, List<MethodContext<?, T>> annotatedMethods)
    {
        for (MethodContext<?, T> methodContext : annotatedMethods) {
            if (!methodContext.isPublic()) {
                this.applicationContext.log().warn("The command method %s should be public if its annotated with @Command"
                        .formatted(methodContext.qualifiedName()));
                continue;
            }

            if (!this.parameterAnnotationsAreValid(methodContext))
                continue;

            Command command = methodContext.annotation(Command.class).get();
            List<String> aliases = this.aliases(command, methodContext);
            CommandContext commandContext = this.createCommand(
                    aliases,
                    instance,
                    command,
                    methodContext,
                    new MessageParsingContext());

            for (String alias : aliases) {
                if (this.registeredCommands.containsKey(alias)) {
                    this.applicationContext.log().warn(
                        "The alias %s is already being used by the command [%s]. The command [%s] will not be registered under this alias"
                                .formatted(alias, this.registeredCommands.get(alias).usage(), commandContext.usage()));
                    continue;
                }

                this.registeredCommands.put(alias, commandContext);
            }
        }
    }

    private List<String> aliases(Command command, MethodContext<?, ?> methodContext) {
        List<String> aliases = HartshornUtils.asList(command.alias())
            .stream()
            .map(alias -> alias.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

        // If an alias hasn't been specified, use the method name
        if (aliases.isEmpty())
            aliases.add(methodContext.name().toLowerCase(Locale.ROOT));
        return aliases;
    }

    private String usage(Command command, MethodContext<?, ?> methodContext) {
        if (!command.usage().isBlank())
            return command.usage();
        else return this.usageBuilder.buildUsage(this.applicationContext, methodContext);
    }

    private <T> CommandContext createCommand(List<String> aliases,
                                             T instance,
                                             Command command,
                                             MethodContext<?, T> methodContext,
                                             ParsingContext parsingContext)
    {
        return this.commandContextFactory.create(
            aliases,
            command.description(),
            this.usage(command, methodContext),
            instance,
            methodContext,
            List.of(command.permissions()),
            Stream.of(command.reflections()).map(TypeContext::of).collect(Collectors.toSet()),
            parsingContext);
    }

    @Override
    public Map<String, CommandContext> registeredCommands() {
        return HartshornUtils.asUnmodifiableMap(this.registeredCommands);
    }
}
