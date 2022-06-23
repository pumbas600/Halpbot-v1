/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.pumbas.halpbot.HalpbotCore;
import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.commands.actioninvokable.HalpbotCommandInvokable;
import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import net.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import net.pumbas.halpbot.commands.actioninvokable.context.command.CommandContextFactory;
import net.pumbas.halpbot.commands.actioninvokable.context.constructor.CustomConstructorContext;
import net.pumbas.halpbot.commands.actioninvokable.context.constructor.CustomConstructorContextFactory;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.commands.annotations.CustomConstructor;
import net.pumbas.halpbot.commands.exceptions.MissingResourceException;
import net.pumbas.halpbot.commands.usage.UsageBuilder;
import net.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;
import net.pumbas.halpbot.converters.tokens.Token;
import net.pumbas.halpbot.converters.tokens.TokenService;
import net.pumbas.halpbot.decorators.DecoratorService;
import net.pumbas.halpbot.events.HalpbotEvent;
import net.pumbas.halpbot.events.MessageEvent;
import net.pumbas.halpbot.utilities.HalpbotUtils;
import net.pumbas.halpbot.utilities.validation.ElementValidator;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Component;
import org.dockbox.hartshorn.util.ArrayListMultiMap;
import org.dockbox.hartshorn.util.MultiMap;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.AccessModifier;
import org.dockbox.hartshorn.util.reflect.ConstructorContext;
import org.dockbox.hartshorn.util.reflect.ExecutableElementContext;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Component
@Accessors(chain = false)
public class HalpbotCommandAdapter implements CommandAdapter {

    private static final ElementValidator REFLECTIVE_COMMAND_VALIDATOR = ElementValidator.build("reflective command")
        .modifiers(AccessModifier.PUBLIC, AccessModifier.STATIC)
        .returnTypeNot(Void.class)
        .create();

    private final MultiMap<TypeContext<?>, CustomConstructorContext> customConstructors = new ArrayListMultiMap<>();
    private final Map<String, CommandContext> commands = new ConcurrentHashMap<>();
    private final Map<TypeContext<?>, MultiMap<String, CommandContext>> reflectiveCommands = new ConcurrentHashMap<>();

    private final Map<Long, String> guildPrefixes = new ConcurrentHashMap<>();

    @Setter
    @Getter
    private String defaultPrefix;
    @Setter
    @Getter
    private UsageBuilder usageBuilder;
    @Inject
    @Getter
    private ApplicationContext applicationContext;
    @Inject
    @Getter
    private ParameterAnnotationService parameterAnnotationService;
    @Inject
    @Getter
    private HalpbotCore halpbotCore;

    @Inject
    private CommandContextFactory commandContextFactory;
    @Inject
    private CommandInvocationContextFactory invocationContextFactory;
    @Inject
    private CustomConstructorContextFactory customConstructorContextFactory;
    @Inject
    private TokenService tokenService;
    @Inject
    private DecoratorService decoratorService;

    //TODO: Setting the guild specific prefixes
    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();
        final String prefix = event.isFromType(ChannelType.TEXT)
            ? this.prefix(event.getGuild().getIdLong())
            : this.defaultPrefix;

        final HalpbotEvent halpbotEvent = new MessageEvent(event);

