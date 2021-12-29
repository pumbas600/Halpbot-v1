package nz.pumbas.halpbot.commands.actioninvokable.context;

import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.utilities.StringTraverser;

public interface CommandInvocationContext extends StringTraverser, InvocationContext
{
    @Nullable
    HalpbotEvent halpbotEvent();

    Set<TypeContext<?>> reflections();

    int currentAnnotationIndex();

    ParameterContext<?> parameterContext();

    List<TypeContext<? extends Annotation>> sortedAnnotations();

    Set<Annotation> annotations();

    List<Token> tokens();

    boolean canHaveContextLeft();

    void reflections(Set<TypeContext<?>> reflections);

    void currentAnnotationIndex(int index);

    void incrementCurrentAnnotationIndex();

    void sortedAnnotations(List<TypeContext<? extends Annotation>> sortedAnnotations);

    void annotations(Set<Annotation> annotations);

    void parameterContext(@Nullable ParameterContext<?> parameterContext);

    void canHaveContextLeft(boolean canHaveContextLeft);

    void tokens(List<Token> tokens);

    /**
     * Updates the current type, sorted annotations, parameter annotations and resets the current annotation index
     * using the {@link ParameterContext} and provided sorted annotations.
     *
     * @param parameterContext
     *      The {@link ParameterContext} to get the current type and annotations from
     * @param sortedAnnotations
     *      The new sorted annotations to use
     */
    default void update(ParameterContext<?> parameterContext,
                        List<TypeContext<? extends Annotation>> sortedAnnotations)
    {
        this.parameterContext(parameterContext);
        this.sortedAnnotations(sortedAnnotations);
        this.currentAnnotationIndex(0);
    }

    /**
     * Increments the {@link CommandInvocationContext#currentAnnotationIndex()} by one.
     */
    void incrementAnnotationIndex();

    /**
     * Retrieves an {@link Exceptional} containing the first annotation with the specified type, or
     * {@link Exceptional#empty()} if there are no matching annotations.
     *
     * @param annotationType
     *      The {@link Class} of the annotation
     * @param <T>
     *      The type of the annotation
     *
     * @return An {@link Exceptional} containing the annotation, or nothing if no matching annotations were found
     */
    @SuppressWarnings("unchecked")
    default <T extends Annotation> Exceptional<T> annotation(Class<T> annotationType) {
        return Exceptional.of(this.annotations().stream()
                .filter(annotation -> annotationType.isAssignableFrom(annotation.annotationType()))
                .findFirst()
                .map(annotation -> (T) annotation));
    }

    @Override
    default String contextString() {
        return this.content();
    }
}
