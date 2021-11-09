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

package nz.pumbas.halpbot.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.commands.annotations.Remaining;
import nz.pumbas.halpbot.commands.annotations.Unmodifiable;
import nz.pumbas.halpbot.commands.context.MethodContext;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.converters.Converters;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class ConverterTests
{

    @Test
    public void retrievingArrayConverterTest() {
        Converter<Object[]> arrayConverter = HalpbotUtils.context().get(ConverterHandler.class)
            .from(MethodContext.of(Object[].class));

        Assertions.assertEquals(Converters.ARRAY_CONVERTER, arrayConverter);
    }

    @Test
    public void retrievingIntegerConverterTest() {
        Converter<Integer> integerConverter = HalpbotUtils.context().get(ConverterHandler.class)
            .from(MethodContext.of(Integer.class));

        Assertions.assertEquals(Converters.INTEGER_CONVERTER, integerConverter);
    }

    @Test
    public void retrievingListConverterTest() {
        Converter<List<?>> listConverter = HalpbotUtils.context().get(ConverterHandler.class)
            .from(MethodContext.of(List.class));

        Assertions.assertEquals(Converters.LIST_CONVERTER, listConverter);
    }

    @Test
    public void retrievingSetConverterTest() {
        Converter<Set<?>> setConverter = HalpbotUtils.context().get(ConverterHandler.class)
            .from(MethodContext.of(Set.class));

        Assertions.assertEquals(Converters.SET_CONVERTER, setConverter);
    }

    @Test
    public void retrievingUnmodifiableListConverterTest() {
        Converter<List<?>> listConverter = HalpbotUtils.context().get(ConverterHandler.class)
            .from(MethodContext.of(List.class, Unmodifiable.class));

        Assertions.assertEquals(Converters.UNMODIFIABLE_LIST_CONVERTER, listConverter);
    }

    @Test
    public void retrievingRemainingStringsConverterTest() {
        Converter<String> remainingStringsConverter = HalpbotUtils.context().get(ConverterHandler.class)
            .from(MethodContext.of(String.class, Remaining.class));

        Assertions.assertEquals(Converters.REMAINING_STRINGS_CONVERTER, remainingStringsConverter);
    }

    @Test
    public void parsingRemainingStringsTest() {
        MethodContext ctx = MethodContext.of("This is a test sentence.", String.class, Remaining.class);
        Converter<String> converter = HalpbotUtils.context().get(ConverterHandler.class).from(ctx);

        Exceptional<String> sentence = converter.getMapper().apply(ctx);

        Assertions.assertTrue(sentence.present());
        Assertions.assertEquals("This is a test sentence.", sentence.get());
    }

    @Test
    public void parsingArrayTest() {
        MethodContext ctx = MethodContext.of("[5 1 3 12 20]", Integer[].class);
        Converter<Integer[]> converter = HalpbotUtils.context().get(ConverterHandler.class).from(ctx);

        Integer[] array = converter.getMapper().apply(ctx).get();

        Assertions.assertEquals(5, array[0]);
        Assertions.assertEquals(1, array[1]);
        Assertions.assertEquals(3, array[2]);
        Assertions.assertEquals(12, array[3]);
        Assertions.assertEquals(20, array[4]);
    }
}
