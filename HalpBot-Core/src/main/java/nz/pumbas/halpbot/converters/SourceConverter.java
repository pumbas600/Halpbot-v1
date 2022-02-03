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

package nz.pumbas.halpbot.converters;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;

@Getter
@RequiredArgsConstructor
public class SourceConverter<T> implements Converter<InvocationContext, T>
{
    private final TypeContext<T> type;
    private final TypeContext<? extends Annotation> annotationType;
    private final Function<InvocationContext, Exceptional<T>> mapper;
    private final OptionType optionType;

    public static <T> SourceConverterBuilder<T> builder(TypeContext<T> type) {
        return new SourceConverterBuilder<>(type);
    }

    public static <T> SourceConverterBuilder<T> builder(Class<T> type) {
        return builder(TypeContext.of(type));
    }

    public static class SourceConverterBuilder<T> extends ConverterBuilder<SourceConverter<T>, InvocationContext, T> {

        protected SourceConverterBuilder(TypeContext<T> type) {
            super(type);
        }

        @Override
        public SourceConverter<T> build() {
            this.assertConverterSet();
            return new SourceConverter<>(
                    this.type,
                    TypeContext.of(this.annotation),
                    this.converter,
                    this.optionType);
        }
    }
}
