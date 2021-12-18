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

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.InvocationContextFactory;
import nz.pumbas.halpbot.commands.objects.Matrix;
import nz.pumbas.halpbot.commands.objects.Shape;
import nz.pumbas.halpbot.commands.objects.TestMessageEvent;
import nz.pumbas.halpbot.commands.objects.Vector3;
import nz.pumbas.halpbot.converters.DefaultConverters;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.PlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.events.MessageEvent;

//TODO: Test parameters like: List<List<String>>
@Service
@HartshornTest
@UseCommands
public class CommandContextTests
{
    @InjectTest
    public void commandContextParsedValuesPresentTest(CommandAdapter commandAdapter,
                                                      InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("containedWithinArrayTest");

        Assertions.assertNotNull(commandContext);
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("1 [2 3 4 1]")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2 [1 a 2]")).absent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("abc [1 3 2]")).absent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2 agf")).absent());
    }

    @InjectTest
    public void commandContextParsingTest(CommandAdapter commandAdapter,
                                          InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("containedWithinArrayTest");

        Assertions.assertNotNull(commandContext);

        Exceptional<Boolean> result1 = commandContext.invoke(invocationContextFactory.create("1 [2 1 4 3]"));
        Exceptional<Boolean> result2 = commandContext.invoke(invocationContextFactory.create("2 [9 5 4 3]"));

        Assertions.assertTrue(result1.present());
        Assertions.assertTrue(result2.present());
        Assertions.assertTrue(result1.get());
        Assertions.assertFalse(result2.get());
    }

    @InjectTest
    public void commandContextUsesDefaultValueIfNotPresent(CommandAdapter commandAdapter,
                                                           InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("containedWithinArrayTest");

        Assertions.assertNotNull(commandContext);

        Exceptional<Boolean> result = commandContext.invoke(invocationContextFactory.create("1"));

        Assertions.assertTrue(result.present());
        Assertions.assertFalse(result.get());
    }

    @InjectTest
    public void commandContextParsingAndValuesPresentTest(CommandAdapter commandAdapter,
                                                          InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("containedWithinArrayTest");

        Assertions.assertNotNull(commandContext);

        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2 [1 3 3]")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("3")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("")).absent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("alpha")).absent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2 [1 4 c]")).absent());

        Exceptional<Boolean> result = commandContext.invoke(invocationContextFactory.create("2 [1 2 3]"));
        Assertions.assertTrue(result.present());
        Assertions.assertTrue(result.get());

    }

    @Command(alias = "containedWithinArrayTest", description = "Returns if the item is within the specified elements")
    public boolean containedWithinArrayTestMethod(int num, @Unrequired("[]") int[] numbers) {
        for (int element : numbers) {
            if (num == element)
                return true;
        }
        return false;
    }

    @InjectTest
    public void customObjectParameterCommandTest(CommandAdapter commandAdapter,
                                                 InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("customObjectParameterTest");

        Assertions.assertNotNull(commandContext);
        Token token = commandContext.tokens(commandAdapter.applicationContext()).get(0);

        Assertions.assertTrue(token instanceof ParsingToken);
        Assertions.assertEquals(DefaultConverters.OBJECT_CONVERTER,((ParsingToken) token).converter());

        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("Vector3[1 2 3]")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("Vector3[3 1]")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("#Vector3[3 1]")).absent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("[3 1 2]")).absent());

        Exceptional<Double> result = commandContext.invoke(invocationContextFactory.create("Vector3[1 2 3]"));
        Assertions.assertTrue(result.present());
        Assertions.assertEquals(2, result.get());

    }

    @Command(alias = "customObjectParameterTest", description = "Tests if it successfully parses a custom object")
    public double customObjectParameterTestMethod(Vector3 vector3) {
        return vector3.getY();
    }

    @InjectTest
    public void commandContextImplicitArrayTokenTest(CommandAdapter commandAdapter,
                                                     InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("implicitArrayTest");

        Assertions.assertNotNull(commandContext);
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2 3 2 1 4 Heyo")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2 3 Hi")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2 [2 3 8] Hi")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("2 Hi")).absent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("a 1 2 Hi")).absent());

        Exceptional<Object> result = commandContext.invoke(invocationContextFactory.create("2 3 2 1 4 Heyo"));
        Assertions.assertTrue(result.present());
        Assertions.assertEquals("2 - [3, 2, 1, 4] - Heyo", result.get());
    }

    @Command(alias = "implicitArrayTest", description = "Tests the @Implicit attribute on arrays")
    public String implicitArrayTokenTestMethod(int num, @Implicit int[] array, String stop) {
        return "%s - %s - %s".formatted(num, Arrays.toString(array), stop);
    }

    @InjectTest
    public void commandContextImplicitArrayTokenAtEndTest(CommandAdapter commandAdapter,
                                                          InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("implicitArrayAtEndTest");

        Assertions.assertNotNull(commandContext);
        Exceptional<Double> result1 = commandContext.invoke(invocationContextFactory.create(""));
        Exceptional<Double> result2 = commandContext.invoke(invocationContextFactory.create(
                "Shape[Rectangle 200 50 100 25] Shape[Rectangle 50 200 25 150]"));
        Exceptional<Double> result3 = commandContext.invoke(invocationContextFactory.create(
                "Shape[Rectangle 200 50 100 25] Shape[Rectangle 50 200 25 150] Shape[Rectangle 200 50 100 275]"));

        Assertions.assertTrue(result3.present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create(
            "Shape[Rectangle 200 50 100 25]")).present());

        Assertions.assertTrue(result1.absent());
        Assertions.assertTrue(result2.present());
        Assertions.assertEquals(20000D, result2.get());
    }

    @Command(alias = "implicitArrayAtEndTest",
             description = "Tests the @Implicit attribute with no parameter after it")
    public double implicitArrayTokenAtEndTestMethod(@Implicit Shape[] shapes) {
        double totalArea = 0;
        for (Shape shape : shapes) {
            totalArea += shape.getArea();
        }

        return totalArea;
    }

    @InjectTest
    public void commandContextStringDefaultValueTest(CommandAdapter commandAdapter,
                                                     InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("stringDefaultValueTest");

        Assertions.assertNotNull(commandContext);
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("Hi")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("-1")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("a -1")).absent());

        Exceptional<String> result = commandContext.invoke(invocationContextFactory.create(""));
        Assertions.assertTrue(result.present());
        Assertions.assertEquals("default value", result.get());
    }

    @Command(alias = "stringDefaultValueTest")
    public String stringDefaultValueTestMethod(@Unrequired("default value") String string) {
        return string;
    }

    @InjectTest
    public void commandContextWithMessageReceivedEventParameterTest(CommandAdapter commandAdapter,
                                                                    InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("commandWithMessageReceivedEventParameterTest");
        InvocationContext invocationContext = invocationContextFactory.create(
                "", new MessageEvent(new TestMessageEvent()), Collections.emptySet());

        Assertions.assertNotNull(commandContext);
        Exceptional<Boolean> result1 = commandContext.invoke(invocationContextFactory.create(""));
        Exceptional<Boolean> result2 = commandContext.invoke(invocationContext);

        Assertions.assertEquals(1, commandContext.tokens(commandAdapter.applicationContext()).size());
        Assertions.assertTrue(result1.caught());
        Assertions.assertThrows(NullPointerException.class, result1::rethrow);
        Assertions.assertTrue(result2.present());
    }

    @Command(alias = "commandWithMessageReceivedEventParameterTest")
    public boolean commandWithMessageReceivedEventParameterTestMethod(MessageReceivedEvent event) {
        return true;
    }

    @InjectTest
    public void commandContextWithMultipleAnnotationsTest(CommandAdapter commandAdapter,
                                                          InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("commandWithMultipleAnnotationsTest");

        Assertions.assertNotNull(commandContext);
        List<Token> tokens = commandContext.tokens(commandAdapter.applicationContext());

        Assertions.assertEquals(1, tokens.size());
        Assertions.assertTrue(tokens.get(0) instanceof ParsingToken);
        Assertions.assertEquals(1, ((ParsingToken) tokens.get(0)).sortedAnnotations().size());

        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("1 2 3")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("[1 2 3]")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("1.0 2 3")).absent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("[1 2 3")).absent());
    }

    @Command(alias = "commandWithMultipleAnnotationsTest")
    public int commandWithMultipleAnnotationsTestMethod(@Unrequired("[]") @Implicit int[] array) {
        return -1;
    }

    @InjectTest
    public void commandContextWithVarargsTest(CommandAdapter commandAdapter,
                                              InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("commandWithVarargsTest");

        Assertions.assertNotNull(commandContext);
        List<Token> tokens = commandContext.tokens(commandAdapter.applicationContext());

        Assertions.assertEquals(1,tokens.size());
        Assertions.assertEquals(DefaultConverters.ARRAY_CONVERTER, ((ParsingToken) tokens.get(0)).converter());

        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("[1 2 3]")).errorAbsent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("")).caught());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("1 2 3")).caught());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("1.0 2 3")).caught());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("[1 2 3")).caught());
    }

    @Command(alias = "commandWithVarargsTest")
    public void commandWithVarargsTestMethod(int... values) {
    }

    @InjectTest
    public void commandContextMultipleAliasesTest(CommandAdapter commandAdapter,
                                                  InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext1 = commandAdapter.commandContext("commandWithMultipleAliases1");
        CommandContext commandContext2 = commandAdapter.commandContext("commandWithMultipleAliases2");

        Assertions.assertNotNull(commandContext1);
        Assertions.assertNotNull(commandContext2);
        Assertions.assertEquals(commandContext1, commandContext2);
        Assertions.assertTrue(commandContext1.invoke(invocationContextFactory.create("")).errorAbsent());
        Assertions.assertTrue(commandContext1.invoke(invocationContextFactory.create("1")).caught());
    }

    @Command(alias = {"commandWithMultipleAliases1", "CommandWithMultipleAliases2"})
    public void commandWithMultipleAliasesTestMethod() {
    }

    @InjectTest
    public void commandContextWithAListTest(CommandAdapter commandAdapter,
                                            InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("commandWithAListTest");

        Assertions.assertNotNull(commandContext);
        Exceptional<Integer> result1 = commandContext.invoke(invocationContextFactory.create("[]"));
        Exceptional<Integer> result2 = commandContext.invoke(invocationContextFactory.create("[1 5 3 4]"));

        Assertions.assertTrue(result1.present());
        Assertions.assertTrue(result2.present());
        Assertions.assertEquals(0, result1.get());
        Assertions.assertEquals(13, result2.get());
    }

    @Command(alias = "commandWithAListTest")
    public int commandWithAListTestMethod(List<Integer> values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum;
    }

    @InjectTest
    public void commandContextWithPlaceholderTest(CommandAdapter commandAdapter,
                                                  InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("commandWithOptionalPlaceholderTest");

        Assertions.assertNotNull(commandContext);

        List<Token> tokens = commandContext.tokens(commandAdapter.applicationContext());
        Assertions.assertEquals(2, tokens.size());
        Assertions.assertInstanceOf(PlaceholderToken.class, tokens.get(0));
        Assertions.assertEquals("My name is", ((PlaceholderToken) tokens.get(0)).placeholder());
        Assertions.assertInstanceOf(ParsingToken.class, tokens.get(1));

        Exceptional<String> result1 = commandContext.invoke(invocationContextFactory.create("pumbas600"));
        Exceptional<String> result2 = commandContext.invoke(invocationContextFactory.create("My name is pumbas600"));;

        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("my name is pumbas600")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("My NaMe IS pumbas600")).present());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("My pumbas600")).absent());
        Assertions.assertTrue(commandContext.invoke(invocationContextFactory.create("NaMe IS pumbas600")).absent());

        Assertions.assertTrue(result1.present());
        Assertions.assertTrue(result2.present());
        Assertions.assertEquals("pumbas600", result1.get());
        Assertions.assertEquals("pumbas600", result2.get());
    }

    @Command(alias = "commandWithOptionalPlaceholderTest",
             command = "[My name is] String")
    public String commandWithOptionalPlaceholderTestMethod(String name) {
        return name;
    }

