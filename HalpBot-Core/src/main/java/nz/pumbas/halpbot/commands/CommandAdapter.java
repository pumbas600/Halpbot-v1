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

package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Reflective;
import nz.pumbas.halpbot.commands.annotations.SlashCommand;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.constructor.CustomConstructorContext;
import nz.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.configurations.BotConfiguration;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

public interface CommandAdapter extends HalpbotAdapter
{
    ParameterAnnotationService parameterAnnotationService();

    @Override
    default void onEvent(GenericEvent event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent)
            this.onMessageReceived(messageReceivedEvent);
    }

    void onMessageReceived(MessageReceivedEvent event);

    String defaultPrefix();

    void defaultPrefix(String defaultPrefix);

    String prefix(long guildId);

    UsageBuilder usageBuilder();

    void usageBuilder(UsageBuilder usageBuilder);

    @Override
    default void enable() throws ApplicationException {
        BotConfiguration config = this.applicationContext().get(BotConfiguration.class);
        this.defaultPrefix(config.defaultPrefix());
        if (this.defaultPrefix().isBlank())
            throw new ApplicationException(
                "A 'defaultPrefix' must be defined in the bot-config.properties file if you're using commands");

        this.determineUsageBuilder(config);
        HalpbotAdapter.super.enable();
    }

    default void determineUsageBuilder(BotConfiguration config) {
        TypeContext<?> typeContext = TypeContext.lookup(config.usageBuilder());
        if (typeContext.childOf(UsageBuilder.class)) {
            UsageBuilder usageBuilder = (UsageBuilder) this.applicationContext().get(typeContext);
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

    default <T> void registerCommands(TypeContext<T> type) {
        int messageCommands = 0, slashCommands = 0, reflectiveCommands = 0;

        for (MethodContext<?, T> methodContext : type.methods(Command.class)) {
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

    <T> void registerMessageCommand(T instance, MethodContext<?, T> methodContext);

    <T> void registerSlashCommand(T instance, MethodContext<?, T> methodContext);

    void registerReflectiveCommand(MethodContext<?, ?> methodContext);

    default Exceptional<CommandContext> commandContextSafely(String alias) {
        return Exceptional.of(this.commandContext(alias));
    }

    @Nullable
    CommandContext commandContext(String alias);

    Collection<CommandContext> reflectiveCommandContext(TypeContext<?> targetType,
                                                        String methodName,
                                                        Set<TypeContext<?>> reflections);


    Map<String, CommandContext> commands();

    Collection<CustomConstructorContext> customConstructors(TypeContext<?> typeContext);

    void registerCustomConstructors(TypeContext<?> typeContext);

    default String typeAlias(Class<?> type) {
        return this.typeAlias(TypeContext.of(type));
    }

    String typeAlias(TypeContext<?> typeContext);

    default boolean parameterAnnotationsAreValid(ExecutableElementContext<?, ?> executable) {
        for (ParameterContext<?> parameterContext : executable.parameters()) {
            TypeContext<?> parameterType = parameterContext.type();
            List<TypeContext<? extends Annotation>> parameterAnnotations = parameterContext.annotations()
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
}
