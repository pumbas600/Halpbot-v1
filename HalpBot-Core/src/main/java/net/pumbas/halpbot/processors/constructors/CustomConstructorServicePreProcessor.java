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

package net.pumbas.halpbot.processors.constructors;

import net.pumbas.halpbot.commands.annotations.CustomConstructor;
import net.pumbas.halpbot.utilities.handlervalidation.HandlerValidator;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.processing.ServicePreProcessor;
import org.dockbox.hartshorn.inject.Key;
import org.dockbox.hartshorn.util.reflect.TypeContext;

public class CustomConstructorServicePreProcessor implements ServicePreProcessor {

    private static final HandlerValidator CUSTOM_CONSTRUCTOR_VALIDATOR = HandlerValidator.publicModifier("custom constructor");

    @Override
    public boolean preconditions(final ApplicationContext context, final Key<?> key) {
        return key.type().constructors()
            .stream()
            .anyMatch(constructorContext -> constructorContext.annotation(CustomConstructor.class).present());
    }

    @Override
    public <T> void process(final ApplicationContext context, final Key<T> key) {
        final TypeContext<T> type = key.type();
        final CustomConstructorContext customConstructorContext = context.first(CustomConstructorContext.class).get();

        type.constructors()
            .stream()
            .filter(constructor -> constructor.annotation(CustomConstructor.class).present()
                && CUSTOM_CONSTRUCTOR_VALIDATOR.isValid(context, constructor))
            .forEach(constructor -> customConstructorContext.register(type, constructor));
    }
}
