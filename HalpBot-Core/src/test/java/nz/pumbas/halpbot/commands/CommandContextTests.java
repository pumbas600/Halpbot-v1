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

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.InvocationContextFactory;
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
@UseCommands
@HartshornTest
public class CommandContextTests
{
    @Inject CommandAdapter commandAdapter;
    @Inject InvocationContextFactory invocationFactory;

    @Test
    public void commandContextParsedValuesPresentTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("containedWithinArrayTest");

        Assertions.assertNotNull(commandContext);
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1 [2 3 4 1]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 [1 a 2]")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("abc [1 3 2]")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 agf")).absent());
    }

    @Test
    public void commandContextParsingTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("containedWithinArrayTest");

        Assertions.assertNotNull(commandContext);

        Exceptional<Boolean> result1 = commandContext.invoke(this.invocationFactory.create("1 [2 1 4 3]"));
        Exceptional<Boolean> result2 = commandContext.invoke(this.invocationFactory.create("2 [9 5 4 3]"));

        Assertions.assertTrue(result1.present());
        Assertions.assertTrue(result2.present());
        Assertions.assertTrue(result1.get());
        Assertions.assertFalse(result2.get());
    }

    @Test
    public void commandContextUsesDefaultValueIfNotPresent() {
        CommandContext commandContext = this.commandAdapter.commandContext("containedWithinArrayTest");

        Assertions.assertNotNull(commandContext);

        Exceptional<Boolean> result = commandContext.invoke(this.invocationFactory.create("1"));

        Assertions.assertTrue(result.present());
        Assertions.assertFalse(result.get());
    }

    @Test
    public void commandContextParsingAndValuesPresentTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("containedWithinArrayTest");

        Assertions.assertNotNull(commandContext);

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 [1 3 3]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("3")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("alpha")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 [1 4 c]")).absent());

        Exceptional<Boolean> result = commandContext.invoke(this.invocationFactory.create("2 [1 2 3]"));
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

    @Test
    public void commandContextCustomObjectParameterCommandTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("customObjectParameterTest");

        Assertions.assertNotNull(commandContext);
        Token token = commandContext.tokens().get(0);

        Assertions.assertTrue(token instanceof ParsingToken);
        Assertions.assertEquals(DefaultConverters.OBJECT_CONVERTER,((ParsingToken) token).converter());

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Vector3(1 2 3)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Vector3(3 1)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("#Vector3(3 1)")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("(3 1 2)")).absent());

        Exceptional<Double> result = commandContext.invoke(this.invocationFactory.create("Vector3(1 2 3)"));
        Assertions.assertTrue(result.present());
        Assertions.assertEquals(2, result.get());

    }

    @Command(alias = "customObjectParameterTest", description = "Tests if it successfully parses a custom object")
    public double customObjectParameterTestMethod(Vector3 vector3) {
        return vector3.getY();
    }

    @Test
    public void commandContextImplicitArrayTokenTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("implicitArrayTest");

        Assertions.assertNotNull(commandContext);
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 3 2 1 4 Heyo")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 3 Hi")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 [2 3 8] Hi")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 Hi")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("a 1 2 Hi")).absent());

        Exceptional<Object> result = commandContext.invoke(this.invocationFactory.create("2 3 2 1 4 Heyo"));
        Assertions.assertTrue(result.present());
        Assertions.assertEquals("2 - [3, 2, 1, 4] - Heyo", result.get());
    }

    @Command(alias = "implicitArrayTest", description = "Tests the @Implicit attribute on arrays")
    public String implicitArrayTokenTestMethod(int num, @Implicit int[] array, String stop) {
        return "%s - %s - %s".formatted(num, Arrays.toString(array), stop);
    }

    @Test
    public void commandContextImplicitArrayTokenAtEndTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("implicitArrayAtEndTest");

        Assertions.assertNotNull(commandContext);
        Exceptional<Double> result1 = commandContext.invoke(this.invocationFactory.create(""));
        Exceptional<Double> result2 = commandContext.invoke(this.invocationFactory.create(
                "Shape(Rectangle 200 50 100 25) Shape(Rectangle 50 200 25 150)"));
        Exceptional<Double> result3 = commandContext.invoke(this.invocationFactory.create(
                "Shape(Rectangle 200 50 100 25) Shape(Rectangle 50 200 25 150) Shape(Rectangle 200 50 100 275)"));

        Assertions.assertTrue(result3.present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create(
            "Shape(Rectangle 200 50 100 25)")).present());

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

    @Test
    public void commandContextStringDefaultValueTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("stringDefaultValueTest");

        Assertions.assertNotNull(commandContext);
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Hi")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("-1")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("a -1")).absent());

        Exceptional<String> result = commandContext.invoke(this.invocationFactory.create(""));
        Assertions.assertTrue(result.present());
        Assertions.assertEquals("default value", result.get());
    }

    @Command(alias = "stringDefaultValueTest")
    public String stringDefaultValueTestMethod(@Unrequired("default value") String string) {
        return string;
    }

    @Test
    public void commandContextWithMessageReceivedEventParameterTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithMessageReceivedEventParameterTest");
        CommandInvocationContext invocationContext = this.invocationFactory.create(
                "", new MessageEvent(new TestMessageEvent()));

        Assertions.assertNotNull(commandContext);
        Exceptional<Boolean> result1 = commandContext.invoke(this.invocationFactory.create(""));
        Exceptional<Boolean> result2 = commandContext.invoke(invocationContext);

        Assertions.assertEquals(1, commandContext.tokens().size());
        Assertions.assertTrue(result1.caught());
        Assertions.assertThrows(NullPointerException.class, result1::rethrow);
        Assertions.assertTrue(result2.present());
    }

    @Command(alias = "commandWithMessageReceivedEventParameterTest")
    public boolean commandWithMessageReceivedEventParameterTestMethod(MessageReceivedEvent event) {
        return true;
    }

    @Test
    public void commandContextWithMultipleAnnotationsTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithMultipleAnnotationsTest");

        Assertions.assertNotNull(commandContext);
        List<Token> tokens = commandContext.tokens();

        Assertions.assertEquals(1, tokens.size());
        Assertions.assertTrue(tokens.get(0) instanceof ParsingToken);
        Assertions.assertEquals(1, ((ParsingToken) tokens.get(0)).sortedAnnotations().size());

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1 2 3")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("[1 2 3]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1.0 2 3")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("[1 2 3")).absent());
    }

    @Command(alias = "commandWithMultipleAnnotationsTest")
    public int commandWithMultipleAnnotationsTestMethod(@Unrequired("[]") @Implicit int[] array) {
        return -1;
    }

    @Test
    public void commandContextWithVarargsTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithVarargsTest");

        Assertions.assertNotNull(commandContext);
        List<Token> tokens = commandContext.tokens();

        Assertions.assertEquals(1,tokens.size());
        Assertions.assertEquals(DefaultConverters.ARRAY_CONVERTER, ((ParsingToken) tokens.get(0)).converter());

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("[1 2 3]")).errorAbsent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("")).caught());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1 2 3")).caught());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1.0 2 3")).caught());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("[1 2 3")).caught());
    }

    @Command(alias = "commandWithVarargsTest")
    public void commandWithVarargsTestMethod(int... values) {
    }

    @Test
    public void commandContextMultipleAliasesTest() {
        CommandContext commandContext1 = this.commandAdapter.commandContext("commandWithMultipleAliases1");
        CommandContext commandContext2 = this.commandAdapter.commandContext("commandWithMultipleAliases2");

        Assertions.assertNotNull(commandContext1);
        Assertions.assertNotNull(commandContext2);
        Assertions.assertEquals(commandContext1, commandContext2);
        Assertions.assertTrue(commandContext1.invoke(this.invocationFactory.create("")).errorAbsent());
        Assertions.assertTrue(commandContext1.invoke(this.invocationFactory.create("1")).caught());
    }

    @Command(alias = {"commandWithMultipleAliases1", "CommandWithMultipleAliases2"})
    public void commandWithMultipleAliasesTestMethod() {
    }

    @Test
    public void commandContextWithAListTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithAListTest");

        Assertions.assertNotNull(commandContext);
        Exceptional<Integer> result1 = commandContext.invoke(this.invocationFactory.create("[]"));
        Exceptional<Integer> result2 = commandContext.invoke(this.invocationFactory.create("[1 5 3 4]"));

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

    @Test
    public void commandContextWithOptionalPlaceholderTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithOptionalPlaceholderTest");

        Assertions.assertNotNull(commandContext);

        List<Token> tokens = commandContext.tokens();
        Assertions.assertEquals(2, tokens.size());
        Assertions.assertInstanceOf(PlaceholderToken.class, tokens.get(0));
        Assertions.assertEquals("My name is", ((PlaceholderToken) tokens.get(0)).placeholder());
        Assertions.assertInstanceOf(ParsingToken.class, tokens.get(1));

        Exceptional<String> result1 = commandContext.invoke(this.invocationFactory.create("pumbas600"));
        Exceptional<String> result2 = commandContext.invoke(this.invocationFactory.create("My name is pumbas600"));

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("my name is pumbas600")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("My NaMe IS pumbas600")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("My pumbas600")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("NaMe IS pumbas600")).absent());

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

    @Test
    public void commandContextWithMultiplePlaceholdersTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithMultiplePlaceholdersTest");

        Assertions.assertNotNull(commandContext);
        List<Token> tokens = commandContext.tokens();

        Assertions.assertEquals(4, tokens.size());
        Assertions.assertInstanceOf(PlaceholderToken.class, tokens.get(0));
        Assertions.assertEquals("Hi", ((PlaceholderToken) tokens.get(0)).placeholder());
        Assertions.assertInstanceOf(PlaceholderToken.class, tokens.get(1));
        Assertions.assertEquals(",", ((PlaceholderToken) tokens.get(1)).placeholder());
        Assertions.assertInstanceOf(PlaceholderToken.class, tokens.get(2));
        Assertions.assertEquals("my name is", ((PlaceholderToken) tokens.get(2)).placeholder());
        Assertions.assertInstanceOf(ParsingToken.class, tokens.get(3));

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Hi, my name is pumbas600")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Hi my NaMe IS pumbas600")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("hi my name is pumbas600")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("My NaMe is")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create(", My pumbas600")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("hi, NaMe IS pumbas600")).absent());
    }

    @Command(alias = "commandWithMultiplePlaceholdersTest",
             command = "[Hi] [,] <my name is> String")
    public String commandWithMultiplePlaceholdersTestMethod(String name) {
        return name;
    }

    @Test
    public void commandContextWithPlaceholdersAndPrimativeTypesTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithPlaceholdersAndPrimativeTypesTest");

        Assertions.assertNotNull(commandContext);
        List<Token> tokens = commandContext.tokens();

        Assertions.assertEquals(6, tokens.size());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1 2 [1 2 3 4]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("add 1 2 [1 2 3 4]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("add 1 and 2 [1 2 3 4]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("add 1 and 2 and [1 2 3 4]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1 and 2 and [1 2 3 4]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1 2 1 2 3 4")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("hi 1 2 [1 2 3 4]")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("1 2 add [1 2 3 4]")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("add 1 and 2 not [1 2 3 4]")).absent());
    }

    @Command(alias = "commandWithPlaceholdersAndPrimativeTypesTest",
             command = "[add] Integer [and] Byte [and] Integer[]")
    public int commandWithPlaceholdersAndPrimativeTypesTestMethod(int a, byte b, int[] nums) {
        int sum = a + b;
        for (int num : nums) {
            sum += num;
        }
        return sum;
    }

    @Test
    public void commandContextCommandStringWithMultipleAnnotationsTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandStringWithMultipleAnnotationsTest");

        Assertions.assertNotNull(commandContext);
        List<Token> tokens = commandContext.tokens();

        Assertions.assertEquals(4, tokens.size());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 x 3")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 x 2 [1 0 0 1]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 2 [1 0 0 1]")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 x 2 1 0 0 1")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 2 1 0 0 1")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 2 x 1 0 0 1")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 2 1.2")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("2 2 [1 2 0.0 3]")).absent());
    }

    @Command(alias = "commandStringWithMultipleAnnotationsTest", command = "Integer [x] Integer Integer[]")
    public int commandStringWithMultipleAnnotationsTestMethod(int rows, int columns,
                                                              @Unrequired("[]") @Implicit int... values) {
        return rows;
    }

    @Test
    public void commandContextWithComplexCustomParameterMatchesTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithComplexParameterTest");

        Assertions.assertNotNull(commandContext);
        Exceptional<Object> result1 = commandContext.invoke(this.invocationFactory.create("Matrix(2 2 x 1 0 0 1)"));
        Exceptional<Object> result2 = commandContext.invoke(this.invocationFactory.create("Matr(2 2 [1 2 0.0 3])"));
        Exceptional<Object> result3 = commandContext.invoke(this.invocationFactory.create("Matrix(2 2 [1 2 1 3]"));

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix(2 x 3)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix(2 x 2 [1 0 0 1])")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix(2 2 [1 0 0 1])")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix(2 x 2 1 0 0 1)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix(2 2 1 0 0 1)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix(2 2 1.2)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix(2 2 [1 2 0.0 3])")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix(2 x 3 [1 2 3 4 5 6])")).present());

        Assertions.assertFalse(result1.present());
        Assertions.assertEquals("There seems to have been an error when constructing the object Matrix",
            result1.error().getMessage());

        Assertions.assertFalse(result2.present());
        Assertions.assertEquals("Expected the alias 'Matrix' but got 'Matr'",
            result2.error().getMessage());

        Assertions.assertFalse(result3.present());
        Assertions.assertEquals("There seems to have been an error when constructing the object Matrix",
            result3.error().getMessage());
    }

    @Test
    public void commandContentWithComplexParameterInvocationTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithComplexParameterTest");

        Assertions.assertNotNull(commandContext);
        Exceptional<Integer> result = commandContext.invoke(this.invocationFactory.create("Matrix(2 3 [1 2 3 4 5 6])"));

        Assertions.assertTrue(result.present());
        Assertions.assertEquals(3, result.get());
    }

    @Test
    public void commandContextWith2DArrayTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithComplexParameterTest");

        Assertions.assertNotNull(commandContext);
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix()")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix([1 0 0 1])")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix([1 0] [0 1])")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix([0 1] 0 1 2)")).absent());
    }

    @Test
    public void commandContextWithReflectiveMethodsTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithComplexParameterTest");

        Assertions.assertNotNull(commandContext);

        Exceptional<Integer> result1 = commandContext.invoke(this.invocationFactory.create("yShear(4"));
        Exceptional<Integer> result2 = commandContext.invoke(this.invocationFactory.create("scale(3)"));

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("scale(2)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("roTaTe(45)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("xShear(2)")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("Matrix.xShear(2)")).absent());

        Assertions.assertFalse(result1.present());
        Assertions.assertTrue(result2.present());
        Assertions.assertEquals(2, result2.get());
    }

    @Test
    public void commandContextWithReflectiveMethodAliasesTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithComplexParameterTest");

        Assertions.assertNotNull(commandContext);

        Exceptional<Integer> result1 = commandContext.invoke(this.invocationFactory.create("unitSquare()"));
        Exceptional<Integer> result2 = commandContext.invoke(this.invocationFactory.create("us()"));

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("mirrorX()")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("xReflection()")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("yReflection()")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("mirrorY()")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("yReflection(fd)")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("yReflection)")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("unitSquare(23)")).absent());

        Assertions.assertTrue(result1.present());
        Assertions.assertEquals(4, result1.get());
        Assertions.assertTrue(result2.present());
        Assertions.assertEquals(4, result2.get());
    }

    @Command(alias = "commandWithComplexParameterTest", reflections = Matrix.class)
    public int commandWithComplexCustomParameterTestMethod(Matrix matrix) {
        return matrix.getColumns();
    }

    @Test
    public void commandContextWithReflectiveListTest() {
        CommandContext commandContext = this.commandAdapter.commandContext("commandWithListOfCustomObjectTest");

        Assertions.assertNotNull(commandContext);

        Exceptional<Integer> result1 = commandContext.invoke(this.invocationFactory.create("mirrorX() us()"));
        Exceptional<Integer> result2 = commandContext.invoke(this.invocationFactory.create("Matrix([1 0 0 1]) us()"));

        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("mirrorX() scale(2) us()")).present());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("reflection()")).absent());
        Assertions.assertTrue(commandContext.invoke(this.invocationFactory.create("yReflection(")).absent());

        Assertions.assertTrue(result1.present());
        Assertions.assertEquals(2, result1.get());
        Assertions.assertTrue(result2.present());
        Assertions.assertEquals(2, result2.get());
    }

    @Command(alias = "commandWithListOfCustomObjectTest", reflections = Matrix.class)
    public int commandWithListOfCustomObjectTestMethod(@Implicit List<Matrix> matrices) {
        return matrices.size();
    }
}
