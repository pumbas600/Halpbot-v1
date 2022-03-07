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

package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;

@Singleton
@ComponentBinding(DecoratorService.class)
public class HalpbotDecoratorService implements DecoratorService
{
    private final Map<TypeContext<? extends Annotation>, DecoratorFactory<?, ?, ?>> decorators = new ConcurrentHashMap<>();

    @Getter
    @Inject
    private ApplicationContext applicationContext;

    @Override
    public void register(TypeContext<? extends Annotation> decoratedAnnotation) {
        Class<? extends DecoratorFactory<?, ?, ?>> factoryType = decoratedAnnotation.annotation(Decorator.class)
            .map(Decorator::value)
            .get();

        DecoratorFactory<?, ?, ?> factory = this.applicationContext.get(factoryType);
        if (factory == null) {
            this.applicationContext.log().error("There was an error retrieving the decorator factory %s"
                .formatted(factoryType.getCanonicalName()));
            return;
        }

        this.decorators.put(decoratedAnnotation, factory);
    }

    @Override
    @Nullable
    public DecoratorFactory<?, ?, ?> decorator(TypeContext<? extends Annotation> decoratedAnnotation) {
        return this.decorators.get(decoratedAnnotation);
    }
}
