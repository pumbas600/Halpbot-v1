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

package net.pumbas.halpbot.converters.parametercontext;

import net.pumbas.halpbot.converters.annotations.Any;

import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ConstantDeclaredInInterface")
public interface ParameterAnnotationContext {

    TypeContext<Any> ANY = TypeContext.of(Any.class);

    default boolean comesAfter(final TypeContext<? extends Annotation> annotationType) {
        return this.afterAnnotations().contains(annotationType);
    }

    Set<TypeContext<? extends Annotation>> afterAnnotations();

    void addAfterAnnotation(TypeContext<? extends Annotation> afterAnnotation);

    void conflictingAnnotations(Set<TypeContext<? extends Annotation>> conflictingAnnotations);

    void allowedTypes(Set<TypeContext<?>> allowedTypes);

    default boolean isValidType(final TypeContext<?> typeContext) {
        return this.allowedTypes()
            .stream()
            .anyMatch(type -> type.parentOf(typeContext));
    }

    Set<TypeContext<?>> allowedTypes();

    default boolean noConflictingAnnotations(final List<TypeContext<? extends Annotation>> annotationTypes) {
        return (!this.conflictingAnnotations().contains(ANY) || annotationTypes.size() == 1) &&
            annotationTypes.stream()
                .noneMatch(annotationType -> this.conflictingAnnotations().contains(annotationType));
    }

    Set<TypeContext<? extends Annotation>> conflictingAnnotations();
}
