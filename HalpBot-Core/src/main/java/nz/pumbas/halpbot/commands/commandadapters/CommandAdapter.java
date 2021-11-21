package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.commandmethods.CommandContext;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

@Service
public interface CommandAdapter extends HalpbotAdapter
{
    ParameterAnnotationService parameterAnnotationService();

    @SubscribeEvent
    void onMessageReceived(@NotNull MessageReceivedEvent event);

    @NotNull
    String prefix();

    <T> void registerCommands(@NotNull TypeContext<T> typeContext);

    @NotNull
    default Exceptional<CommandContext> commandContextSafely(@NotNull String alias) {
        return Exceptional.of(this.commandContext(alias));
    }

    @Nullable
    CommandContext commandContext(@NotNull String alias);

    @NotNull
    Map<String, CommandContext> registeredCommands();

    default boolean parameterAnnotationsAreValid(@NotNull ExecutableElementContext<?> executable) {
        for (ParameterContext<?> parameterContext : executable.parameters()) {
            TypeContext<?> parameterType = parameterContext.type();
            List<TypeContext<? extends Annotation>> parameterAnnotations = parameterContext.annotations()
                    .stream()
                    .map(annotation -> TypeContext.of(annotation.annotationType()))
                    .collect(Collectors.toList());

            if (!this.parameterAnnotationService().isValid(parameterType, parameterAnnotations)) {
                this.applicationContext().log()
                        .error("There are conflicts regarding the annotations on the %s type in the executable %s"
                                .formatted(parameterType.qualifiedName(), this.name(executable)));
                return false;
            }
        }
        return true;
    }

    //TODO: Remove when this is added directly to ExecutableElementContext<?>
    private String name(ExecutableElementContext<?> executable) {
        if (executable instanceof MethodContext<?, ?> methodContext) {
            return methodContext.name();
        }
        ConstructorContext<?> constructorContext = (ConstructorContext<?>) executable;
        return constructorContext.name();
    }
}
