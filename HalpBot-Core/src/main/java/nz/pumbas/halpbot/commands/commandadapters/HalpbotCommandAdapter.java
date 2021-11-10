package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.boot.ApplicationState.Started;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.events.EngineChangedState;
import org.dockbox.hartshorn.events.annotations.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.Getter;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.commandmethods.CommandContext;
import nz.pumbas.halpbot.commands.commandmethods.HalpbotCommandContext;
import nz.pumbas.halpbot.commands.exceptions.CommandException;
import nz.pumbas.halpbot.commands.exceptions.IllegalPrefixException;
import nz.pumbas.halpbot.commands.exceptions.UndefinedActivatorException;
import nz.pumbas.halpbot.commands.usage.NameVariableBuilder;
import nz.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.common.annotations.Bot;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.utilities.ErrorManager;

@Service
@Binds(CommandAdapter.class)
public class HalpbotCommandAdapter extends HalpbotAdapter implements CommandAdapter, ContextCarrier
{

    private final Map<String, CommandContext> registeredCommands = HartshornUtils.emptyMap();

    @Getter private String prefix;
    @Getter private UsageBuilder usageBuilder;

    @Inject
    @Getter private ApplicationContext applicationContext;


    @Listener
    public void on(@NotNull EngineChangedState<Started> event) throws UndefinedActivatorException, IllegalPrefixException {
        if (!this.applicationContext.hasActivator(Bot.class))
            throw new UndefinedActivatorException("The @Bot activator must be present on the main class");

        this.prefix = this.applicationContext.activator(Bot.class).prefix();
        if (this.prefix.isBlank())
            throw new IllegalPrefixException("The prefix defined in @Bot cannot be blank");

        this.determineUsageBuilder();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
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
                .absent(() -> this.halpBotCore.getDisplayConfiguration()
                    .displayTemporary(halpbotEvent,
                        "The command **" + commandAlias + "** doesn't seem to exist, you may want to check your spelling",
                        30))
                .flatMap(commandContext ->
                    this.handleCommandInvocation(halpbotEvent, commandContext, content))
                .present(output -> this.getHalpbotCore().getDisplayConfiguration().display(halpbotEvent, output))
                .caught(exception -> {
                    ErrorManager.handle(event, exception);
                    this.getHalpbotCore().getDisplayConfiguration()
                        .displayTemporary(
                            halpbotEvent,
                            "There was the following error trying to invoke this command: " + exception.getMessage(),
                            30);
                });
        }
    }

    @NotNull
    private Exceptional<Object> handleCommandInvocation(@NotNull HalpbotEvent event,
                                                        @NotNull CommandContext commandContext,
                                                        @NotNull String content)
    {
        if (!commandContext.hasPermission(event.getUser()))
            return Exceptional.of(new CommandException("You do not have permission to use this command"));

//        nz.pumbas.halpbot.commands.context.MethodContext ctx = nz.pumbas.halpbot.commands.context.MethodContext.of(content, this.halpBotCore, event, commandContext.reflections());
//
//        return commandContext.parse(ctx, false);
        //TODO: Implement command invocation handling
        return Exceptional.empty();
    }

    private void hasMethodParameterNamesVerifier(String sampleParameter) { }

    private void determineUsageBuilder() {
        if ("arg0".equals(TypeContext.of(this)
                .method("hasMethodParameterNamesVerifier")
                .get()
                .parameters()
                .get(0)
                .name()))
        {
            this.usageBuilder = new TypeUsageBuilder();
        } else {
            this.usageBuilder = new NameVariableBuilder();
        }
    }

    @Override
    public <T> void registerCommands(@NotNull TypeContext<T> typeContext)     {
        T instance = this.applicationContext.get(typeContext);
        this.registerCommandContext(
            instance,
            typeContext.methods(Command.class));
    }

    @Override
    @Nullable
    public CommandContext commandContext(@NotNull String alias) {
        return this.registeredCommands.get(alias);
    }

    private <T> void registerCommandContext(@NotNull T instance, @NotNull List<MethodContext<?, T>> annotatedMethods)
    {
        for (MethodContext<?, T> methodContext : annotatedMethods) {
            if (!methodContext.isPublic()) {
                this.applicationContext.log().warn("The command method %s should be public if its annotated with @Command"
                        .formatted(methodContext.qualifiedName()));
                continue;
            }
            Command command = methodContext.annotation(Command.class).get();
            List<String> aliases = this.aliases(command, methodContext);
            CommandContext commandContext = this.createCommand(aliases, instance, command, methodContext);

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

    @NotNull
    private List<String> aliases(@NotNull Command command, @NotNull MethodContext<?, ?> methodContext) {
        List<String> aliases = HartshornUtils.asList(command.alias())
            .stream()
            .map(alias -> alias.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

        // If an alias hasn't been specified, use the method name
        if (aliases.isEmpty())
            aliases.add(methodContext.name().toLowerCase(Locale.ROOT));
        return aliases;
    }

    @NotNull
    private String usage(@NotNull Command command, @NotNull MethodContext<?, ?> methodContext) {
        if (!command.usage().isBlank())
            return command.usage();
        else return this.usageBuilder.buildUsage(this.applicationContext, methodContext);
    }

    @NotNull
    private <T> CommandContext createCommand(@NotNull List<String> aliases,
                                             @NotNull T instance,
                                             @NotNull Command command,
                                             @NotNull MethodContext<?, T> methodContext)
    {
        return new HalpbotCommandContext(
            aliases,
            command.description(),
            this.usage(command, methodContext),
            instance,
            methodContext,
            List.of(command.permissions()),
            Stream.of(command.reflections()).map(TypeContext::of).collect(Collectors.toSet()));
    }

    @Override
    @NotNull
    public Map<String, CommandContext> registeredCommands() {
        return HartshornUtils.asUnmodifiableMap(this.registeredCommands);
    }
}
