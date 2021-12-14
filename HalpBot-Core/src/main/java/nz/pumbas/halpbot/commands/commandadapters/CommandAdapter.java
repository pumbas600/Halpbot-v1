package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.ReflectiveCommand;
import nz.pumbas.halpbot.commands.annotations.SlashCommand;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.customconstructors.CustomConstructorContext;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

public interface CommandAdapter extends HalpbotAdapter
{
    ParameterAnnotationService parameterAnnotationService();

    @SubscribeEvent
    void onMessageReceived(MessageReceivedEvent event);

    String prefix();

    default <T> void registerCommands(T instance) {
        TypeContext<T> type = TypeContext.of(instance);
        int messageCommands = 0, slashCommands = 0, reflectiveCommands = 0;

        for (MethodContext<?, T> methodContext : type.methods(Command.class)) {
            if (methodContext.annotation(SlashCommand.class).present()) {
                slashCommands++;
                this.registerSlashCommand(instance, methodContext);
            } else if (methodContext.annotation(ReflectiveCommand.class).present()) {
                reflectiveCommands++;
                this.registerReflectiveCommand(methodContext);
            } else {
                messageCommands++;
                this.registerMessageCommand(instance, methodContext);
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

    Exceptional<CommandContext> reflectiveCommandContext(TypeContext<?> Type, String methodName);

    Map<String, CommandContext> registeredCommands();

    Collection<CustomConstructorContext> customConstructors(TypeContext<?> typeContext);

    void registerCustomConstructors(TypeContext<?> typeContext);

    default String typeAlias(Class<?> type) {
        return this.typeAlias(TypeContext.of(type));
    }

    String typeAlias(TypeContext<?> typeContext);

    default boolean parameterAnnotationsAreValid(ExecutableElementContext<?> executable) {
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
