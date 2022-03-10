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

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;

@Singleton
@ComponentBinding(ParameterAnnotationService.class)
public class HalpbotParameterAnnotationService implements ParameterAnnotationService
{
    private final Map<TypeContext<? extends Annotation>, ParameterAnnotationContext> parameterAnnotationContextMap
        = new ConcurrentHashMap<>();

    @Inject
    @Getter
    private ApplicationContext applicationContext;
    @Inject
    @Getter
    private ParameterAnnotationContextFactory factory;

    @Getter
    private final Comparator<TypeContext<? extends Annotation>> comparator = (typeA, typeB) -> {
        ParameterAnnotationContext contextA = this.get(typeA);
        ParameterAnnotationContext contextB = this.get(typeB);

        if (contextA.comesAfter(typeB))
            return 1;
        else if (contextB.comesAfter(typeA))
            return -1;
        else if (contextA.afterAnnotations().size() > contextB.afterAnnotations().size())
            return 1;
        else if (contextB.afterAnnotations().size() > contextA.afterAnnotations().size())
            return -1;
        return 0;
    };

    @Override
    public ParameterAnnotationContext get(TypeContext<? extends Annotation> annotationType) {
        return this.parameterAnnotationContextMap
            .getOrDefault(annotationType, HalpbotParameterAnnotationContext.GENERIC);
    }

    @Override
    public boolean isRegisteredParameterAnnotation(TypeContext<? extends Annotation> annotationType) {
        return this.parameterAnnotationContextMap.containsKey(annotationType);
    }

    @Override
    public void add(TypeContext<? extends Annotation> annotationType,
                    ParameterAnnotationContext annotationContext) {
        this.parameterAnnotationContextMap.put(annotationType, annotationContext);
    }

    @Override
    public boolean contains(TypeContext<? extends Annotation> annotationType) {
        return this.parameterAnnotationContextMap.containsKey(annotationType);
    }
}
