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

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.pumbas.halpbot.adapters.HalpbotAdapter;
import net.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import net.pumbas.halpbot.commands.actioninvokable.context.constructor.CustomConstructorContext;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.commands.annotations.Reflective;
import net.pumbas.halpbot.commands.annotations.SlashCommand;
import net.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import net.pumbas.halpbot.commands.usage.UsageBuilder;
import net.pumbas.halpbot.configurations.BotConfiguration;
import net.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;
import net.pumbas.halpbot.processors.commands.CommandHandlerContext;

import org.dockbox.hartshorn.component.Enableable;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.ExecutableElementContext;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.ParameterContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface CommandAdapter extends HalpbotAdapter, Enableable {

    @Override
    default void onEvent(final GenericEvent event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent)
            this.onMessageReceived(messageReceivedEvent);
    }

    void onMessageReceived(MessageReceivedEvent event);

    @Override
    default void enable() throws ApplicationException {
        final BotConfiguration config = this.applicationContext().get(BotConfiguration.class);
        this.defaultPrefix(config.defaultPrefix());
        if (this.defaultPrefix().isBlank())
            throw new ApplicationException(
                "A 'defaultPrefix' must be defined in the bot-config.properties file if you're using commands");

        this.determineUsageBuilder(config);
        final CommandHandlerContext commandHandlerContext =
            this.applicationContext().first(CommandHandlerContext.class).get();
        
    }

    void defaultPrefix(String defaultPrefix);

    String defaultPrefix();

    default void determineUsageBuilder(final BotConfiguration config) {
        final TypeContext<?> typeContext = TypeContext.lookup(config.usageBuilder());
        if (typeContext.childOf(UsageBuilder.class)) {
            final UsageBuilder usageBuilder = (UsageBuilder) this.applicationContext().get(typeContext);
            if (usageBuilder.isValid(this.applicationContext())) {
                this.usageBuilder(usageBuilder);
                return;
            } else this.applicationContext().log()
                .warn("The usage builder %s defined in bot-config.properties is not valid"
                    .formatted(typeContext.qualifiedName()));
        } else this.applicationContext().log()
            .warn("The usage builder %s defined in bot-config.properties must implement UsageBuilder"
                .formatted(config.displayConfiguration()));

        this.applicationContext().log().warn("Falling back to usage builder %s"
            .formatted(TypeUsageBuilder.class.getCanonicalName()));
        this.usageBuilder(new TypeUsageBuilder());
    }

    void usageBuilder(UsageBuilder usageBuilder);

    String prefix(long guildId);

    UsageBuilder usageBuilder();

    default <T> void registerCommands(final TypeContext<T> type) {
        int messageCommands = 0, slashCommands = 0, reflectiveCommands = 0;

        for (final MethodContext<?, T> methodContext : type.methods(Command.class)) {
            if (methodContext.annotation(SlashCommand.class).present()) {
                slashCommands++;
                this.registerSlashCommand(this.applicationContext().get(type), methodContext);
            } else if (methodContext.annotation(Reflective.class).present()) {
                reflectiveCommands++;
                this.registerReflectiveCommand(methodContext);
            } else {
                messageCommands++;
                this.registerMessageCommand(this.applicationContext().get(type), methodContext);
            }
        }

        this.applicationContext().log().info("Registered %d message; %d slash; %d reflective commands found in %s"
            .formatted(messageCommands, slashCommands, reflectiveCommands, type.qualifiedName()));
    }

    <T> void registerSlashCommand(T instance, MethodContext<?, T> methodContext);

    void registerReflectiveCommand(MethodContext<?, ?> methodContext);

    <T> void registerMessageCommand(T instance, MethodContext<?, T> methodContext);

    default Result<CommandContext> commandContextSafely(final String alias) {
        return Result.of(this.commandContext(alias));
    }

    @Nullable
    CommandContext commandContext(String alias);

    Collection<CommandContext> reflectiveCommandContext(TypeContext<?> targetType,
                                                        String methodName,
                                                        Set<TypeContext<?>> reflections);

    Map<String, CommandContext> commands();

    Collection<CustomConstructorContext> customConstructors(TypeContext<?> typeContext);

    void registerCustomConstructors(TypeContext<?> typeContext);

    default String typeAlias(final Class<?> type) {
        return this.typeAlias(TypeContext.of(type));
    }

    String typeAlias(TypeContext<?> typeContext);

    default boolean parameterAnnotationsAreValid(final ExecutableElementContext<?, ?> executable) {
        for (final ParameterContext<?> parameterContext : executable.parameters()) {
            final TypeContext<?> parameterType = parameterContext.type();
            final List<TypeContext<? extends Annotation>> parameterAnnotations = parameterContext.annotations()
                .stream()
                .map(annotation -> TypeContext.of(annotation.annotationType()))
                .collect(Collectors.toList());

            if (!this.parameterAnnotationService().isValid(parameterType, parameterAnnotations)) {
                this.applicationContext().log()
                    .error("There are conflicts regarding the annotations on the %s type in the executable %s"
                        .formatted(parameterType.qualifiedName(), executable.name()));
                return false;
            }
        }
        return true;
    }

    ParameterAnnotationService parameterAnnotationService();
}