        if (message.startsWith(prefix)) {
            message = message.substring(prefix.length()).stripLeading();

            final String[] splitText = message.split("\\s", 2);
            final String alias = splitText[0];
            String content = (2 == splitText.length) ? splitText[1] : "";

            final Result<CommandContext> eCommandContext = this.commandContextSafely(alias);
            if (eCommandContext.present()) {
                final CommandContext commandContext = eCommandContext.get();
                if (commandContext.content() != Content.RAW) {
                    final String tempContent = commandContext.content().parse(event);
                    final int startIndex = tempContent.indexOf(alias);
                    if (startIndex != -1) {
                        content = tempContent.substring(startIndex + alias.length());
                    }
                }

                if (!commandContext.preserveWhitespace())
                    content = content.replaceAll("\\s+", " ");

                final Result<Object> result = this.handleCommandInvocation(halpbotEvent, commandContext, content);

                if (result.present())
                    this.displayResult(halpbotEvent, commandContext, result.get());
                else if (result.caught()) {
                    //this.applicationContext.log().error("Caught the error: ", result.error());
                    this.handleException(halpbotEvent, result.error());
                }
            }
            else this.halpbotCore.displayConfiguration()
                .displayTemporary(halpbotEvent,
                    "The command **" + alias + "** doesn't seem to exist, you may want to check your spelling",
                    30);
        }
    }

    private Result<Object> handleCommandInvocation(final HalpbotEvent event,
                                                   final CommandContext commandContext,
                                                   final String content)
    {
        final CommandInvocationContext invocationContext = this.invocationContextFactory.command(content, event);
        return commandContext.invoke(invocationContext);
    }

    @Override
    public void registerCustomConstructors(final TypeContext<?> type,
                                           final Collection<ConstructorContext<?>> customConstructors)
    {
        if (customConstructors.isEmpty()) return;

        final List<CustomConstructorContext> constructors = customConstructors.stream()
            .map(constructor -> {
                final CustomConstructor construction = constructor.annotation(CustomConstructor.class).get();
                final List<Token> tokens = this.tokenService.tokens(constructor);

                return this.customConstructorContextFactory.create(
                    this.usage(construction.usage(), constructor),
                    this.decoratorService.decorate(new HalpbotCommandInvokable(null, constructor)),
                    this.reflections(construction.reflections()),
                    tokens);
            })
            .collect(Collectors.toList());

        this.applicationContext.log().info("Registered {} custom constructors found in {}", constructors.size(), type.qualifiedName());
        this.customConstructors.putAll(type, constructors);
    }

    @Override
    public <T> void registerSlashCommand(final T instance, final MethodContext<?, T> methodContext) {
        //TODO: Slash Commands
    }

    @Override
    public void registerReflectiveCommand(final MethodContext<?, ?> methodContext) {
        if (!REFLECTIVE_COMMAND_VALIDATOR.isValid(this.applicationContext, methodContext))
            return;

        if (!this.parameterAnnotationsAreValid(methodContext)) return;

        final Command command = methodContext.annotation(Command.class).get();
        final List<String> aliases = this.aliases(command, methodContext);
        final CommandContext commandContext = this.createCommand(
            aliases,
            command,
            methodContext,
            new HalpbotCommandInvokable(null, methodContext));

        final TypeContext<?> returnType = methodContext.genericReturnType();
        if (!this.reflectiveCommands.containsKey(returnType))
            this.reflectiveCommands.put(returnType, new ArrayListMultiMap<>());

        final MultiMap<String, CommandContext> aliasMappings = this.reflectiveCommands.get(returnType);

        for (final String alias : aliases) {
            aliasMappings.put(alias.toLowerCase(Locale.ROOT), commandContext);
        }
    }

    @Override
    public <T> void registerMessageCommand(final T instance, final MethodContext<?, T> methodContext) {
        if (!this.parameterAnnotationsAreValid(methodContext))
            return;

        final Command command = methodContext.annotation(Command.class).get();
        final List<String> aliases = this.aliases(command, methodContext);
        final CommandContext commandContext = this.createCommand(
            aliases,
            command,
            methodContext,
            new HalpbotCommandInvokable(instance, methodContext));

        for (final String alias : aliases) {
            if (this.commands.containsKey(alias)) {
                this.applicationContext.log().warn(
                    "The alias '%s' is already being used by the command '%s'. The command %s will not be registered under this alias"
                        .formatted(alias, this.commands.get(alias).toString(), commandContext.toString()));
                continue;
            }

            this.commands.put(alias, commandContext);
        }
    }

    @Override
    public String prefix(final long guildId) {
        return this.guildPrefixes.getOrDefault(guildId, this.defaultPrefix);
    }

    @Override
    @Nullable
    public CommandContext commandContext(final String alias) {
        return this.commands.get(alias.toLowerCase(Locale.ROOT));
    }

    @Override
    public Collection<CommandContext> reflectiveCommandContext(final TypeContext<?> targetType,
                                                               final String methodName,
                                                               final Set<TypeContext<?>> reflections)
    {
        if (!this.reflectiveCommands.containsKey(targetType))
            return Collections.emptyList();

        return this.reflectiveCommands.get(targetType).get(methodName.toLowerCase(Locale.ROOT))
            .stream()
            .filter(commandContext -> commandContext.executable() instanceof MethodContext methodContext
                && reflections.contains(methodContext.parent()))
            .toList();
    }

    @Override
    public Map<String, CommandContext> commands() {
        return Collections.unmodifiableMap(this.commands);
    }

    @Override
    public Collection<CustomConstructorContext> customConstructors(final TypeContext<?> typeContext) {
        if (!this.customConstructors.containsKey(typeContext))
            throw new MissingResourceException(
                "There is no custom constructor registered for the type %s".formatted(typeContext.qualifiedName()));
        return this.customConstructors.get(typeContext);

    }

    private List<String> aliases(final Command command, final MethodContext<?, ?> methodContext) {
        final List<String> aliases = Arrays.stream(command.alias())
            .map(alias -> alias.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

        // If an alias hasn't been specified, use the method name
        if (aliases.isEmpty())
            aliases.add(methodContext.name().toLowerCase(Locale.ROOT));
        return aliases;
    }

    private <T> CommandContext createCommand(final List<String> aliases,
                                             final Command command,
                                             final MethodContext<?, T> methodContext,
                                             final ActionInvokable<CommandInvocationContext> actionInvokable)
    {
        final TypeContext<T> parent = methodContext.parent();
        final Set<TypeContext<?>> reflections = this.reflections(command.reflections());

        if (parent.annotation(Command.class).present()) {
            final Command sharedProperties = parent.annotation(Command.class).get();
            reflections.addAll(this.reflections(sharedProperties.reflections()));
        }

        return this.commandContextFactory.create(
            aliases,
            command.description(),
            this.usage(command.usage(), methodContext),
            this.decoratorService.decorate(actionInvokable),
            this.tokenService.tokens(methodContext),
            reflections,
            HalpbotUtils.asDuration(command.display()),
            command.isEphemeral(),
            command.preserveWhitespace(),
            command.content()
        );
    }

    private String usage(final String usage, final ExecutableElementContext<?, ?> executable) {
        if (!usage.isBlank())
            return usage;
        else return this.usageBuilder.buildUsage(this.applicationContext, executable);
    }

    private Set<TypeContext<?>> reflections(final Class<?>[] reflections) {
        return Stream.of(reflections).map(TypeContext::of).collect(Collectors.toSet());
    }
}
