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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Invite.Channel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.InvocationContextFactory;
import nz.pumbas.halpbot.commands.objects.ShapeType;
import nz.pumbas.halpbot.commands.objects.Vector3;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.SourceConverter;
import nz.pumbas.halpbot.converters.annotations.parameter.Children;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.converters.annotations.parameter.Unmodifiable;
import nz.pumbas.halpbot.converters.ParameterConverter;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.converters.DefaultConverters;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.utilities.Reflect;

@UseCommands
@HartshornTest
public class ConverterHandlerTests
{
    @InjectTest
    public void retrievingArrayConverterTest(ConverterHandler converterHandler) {
        Converter<?, Object[]> converter = converterHandler.from(Object[].class);

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.ARRAY_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingMessageReceivedEventConverterTest(ConverterHandler converterHandler) {
        Converter<?, MessageReceivedEvent> converter = converterHandler.from(MessageReceivedEvent.class);

        Assertions.assertInstanceOf(SourceConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.EVENT_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingObjectConverterForUnknownTypesTest(ConverterHandler converterHandler) {
        Converter<?, Vector3> converter = converterHandler.from(Vector3.class);

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.OBJECT_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingIntegerConverterTest(ConverterHandler converterHandler) {
        Converter<?, Integer> converter = converterHandler.from(Integer.class);

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.INTEGER_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingListConverterTest(ConverterHandler converterHandler) {
        Converter<?, List> converter = converterHandler.from(List.class);

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.LIST_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingSetConverterTest(ConverterHandler converterHandler) {
        Converter<?, Set> converter = converterHandler.from(Set.class);

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.SET_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingUnmodifiableListConverterTest(ConverterHandler converterHandler) {
        Converter<?, List> converter = converterHandler.from(TypeContext.of(List.class), TypeContext.of(Unmodifiable.class));

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.UNMODIFIABLE_LIST_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingEnumConverterTest(ConverterHandler converterHandler) {
        Converter<?, ShapeType> converter = converterHandler.from(ShapeType.class);

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.ENUM_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingRemainingStringsConverterTest(ConverterHandler converterHandler) {
        Converter<?, String> converter = converterHandler
                .from(TypeContext.of(String.class), TypeContext.of(Remaining.class));

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.REMAINING_STRINGS_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingImplicitListConverterTest(ConverterHandler converterHandler) {
        Converter<?, int[]> converter = converterHandler
                .from(TypeContext.of(int[].class), TypeContext.of(Implicit.class));

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.ARRAY_CONVERTER, converter);
    }

    @InjectTest
    public void retrievingChildrenConverterTest(ConverterHandler converterHandler) {
        Converter<?, List> converter = converterHandler
                .from(TypeContext.of(List.class), TypeContext.of(Children.class));

        Assertions.assertInstanceOf(ParameterConverter.class, converter);
        Assertions.assertEquals(DefaultConverters.CHILDREN_TYPE_CONVERTER, converter);
    }

    @InjectTest
    public void parsingRemainingStringsTest(InvocationContextFactory invocationContextFactory, ConverterHandler converterHandler) {
        CommandInvocationContext invocationContext = invocationContextFactory.create("This is a test sentence.");

        Converter<CommandInvocationContext, String> converter = converterHandler
                .from(TypeContext.of(String.class), TypeContext.of(Remaining.class));

        Exceptional<String> sentence = converter.apply(invocationContext);

        Assertions.assertTrue(sentence.present());
        Assertions.assertEquals("This is a test sentence.", sentence.get());
    }

    @InjectTest
    public void parsingArrayTest(InvocationContextFactory invocationContextFactory, ConverterHandler converterHandler) {
        CommandInvocationContext invocationContext = invocationContextFactory.create("[5 1 3 12 20]");
        invocationContext.currentType(TypeContext.of(Integer[].class));
        Converter<CommandInvocationContext, Integer[]> converter = converterHandler.from(Integer[].class);

        Integer[] array = converter.apply(invocationContext).get();

        Assertions.assertEquals(5,  array[0]);
        Assertions.assertEquals(1,  array[1]);
        Assertions.assertEquals(3,  array[2]);
        Assertions.assertEquals(12, array[3]);
        Assertions.assertEquals(20, array[4]);
    }

    @InjectTest
    public void isCommandParameterTests(ConverterHandler converterHandler) {
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(int.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(byte.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(double.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(long.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(float.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(boolean.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(short.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(String.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(Vector3.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(int[].class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(boolean[].class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(float[].class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(double[].class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(List.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(Set.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(User.class)));
        Assertions.assertTrue(converterHandler.isCommandParameter(TypeContext.of(MessageChannel.class)));
    }

    @InjectTest
    public void isNotCommandParameterTests(ConverterHandler converterHandler) {
        Assertions.assertFalse(converterHandler.isCommandParameter(TypeContext.of(JDA.class)));
        Assertions.assertFalse(converterHandler.isCommandParameter(TypeContext.of(GenericEvent.class)));
        Assertions.assertFalse(converterHandler.isCommandParameter(TypeContext.of(MessageReceivedEvent.class)));
        Assertions.assertFalse(converterHandler.isCommandParameter(TypeContext.of(Interaction.class)));
        Assertions.assertFalse(converterHandler.isCommandParameter(TypeContext.of(HalpbotAdapter.class)));
        Assertions.assertFalse(converterHandler.isCommandParameter(TypeContext.of(HalpbotCore.class)));
        Assertions.assertFalse(converterHandler.isCommandParameter(TypeContext.of(ApplicationContext.class)));
    }

    @InjectTest
    public void isNotCommandParameterWhenAnnotatedWithSourceTests(ConverterHandler converterHandler) {
        Assertions.assertFalse(converterHandler.isCommandParameter(
                TypeContext.of(User.class), Set.of(TypeContext.of(Source.class))));
        Assertions.assertFalse(converterHandler.isCommandParameter(
                TypeContext.of(Member.class), Set.of(TypeContext.of(Source.class))));
        Assertions.assertFalse(converterHandler.isCommandParameter(
                TypeContext.of(MessageChannel.class), Set.of(TypeContext.of(Source.class))));
        Assertions.assertFalse(converterHandler.isCommandParameter(
                TypeContext.of(Channel.class), Set.of(TypeContext.of(Source.class))));
        Assertions.assertFalse(converterHandler.isCommandParameter(
                TypeContext.of(VoiceChannel.class), Set.of(TypeContext.of(Source.class))));
    }

    @InjectTest
    public void isCommandParameterWhenAnnotated(ConverterHandler converterHandler) {
        Assertions.assertTrue(converterHandler.isCommandParameter(
                TypeContext.of(int.class), Set.of(TypeContext.of(Unrequired.class))));
        Assertions.assertTrue(converterHandler.isCommandParameter(
                TypeContext.of(User.class), Set.of(TypeContext.of(Unrequired.class))));
        Assertions.assertTrue(converterHandler.isCommandParameter(
                TypeContext.of(String.class), Set.of(TypeContext.of(Remaining.class))));
        Assertions.assertTrue(converterHandler.isCommandParameter(
                TypeContext.of(String[].class), Set.of(TypeContext.of(Implicit.class))));
        Assertions.assertTrue(converterHandler.isCommandParameter(
                TypeContext.of(List.class), Set.of(TypeContext.of(Implicit.class))));
    }
}
