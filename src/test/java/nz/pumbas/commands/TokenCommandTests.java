package nz.pumbas.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationContext;
import nz.pumbas.commands.tokens.tokentypes.ArrayToken;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.halpbot.customparameters.Matrix;
import nz.pumbas.halpbot.customparameters.Shape;
import nz.pumbas.halpbot.customparameters.Vector3;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Language;
import nz.pumbas.utilities.Reflect;

public class TokenCommandTests
{
    @Test
    public void tokenCommandMatchesTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Assertions.assertTrue(tokenCommand.matches(InvocationContext.of("1 [2 3 4 1]")));
        Assertions.assertTrue(tokenCommand.matches(InvocationContext.of("2")));
        Assertions.assertFalse(tokenCommand.matches(InvocationContext.of("2 [1 a 2]")));
        Assertions.assertFalse(tokenCommand.matches(InvocationContext.of("abc [1 3 2]")));
        Assertions.assertFalse(tokenCommand.matches(InvocationContext.of("2 agf")));
    }

    @Test
    public void simpleTokenCommandInvokeTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Optional<Object> result1 = tokenCommand.invoke(InvocationContext.of("1 [2 1 4 3]"),
            null, null);
        Optional<Object> result2 = tokenCommand.invoke(InvocationContext.of("2 [9 5 4 3]"),
            null, null);

        Assertions.assertTrue(result1.isPresent());
        Assertions.assertTrue(result2.isPresent());
        Assertions.assertTrue((boolean) result1.get());
        Assertions.assertFalse((boolean) result2.get());
    }

    @Test
    public void testDefaultValueForLastElementTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Optional<Object> result1 = tokenCommand.invoke(InvocationContext.of("1"),
            null, null);

        Assertions.assertTrue(result1.isPresent());
        Assertions.assertFalse((boolean) result1.get());
    }

    @Command(alias = "contained", description = "Returns if the item is within the specified elements")
    private boolean containedWithinArrayTestMethod(int num, @Unrequired("[]") int[] numbers) {
        for (int element : numbers) {
            if (num == element)
                return true;
        }
        return false;
    }

    @Test
    public void tokenCommandTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Assertions.assertTrue(command.matches(InvocationContext.of("2 [1 3 3]")));
        Assertions.assertTrue(command.matches(InvocationContext.of("3")));
        Assertions.assertFalse(command.matches(InvocationContext.of("")));
        Assertions.assertFalse(command.matches(InvocationContext.of("alpha")));
        Assertions.assertFalse(command.matches(InvocationContext.of("2 [1 4 c]")));

        Optional<Object> result = command.invoke(InvocationContext.of("2 [1 2 3]"),
            null, null);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue((boolean) result.get());

    }

    @Test
    public void customObjectTokenCommandTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "customObjectTokenCommandMethodTest"));

        Assertions.assertTrue(command.matches(InvocationContext.of("#Vector3[1 2 3]")));
        Assertions.assertTrue(command.matches(InvocationContext.of("#Vector3[3 1]")));
        Assertions.assertFalse(command.matches(InvocationContext.of("Vector3[3 1]")));
        Assertions.assertFalse(command.matches(InvocationContext.of("[3 1 2]")));

        Optional<Object> result = command.invoke(InvocationContext.of("#Vector3[1 2 3]"),
            null, null);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(2, (double) result.get());

    }

    @Command(alias = "CustomObject", description = "Tests if it successfully parses a custom object")
    private double customObjectTokenCommandMethodTest(Vector3 vector3) {
        return vector3.getY();
    }

    @Test
    public void implicitArrayTokenTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this,"implicitArrayTokenMethodTest"));

        Assertions.assertTrue(command.matches(InvocationContext.of("2 3 2 1 4 Heyo")));
        Assertions.assertTrue(command.matches(InvocationContext.of("2 3 Hi")));
        Assertions.assertTrue(command.matches(InvocationContext.of("2 [2 3 8] Hi")));
        Assertions.assertFalse(command.matches(InvocationContext.of("2 Hi")));
        Assertions.assertFalse(command.matches(InvocationContext.of("a 1 2 Hi")));

        Optional<Object> result = command.invoke(InvocationContext.of("2 3 2 1 4 Heyo"),
            null, null);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("2: [3, 2, 1, 4]: Heyo", result.get());
    }


    @Command(alias = "ImplicitArray", description = "Tests the @Implicit attribute on arrays")
    private String implicitArrayTokenMethodTest(int num, @Implicit int[] array, String stop) {
        return String.format("%s: %s: %s", num, Arrays.toString(array), stop);
    }

    @Test
    public void implicitArrayTokenAtEndTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this,"implicitArrayTokenAtEndMethodTest"));

        Assertions.assertTrue(command.matches(InvocationContext.of(
            "#Shape[Rectangle 200 50 100 25] #Shape[Rectangle 50 200 25 150] #Shape[Rectangle 200 50 100 275]")));
        Assertions.assertTrue(command.matches(InvocationContext.of(
            "#Shape[Rectangle 200 50 100 25]")));
        Assertions.assertFalse(command.matches(InvocationContext.of("")));

        Optional<Object> result = command.invoke(InvocationContext.of("#Shape[Rectangle 200 50 100 25] " +
            "#Shape[Rectangle 50 200 25 150]"), null, null);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(20000D, (double) result.get());
    }

    @Command(alias = "ImplicitArrayTest2", description = "Tests the @Implicit attribute with no parameter after it")
    private double implicitArrayTokenAtEndMethodTest(@Implicit Shape[] shapes) {
        double totalArea = 0;
        for (Shape shape : shapes) {
            totalArea += shape.getArea();
        }

        return totalArea;
    }

    @Test
    public void stringDefaultValueTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this,"stringDefaultValueMethodTest"));

        Assertions.assertTrue(command.matches(InvocationContext.of("")));
        Assertions.assertTrue(command.matches(InvocationContext.of("Hi")));
        Assertions.assertTrue(command.matches(InvocationContext.of("-1")));
        Assertions.assertFalse(command.matches(InvocationContext.of("a -1")));

        Optional<Object> oResult = command.invoke(InvocationContext.of(""), null, null);
        Assertions.assertTrue(oResult.isPresent());
        Assertions.assertEquals("", oResult.get());
    }

    @Command(alias = "test")
    private String stringDefaultValueMethodTest(@Unrequired String string) {
        return string;
    }

    @Test
    public void commandWithMessageReceivedEventParameterTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithMessageReceivedEventParameterMethodTest"));

        Assertions.assertTrue(command.getCommandTokens().isEmpty());
        Assertions.assertTrue(command.matches(InvocationContext.of("")));
    }

    @Command(alias = "test")
    private void commandWithMessageReceivedEventParameterMethodTest(MessageReceivedEvent event) {

    }

    @Test
    public void commandWithMultipleAnnotationsTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithMultipleAnnotationsMethodTest"));

        Assertions.assertEquals(1, command.getCommandTokens().size());
        Assertions.assertTrue(command.getCommandTokens().get(0) instanceof ArrayToken);
        Assertions.assertEquals(2, ((ParsingToken) command.getCommandTokens().get(0)).getAnnotations().length);
        Assertions.assertTrue(command.matches(InvocationContext.of("")));
        Assertions.assertTrue(command.matches(InvocationContext.of("1 2 3")));
        Assertions.assertTrue(command.matches(InvocationContext.of("[1 2 3]")));
        Assertions.assertFalse(command.matches(InvocationContext.of("1.0 2 3")));
        Assertions.assertFalse(command.matches(InvocationContext.of("[1 2 3")));
    }

    @Command(alias = "test")
    private void commandWithMultipleAnnotationsMethodTest(@Unrequired("[]") @Implicit int[] array) {

    }

    @Test
    public void commandWithVarargsTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithVarargsMethodTest"));

        Assertions.assertEquals(1, command.getCommandTokens().size());
        Assertions.assertTrue(command.getCommandTokens().get(0) instanceof ArrayToken);
        Assertions.assertFalse(command.matches(InvocationContext.of("")));
        Assertions.assertFalse(command.matches(InvocationContext.of("1 2 3")));
        Assertions.assertTrue(command.matches(InvocationContext.of("[1 2 3]")));
        Assertions.assertFalse(command.matches(InvocationContext.of("1.0 2 3")));
        Assertions.assertFalse(command.matches(InvocationContext.of("[1 2 3")));
    }

    @Command(alias = "test")
    private void commandWithVarargsMethodTest(int... values) {

    }

    @Test
    public void commandStringWithMultipleAnnotationsTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandStringWithMultipleAnnotationsMethodTest"));

        Assertions.assertEquals(4, command.getCommandTokens().size());
        Assertions.assertTrue(command.matches(InvocationContext.of("2 x 3")));
        Assertions.assertTrue(command.matches(InvocationContext.of("2 x 2 [1 0 0 1]")));
        Assertions.assertTrue(command.matches(InvocationContext.of("2 2 [1 0 0 1]")));
        Assertions.assertTrue(command.matches(InvocationContext.of("2 x 2 1 0 0 1")));
        Assertions.assertTrue(command.matches(InvocationContext.of("2 2 1 0 0 1")));
        Assertions.assertFalse(command.matches(InvocationContext.of("2 2 x 1 0 0 1")));
        Assertions.assertFalse(command.matches(InvocationContext.of("2 2 1.2")));
        Assertions.assertFalse(command.matches(InvocationContext.of("2 2 [1 2 0.0 3]")));
    }

    @Command(alias = "test", command = "#int <x> #int #int[]")
    private void commandStringWithMultipleAnnotationsMethodTest(int rows, int columns, @Unrequired("[]") @Implicit int... values) {

    }

    @Test
    public void commandWithComplexCustomParameterMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Result<Object[]> result1 = command.parseParameters(InvocationContext.of("Matrix[2 2 x 1 0 0 1]"));
        Result<Object[]> result2 = command.parseParameters(InvocationContext.of("Matr[2 2 [1 2 0.0 3]]"));
        Result<Object[]> result3 = command.parseParameters(InvocationContext.of("Matrix[2 2 [1 2 1 3]"));

        Assertions.assertEquals(1, command.getCommandTokens().size());
        Assertions.assertTrue(command.parseParameters(InvocationContext.of("Matrix[2 x 3]")).hasValue());
        Assertions.assertTrue(command.parseParameters(InvocationContext.of("Matrix[2 x 2 [1 0 0 1]]")).hasValue());
        Assertions.assertTrue(command.parseParameters(InvocationContext.of("Matrix[2 2 [1 0 0 1]]")).hasValue());
        Assertions.assertTrue(command.parseParameters(InvocationContext.of("Matrix[2 x 2 1 0 0 1]")).hasValue());
        Assertions.assertTrue(command.parseParameters(InvocationContext.of("Matrix[2 2 1 0 0 1]")).hasValue());
        Assertions.assertTrue(command.parseParameters(InvocationContext.of("Matrix[2 2 1.2]")).hasValue());
        Assertions.assertTrue(command.parseParameters(InvocationContext.of("Matrix[2 2 [1 2 0.0 3]]")).hasValue());
        Assertions.assertTrue(command.parseParameters(InvocationContext.of("Matrix[2 x 3 [1 2 3 4 5 6]]")).hasValue());

        Assertions.assertFalse(result1.hasValue());
        Assertions.assertEquals("There doesn't appear to be a constructor that matches '2 2 x 1 0 0 1'",
            result1.getReason().getTranslation(Language.EN_UK));

        Assertions.assertFalse(result2.hasValue());
        Assertions.assertEquals("The alias 'Matr', didn't match the expected Matrix",
            result2.getReason().getTranslation(Language.EN_UK));

        Assertions.assertFalse(result3.hasValue());
        Assertions.assertEquals("The token 'alpha' doesn't match the required syntax for a double",
            result3.getReason().getTranslation(Language.EN_UK));
    }

    @Test
    public void commandWithComplexCustomParameterInvocationTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Optional<Object> oResult = command.invoke(InvocationContext.of("#Matrix[2 x 3 [1 2 3 4 5 6]]"),
            null, null);

        Assertions.assertTrue(oResult.isPresent());
        Assertions.assertEquals(3, oResult.get());
    }

    @Test
    public void commandWith2DArrayTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Assertions.assertTrue(command.matches(InvocationContext.of("#Matrix[]")));
        Assertions.assertTrue(command.matches(InvocationContext.of("#Matrix[[1 0 0 1]]")));
        Assertions.assertTrue(command.matches(InvocationContext.of("#Matrix[[1 0] [0 1]]")));
        Assertions.assertFalse(command.matches(InvocationContext.of("#Matrix[[0 1] 0 1 2]")));
    }

    @Test
    public void commandMethodMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Assertions.assertTrue(command.matches(InvocationContext.of("#Matrix.scale(2)")));
        Assertions.assertTrue(command.matches(InvocationContext.of("#Matrix.roTaTe(45)")));
        Assertions.assertTrue(command.matches(InvocationContext.of("#Matrix.xShear(2)")));
        Assertions.assertFalse(command.matches(InvocationContext.of("#Matrix.yShear(4")));
    }

    @Test
    public void commandFieldMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Assertions.assertTrue(command.matches(InvocationContext.of("#Matrix.UnitSquare")));
        Assertions.assertTrue(command.matches(InvocationContext.of("#Matrix.uNiTsquAre")));
        Assertions.assertFalse(command.matches(InvocationContext.of("#Matrix.unitSquare()")));
    }

    @Command(alias = "test", reflections = Matrix.class)
    private int commandWithComplexCustomParameterMethodTest(Matrix matrix) {
        return matrix.getColumns();
    }




}
