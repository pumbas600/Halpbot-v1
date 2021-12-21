package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import org.dockbox.hartshorn.core.ArrayListMultiMap;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.AccessModifier;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
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

import lombok.Setter;
import lombok.experimental.Accessors;
import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.commands.annotations.CustomParameter;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.CommandContextFactory;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.InvocationContextFactory;
import nz.pumbas.halpbot.commands.context.parsing.MessageParsingContext;
import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;
import nz.pumbas.halpbot.commands.customconstructors.CustomConstructorContext;
import nz.pumbas.halpbot.commands.customconstructors.CustomConstructorContextFactory;
import nz.pumbas.halpbot.commands.exceptions.IllegalCustomParameterException;
import nz.pumbas.halpbot.commands.exceptions.MissingResourceException;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;
import nz.pumbas.halpbot.converters.tokens.TokenService;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.permissions.PermissionService;
import nz.pumbas.halpbot.permissions.exceptions.InsufficientPermissionException;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.Reflect;

@Service
@Binds(CommandAdapter.class)
@Accessors(chain = false)
public class HalpbotCommandAdapter implements CommandAdapter
{
    private final MultiMap<TypeContext<?>, CustomConstructorContext> customConstructors = new ArrayListMultiMap<>();
    private final Map<String, CommandContext> registeredCommands = HartshornUtils.emptyMap();
    private final Map<TypeContext<?>, Map<String, CommandContext>> registeredReflectiveCommands = HartshornUtils.emptyMap();

    private final Map<TypeContext<?>, String> typeAliases = HartshornUtils.emptyMap();
    private final Map<Long, String> guildPrefixes = HartshornUtils.emptyMap();

    @Setter @Getter private String defaultPrefix;
    @Setter @Getter private UsageBuilder usageBuilder;
    @Inject @Getter private ApplicationContext applicationContext;
    @Inject @Getter private ParameterAnnotationService parameterAnnotationService;
    @Inject @Getter private HalpbotCore halpbotCore;
    @Inject @Getter private DecoratorService decoratorService;

    @Inject private PermissionService permissionService;
    @Inject private CommandContextFactory commandContextFactory;
    @Inject private InvocationContextFactory invocationContextFactory;
    @Inject private CustomConstructorContextFactory customConstructorContextFactory;
    @Inject private TokenService tokenService;

    //TODO: Setting the guild specific prefixes
    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // Remove all the additional whitespace
        String message = event.getMessage().getContentRaw().replaceAll("\\s+", " ");
        String prefix = this.prefix(event.getGuild().getIdLong());

        HalpbotEvent halpbotEvent = new MessageEvent(event);

