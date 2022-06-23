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

package net.pumbas.halpbot.commands.actioninvokable.context;

import net.pumbas.halpbot.actions.invokable.InvocationContext;
import net.pumbas.halpbot.converters.tokens.Token;
import net.pumbas.halpbot.utilities.StringTraverser;

import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.ParameterContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public interface CommandInvocationContext extends StringTraverser, InvocationContext {

    Set<TypeContext<?>> reflections();

    int currentAnnotationIndex();

    List<TypeContext<? extends Annotation>> sortedAnnotations();

    List<Token> tokens();

    boolean canHaveContextLeft();

    void reflections(Set<TypeContext<?>> reflections);

    void incrementCurrentAnnotationIndex();

    void canHaveContextLeft(boolean canHaveContextLeft);

    void tokens(List<Token> tokens);

    /**
     * Updates the current type, sorted annotations, parameter annotations and resets the current annotation index using
     * the {@link ParameterContext} and provided sorted annotations.
     *
     * @param parameterContext
     *     The {@link ParameterContext} to get the current type and annotations from
     * @param sortedAnnotations
     *     The new sorted annotations to use
     */
    default void update(ParameterContext<?> parameterContext,
                        List<TypeContext<? extends Annotation>> sortedAnnotations)
    {
        this.currentType(parameterContext.genericType());
        this.annotations(parameterContext.annotations());
        this.sortedAnnotations(sortedAnnotations);
        this.currentAnnotationIndex(0);
    }

    void annotations(Set<Annotation> annotations);

    void sortedAnnotations(List<TypeContext<? extends Annotation>> sortedAnnotations);

    void currentAnnotationIndex(int index);

    /**
     * Increments the {@link CommandInvocationContext#currentAnnotationIndex()} by one.
     */
    void incrementAnnotationIndex();

    /**
     * Retrieves an {@link Result} containing the first annotation with the specified type, or {@link Result#empty()} if
     * there are no matching annotations.
     *
     * @param annotationType
     *     The {@link Class} of the annotation
     * @param <T>
     *     The type of the annotation
     *
     * @return An {@link Result} containing the annotation, or nothing if no matching annotations were found
     */
    @SuppressWarnings("unchecked")
    default <T extends Annotation> Result<T> annotation(Class<T> annotationType) {
        return Result.of(this.annotations().stream()
            .filter(annotation -> annotationType.isAssignableFrom(annotation.annotationType()))
            .findFirst()
            .map(annotation -> (T) annotation));
    }

    Set<Annotation> annotations();
}
