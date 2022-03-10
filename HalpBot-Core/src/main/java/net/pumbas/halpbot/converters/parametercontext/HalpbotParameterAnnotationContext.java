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

import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = false)
@ComponentBinding(ParameterAnnotationContext.class)
@AllArgsConstructor(onConstructor_ = @Bound)
public class HalpbotParameterAnnotationContext implements ParameterAnnotationContext
{
    public static final HalpbotParameterAnnotationContext GENERIC = generic();

    private final Set<TypeContext<? extends Annotation>> afterAnnotations;

    @Setter
    private Set<TypeContext<? extends Annotation>> conflictingAnnotations;
    @Setter
    private Set<TypeContext<?>> allowedTypes;

    @Override
    public void addAfterAnnotation(TypeContext<? extends Annotation> afterAnnotation) {
        this.afterAnnotations.add(afterAnnotation);
    }

    public static HalpbotParameterAnnotationContext generic() {
        return new HalpbotParameterAnnotationContext(
            Collections.emptySet(),
            Collections.emptySet(),
            Set.of(TypeContext.of(Object.class)));
    }
}
