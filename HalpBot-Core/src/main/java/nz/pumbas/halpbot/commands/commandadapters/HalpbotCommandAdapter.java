package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import org.dockbox.hartshorn.core.ArrayListMultiMap;
import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.Getter;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.commands.annotations.CustomParameter;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.CommandContextFactory;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.InvocationContextFactory;
import nz.pumbas.halpbot.commands.context.parsing.MessageParsingContext;
import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;
import nz.pumbas.halpbot.commands.context.HalpbotInvocationContext;
import nz.pumbas.halpbot.commands.customconstructors.CustomConstructorContext;
import nz.pumbas.halpbot.commands.customconstructors.CustomConstructorContextFactory;
import nz.pumbas.halpbot.commands.exceptions.IllegalCustomParameterException;
import nz.pumbas.halpbot.commands.exceptions.MissingResourceException;
import nz.pumbas.halpbot.commands.usage.NameUsageBuilder;
import nz.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.permissions.PermissionManager;
import nz.pumbas.halpbot.permissions.exceptions.InsufficientPermissionException;
import nz.pumbas.halpbot.utilities.ErrorManager;

@Service
@Binds(CommandAdapter.class)
public class HalpbotCommandAdapter implements CommandAdapter, Enableable
{
    private final MultiMap<TypeContext<?>, CustomConstructorContext> customConstructors = new ArrayListMultiMap<>();
    private final Map<String, CommandContext> registeredCommands = HartshornUtils.emptyMap();
    private final Map<TypeContext<?>, String> cachedTypeAliases = HartshornUtils.emptyMap();

    @Getter private String prefix = "";
    @Getter private UsageBuilder usageBuilder = new TypeUsageBuilder();

    @Inject @Getter private ApplicationContext applicationContext;
    @Inject @Getter private ParameterAnnotationService parameterAnnotationService;

    @Inject private HalpbotCore halpbotCore;
    @Inject private PermissionManager permissionManager;
    @Inject private CommandContextFactory commandContextFactory;
    @Inject private InvocationContextFactory invocationContextFactory;
    @Inject private CustomConstructorContextFactory customConstructorContextFactory;

    @Override
    public void enable() throws ApplicationException {
        this.prefix = this.applicationContext.activator(UseCommands.class).value();
        if (this.prefix.isBlank())
            throw new ApplicationException("The prefix defined in @UseCommands cannot be blank");

        this.usageBuilder = this.determineUsageBuilder();
        this.halpbotCore.registerAdapter(this);
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
                .absent(() -> this.halpbotCore.displayConfiguration()
                    .displayTemporary(halpbotEvent,
                        "The command **" + commandAlias + "** doesn't seem to exist, you may want to check your spelling",
                        30))
                .flatMap(commandContext ->
                    this.handleCommandInvocation(halpbotEvent, commandContext, content))
                .present(output -> this.halpbotCore.displayConfiguration().display(halpbotEvent, output))
                .caught(exception -> {
                    ErrorManager.handle(event, exception);
                    this.halpbotCore.displayConfiguration()
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
        if (!commandContext.hasPermission(this.permissionManager, event.getUser()))
            return Exceptional.of(new InsufficientPermissionException("You do not have permission to use this command"));

        InvocationContext invocationContext = this.invocationContextFactory.create(
                content, event, commandContext.reflections());
        return commandContext.invoke(invocationContext, false);
    }

    private void hasMethodParameterNamesVerifier(String sampleParameter) { }

    private UsageBuilder determineUsageBuilder() {
        if ("arg0".equals(TypeContext.of(this)
                .method("hasMethodParameterNamesVerifier", String.class)
                .get()
                .parameters()
                .get(0)
                .name()))
        {
            this.applicationContext.log().info("Parameter names have not been preserved. Using a type usage builder");
            return new TypeUsageBuilder();
        }
        this.applicationContext.log().info("Parameter names have been preserved. Using a variable name usage builder");
        return new NameUsageBuilder();
    }

    @Override
    @Nullable
    public CommandContext commandContext(String alias) {
        return this.registeredCommands.get(alias.toLowerCase(Locale.ROOT));
    }

    @Override
    public Exceptional<CommandContext> reflectiveCommandContext(TypeContext<?> Type, String methodName) {
        return Exceptional.empty(); //TODO: Retrieve reflective command
    }

    @Override
    public <T> void registerMessageCommand(T instance, MethodContext<?, T> methodContext) {
        if (!methodContext.isPublic()) {
            this.applicationContext.log().warn("The command method %s should be public if its annotated with @Command"
                    .formatted(methodContext.qualifiedName()));
            return;
        }

        if (!this.parameterAnnotationsAreValid(methodContext))
            return;

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

    @Override
    public <T> void registerSlashCommand(T instance, MethodContext<?, T> methodContext) {
        //TODO: Slash Commands
    }

    @Override
    public void registerReflectiveCommand(MethodContext<?, ?> methodContext) {
        //TODO: Reflective Commands
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

    private String usage(String usage, ExecutableElementContext<?> executable) {
        if (!usage.isBlank())
            return usage;
        else return this.usageBuilder.buildUsage(this.applicationContext, executable);
    }

    //TODO: Retrieve some details from the class itself if annotated with @Command
    private <T> CommandContext createCommand(List<String> aliases,
                                             T instance,
                                             Command command,
                                             MethodContext<?, T> methodContext,
                                             ParsingContext parsingContext)
    {
        return this.commandContextFactory.create(
            aliases,
            command.description(),
            this.usage(command.usage(), methodContext),
            instance,
            methodContext,
            List.of(command.permissions()),
            Stream.of(command.reflections()).map(TypeContext::of).collect(Collectors.toSet()),
            parsingContext);
    }

    @Override
    public Collection<CustomConstructorContext> customConstructors(TypeContext<?> typeContext) {
        if (!this.customConstructors.containsKey(typeContext))
            throw new MissingResourceException(
                    "There is no custom constructor registered for the type %s".formatted(typeContext.qualifiedName()));
        return this.customConstructors.get(typeContext);

    }

    @Override
    public void registerCustomConstructors(TypeContext<?> typeContext) {
        List<CustomConstructorContext> constructors = typeContext.constructors()
                .stream()
                .filter(constructor -> constructor.annotation(CustomConstructor.class).present())
                .map(constructor -> {
                    CustomConstructor construction = constructor.annotation(CustomConstructor.class).get();
                    return this.customConstructorContextFactory.create(
                            this.usage(construction.usage(), constructor),
                            constructor,
                            new MessageParsingContext());
                })
                .collect(Collectors.toList());

        if (constructors.isEmpty())
            throw new IllegalCustomParameterException(
                    "The custom class %s, must define a constructor annotated with @ParameterConstructor"
                            .formatted(typeContext.qualifiedName()));

        this.customConstructors.putAll(typeContext, constructors);
    }

    @Override
    public Map<String, CommandContext> registeredCommands() {
        return Collections.unmodifiableMap(this.registeredCommands);
    }

    //TODO: Check if primatives's are displayed as int or Integer
    @Override
    public String typeAlias(TypeContext<?> typeContext) {
        if (!this.cachedTypeAliases.containsKey(typeContext)) {
            this.cachedTypeAliases.put(typeContext,
                    typeContext.annotation(CustomParameter.class).map(CustomParameter::identifier)
                            .or(typeContext.name()));
        }

        return this.cachedTypeAliases.get(typeContext);
    }
}