//    @Test
//    public void commandStringWithMultipleAnnotationsTest() {
//        SimpleCommand command = CommandManager.generateCommandMethod(this,
//            Reflect.getMethod(this, "commandStringWithMultipleAnnotationsMethodTest"));
//
//        Assertions.assertEquals(4, command.getCommandTokens().size());
//        Assertions.assertTrue(command.parse(MethodContext.of("2 x 3")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("2 x 2 [1 0 0 1]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("2 2 [1 0 0 1]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("2 x 2 1 0 0 1")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("2 2 1 0 0 1")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("2 2 x 1 0 0 1")).absent());
//        Assertions.assertTrue(command.parse(MethodContext.of("2 2 1.2")).absent());
//        Assertions.assertTrue(command.parse(MethodContext.of("2 2 [1 2 0.0 3]")).absent());
//    }
//
//    @Command(alias = "test", command = "#Integer <x> #Integer #Integer[]")
//    private int commandStringWithMultipleAnnotationsMethodTest(int rows, int columns, @Unrequired(
//        "[]") @Implicit int... values) {
//        return rows;
//    }
//
//    @Test
//    public void commandWithComplexCustomParameterMatchesTest() {
//        SimpleCommand command = CommandManager.generateCommandMethod(this,
//            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));
//
//        Exceptional<Object> result1 = command.parse(MethodContext.of("Matrix[2 2 x 1 0 0 1]"));
//        Exceptional<Object> result2 = command.parse(MethodContext.of("Matr[2 2 [1 2 0.0 3]]"));
//        Exceptional<Object> result3 = command.parse(MethodContext.of("Matrix[2 2 [1 2 1 3]"));
//
//        Assertions.assertEquals(1, command.getCommandTokens().size());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[2 x 3]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[2 x 2 [1 0 0 1]]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[2 2 [1 0 0 1]]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[2 x 2 1 0 0 1]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[2 2 1 0 0 1]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[2 2 1.2]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[2 2 [1 2 0.0 3]]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[2 x 3 [1 2 3 4 5 6]]")).present());
//
//        Assertions.assertFalse(result1.present());
//        Assertions.assertEquals("There seems to have been an error when constructing the Matrix",
//            result1.error().getMessage());
//
//        Assertions.assertFalse(result2.present());
//        Assertions.assertEquals("Expected the alias Matrix but got Matr",
//            result2.error().getMessage());
//
//        Assertions.assertFalse(result3.present());
//        Assertions.assertEquals("There seems to have been an error when constructing the Matrix",
//            result3.error().getMessage());
//    }

    @InjectTest
    public void commandContentWithComplexParameterInvocationTest(CommandAdapter commandAdapter,
                                                                 InvocationContextFactory invocationContextFactory)
    {
        CommandContext commandContext = commandAdapter.commandContext("commandWithComplexParameterTest");

        Assertions.assertNotNull(commandContext);
        Exceptional<Integer> result = commandContext.invoke(invocationContextFactory.create("Matrix[2 3 [1 2 3 4 5 6]]"));

        Assertions.assertTrue(result.present());
        Assertions.assertEquals(3, result.get());
    }