        if (message.startsWith(prefix)) {
            message = message.substring(prefix.length()).stripLeading();
            String[] splitText = message.split(" ", 2);
            String alias       = splitText[0];
            String content     = (2 == splitText.length) ? splitText[1] : "";

            Exceptional<Object> result = this.commandContextSafely(alias)
                .absent(() -> this.halpbotCore.displayConfiguration()
                    .displayTemporary(halpbotEvent,
                        "The command **" + alias + "** doesn't seem to exist, you may want to check your spelling",
                        30))
                .flatMap(commandContext ->
                    this.handleCommandInvocation(halpbotEvent, commandContext, content));

            if (result.present())
                this.halpbotCore.displayConfiguration().display(halpbotEvent, result.get());
            else if (result.caught()) {
                Throwable exception = result.error();
                ErrorManager.handle(event, exception);

                if (exception instanceof ExplainedException explainedException) {
                    this.halpbotCore.displayConfiguration()
                            .displayTemporary(halpbotEvent, explainedException.explanation(), 30);
                }
                else this.halpbotCore.displayConfiguration()
                        .displayTemporary(
                                halpbotEvent,
                                "There was the following error trying to invoke this command: " + exception.getMessage(),
                                30);

            }
        }
    }
    
    private Exceptional<Object> handleCommandInvocation(HalpbotEvent event,
                                                        CommandContext commandContext,
                                                        String content)
    {
        if (!commandContext.hasPermission(this.permissionService, event.getUser()))
            return Exceptional.of(new InsufficientPermissionException("You do not have permission to use this command"));

        InvocationContext invocationContext = this.invocationContextFactory.create(
                content, event, commandContext.reflections());
        return commandContext.invoke(invocationContext, false);
    }

    @Override
    @Nullable
    public CommandContext commandContext(String alias) {
        return this.registeredCommands.get(alias.toLowerCase(Locale.ROOT));
    }

    @Override
    @Nullable
    public CommandContext reflectiveCommandContext(TypeContext<?> type, String methodName) {
        return this.registeredReflectiveCommands.getOrDefault(type, Collections.emptyMap())
                .get(methodName);
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
                        "The alias '%s' is already being used by the command '%s'. The command %s will not be registered under this alias"
                                .formatted(alias, this.registeredCommands.get(alias).toString(), commandContext.toString()));
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
        if (!methodContext.isPublic() && !methodContext.has(AccessModifier.STATIC)) {
            this.applicationContext.log().warn(
                    "The reflective method %s should be public and static if its annotated with @Reflective"
                            .formatted(methodContext.qualifiedName()));
            return;
        }

        if (!this.parameterAnnotationsAreValid(methodContext)) return;

        Command command = methodContext.annotation(Command.class).get();
        List<String> aliases = this.aliases(command, methodContext);
        CommandContext commandContext = this.createCommand(
                aliases,
                null,
                command,
                methodContext,
                new MessageParsingContext());

        TypeContext<?> returnType = methodContext.genericReturnType();
        if (!this.registeredReflectiveCommands.containsKey(returnType))
            this.registeredReflectiveCommands.put(returnType, HartshornUtils.emptyMap());

        Map<String, CommandContext> aliasMappings = this.registeredReflectiveCommands.get(returnType);

        for (String alias : aliases) {
            if (aliasMappings.containsKey(alias)) {
                this.applicationContext.log().warn(
                        "The alias '%s' is already being used by the reflective method '%s'. '%s' will not be registered under this alias"
                                .formatted(alias, aliasMappings.get(alias).toString(), commandContext.toString()));
                continue;
            }

            aliasMappings.put(alias, commandContext);
        }
    }

    @Override
    public String prefix(long guildId) {
        return this.guildPrefixes.getOrDefault(guildId, this.defaultPrefix);
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
                                             @Nullable T instance,
                                             Command command,
                                             MethodContext<?, T> methodContext,
                                             ParsingContext parsingContext)
    {
        return this.decorate(
                this.commandContextFactory.create(
                        aliases,
                        command.description(),
                        this.usage(command.usage(), methodContext),
                        instance,
                        methodContext,
                        parsingContext,
                        this.tokenService.tokens(methodContext),
                        List.of(command.permissions()),
                        Stream.of(command.reflections()).map(TypeContext::of).collect(Collectors.toSet())
                ));
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

        this.applicationContext.log().info("Registered %d custom constructors found in %s"
                .formatted(constructors.size(), typeContext.qualifiedName()));
        this.customConstructors.putAll(typeContext, constructors);
    }

    @Override
    public Map<String, CommandContext> registeredCommands() {
        return Collections.unmodifiableMap(this.registeredCommands);
    }

    @Override
    public String typeAlias(TypeContext<?> typeContext) {
        if (!this.typeAliases.containsKey(typeContext)) {
            String alias;
            if (typeContext.annotation(CustomParameter.class).present())
                alias = typeContext.annotation(CustomParameter.class).get().identifier();
            else if (typeContext.isArray())
                alias = this.typeAlias(typeContext.elementType().get()) + "[]";
            else if (typeContext.isPrimitive())
                alias = Reflect.wrapPrimative(typeContext).name();
            else
                alias = typeContext.name();
            this.typeAliases.put(typeContext, alias);
        }

        return this.typeAliases.get(typeContext);
    }
}
