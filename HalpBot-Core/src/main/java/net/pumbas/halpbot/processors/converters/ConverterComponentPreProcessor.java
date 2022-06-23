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

package net.pumbas.halpbot.processors.converters;

import net.pumbas.halpbot.converters.Converter;
import net.pumbas.halpbot.converters.annotations.Ignore;
import net.pumbas.halpbot.utilities.validation.ElementValidator;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.processing.ComponentPreProcessor;
import org.dockbox.hartshorn.inject.Key;
import org.dockbox.hartshorn.util.reflect.AccessModifier;
import org.dockbox.hartshorn.util.reflect.TypeContext;

public class ConverterComponentPreProcessor implements ComponentPreProcessor {

    private static final ElementValidator CONVERTER_VALIDATOR = ElementValidator.build("converter")
        .modifiers(AccessModifier.PUBLIC, AccessModifier.STATIC, AccessModifier.FINAL)
        .create();

    @Override
    public boolean modifies(final ApplicationContext context, final Key<?> key) {
        return !key.type().fieldsOf(Converter.class).isEmpty();
    }

    @Override
    public <T> void process(final ApplicationContext context, final Key<T> key) {
        final TypeContext<T> type = key.type();
        final ConverterContext converterContext = context.first(ConverterContext.class).get();

        context.log().debug("Processing converters in {}", type.qualifiedName());

        for (final var fieldContext : type.fieldsOf(Converter.class)) {
            if (fieldContext.annotation(Ignore.class).present() || !CONVERTER_VALIDATOR.isValid(context, fieldContext))
                continue;

            fieldContext.get(null).present(converterContext::register);
        }
    }
}