//    @Test
//    public void commandWith2DArrayTest() {
//        SimpleCommand command = CommandManager.generateCommandMethod(this,
//            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));
//
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[[1 0 0 1]]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[[1 0] [0 1]]")).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix[[0 1] 0 1 2]")).absent());
//    }
//
//    @Test
//    public void commandMethodMatchesTest() {
//        SimpleCommand command = CommandManager.generateCommandMethod(this,
//            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));
//
//        Exceptional<Object> result = command.parse(MethodContext.of("Matrix.yShear[4", command));
//
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix.scale[2]", command)).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix.roTaTe[45]", command)).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix.xShear[2]", command)).present());
//
//        Assertions.assertFalse(result.present());
//    }
//
//    @Test
//    public void commandFieldMatchesTest() {
//        SimpleCommand command = CommandManager.generateCommandMethod(this,
//            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));
//
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix.UnitSquare", command)).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix.uNiTsquAre", command)).present());
//        Assertions.assertTrue(command.parse(MethodContext.of("Matrix.unitSquare[]", command)).absent());
//    }

    @Command(alias = "commandWithComplexParameterTest", reflections = Matrix.class)
    public int commandWithComplexCustomParameterTestMethod(Matrix matrix) {
        return matrix.getColumns();
    }
}
