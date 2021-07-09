package nz.pumbas.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.context.InvocationContext;
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

        Assertions.assertTrue(tokenCommand.parse(InvocationContext.of("1 [2 3 4 1]")).isReasonAbsent());
        Assertions.assertTrue(tokenCommand.parse(InvocationContext.of("2")).isReasonAbsent());
        Assertions.assertTrue(tokenCommand.parse(InvocationContext.of("2 [1 a 2]")).hasReason());
        Assertions.assertTrue(tokenCommand.parse(InvocationContext.of("abc [1 3 2]")).hasReason());
        Assertions.assertTrue(tokenCommand.parse(InvocationContext.of("2 agf")).hasReason());
    }

    @Test
    public void simpleTokenCommandInvokeTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Result<Boolean> result1 = tokenCommand.parse(InvocationContext.of("1 [2 1 4 3]")).cast();
        Result<Boolean> result2 = tokenCommand.parse(InvocationContext.of("2 [9 5 4 3]")).cast();

        Assertions.assertTrue(result1.hasValue());
        Assertions.assertTrue(result2.hasValue());
        Assertions.assertTrue(result1.getValue());
        Assertions.assertFalse(result2.getValue());
    }

    @Test
    public void testDefaultValueForLastElementTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Result<Boolean> result = tokenCommand.parse(InvocationContext.of("1")).cast();

        Assertions.assertTrue(result.hasValue());
        Assertions.assertFalse(result.getValue());
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

        Assertions.assertTrue(command.parse(InvocationContext.of("2 [1 3 3]")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("3")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("alpha")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 [1 4 c]")).hasReason());

        Result<Boolean> result = command.parse(InvocationContext.of("2 [1 2 3]")).cast();
        Assertions.assertTrue(result.hasValue());
        Assertions.assertTrue(result.getValue());

    }

    @Test
    public void customObjectTokenCommandTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "customObjectTokenCommandMethodTest"));

        Assertions.assertTrue(command.parse(InvocationContext.of("Vector3[1 2 3]")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("Vector3[3 1]")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("#Vector3[3 1]")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("[3 1 2]")).hasReason());

        Result<Double> result = command.parse(InvocationContext.of("Vector3[1 2 3]")).cast();
        Assertions.assertTrue(result.hasValue());
        Assertions.assertEquals(2, result.getValue());

    }

    @Command(alias = "CustomObject", description = "Tests if it successfully parses a custom object")
    private double customObjectTokenCommandMethodTest(Vector3 vector3) {
        return vector3.getY();
    }

    @Test
    public void implicitArrayTokenTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this,"implicitArrayTokenMethodTest"));

        Assertions.assertTrue(command.parse(InvocationContext.of("2 3 2 1 4 Heyo")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 3 Hi")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 [2 3 8] Hi")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 Hi")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("a 1 2 Hi")).hasReason());

        Result<Object> result = command.parse(InvocationContext.of("2 3 2 1 4 Heyo"));
        Assertions.assertTrue(result.hasValue());
        Assertions.assertEquals("2: [3, 2, 1, 4]: Heyo", result.getValue());
    }


    @Command(alias = "ImplicitArray", description = "Tests the @Implicit attribute on arrays")
    private String implicitArrayTokenMethodTest(int num, @Implicit int[] array, String stop) {
        return String.format("%s: %s: %s", num, Arrays.toString(array), stop);
    }

    @Test
    public void implicitArrayTokenAtEndTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this,"implicitArrayTokenAtEndMethodTest"));

        Result<Object> result1 = command.parse(InvocationContext.of(""));
        Result<Double> result2 = command.parse(InvocationContext.of(
            "Shape[Rectangle 200 50 100 25] Shape[Rectangle 50 200 25 150]")).cast();

        Assertions.assertTrue(command.parse(InvocationContext.of(
            "Shape[Rectangle 200 50 100 25] Shape[Rectangle 50 200 25 150] Shape[Rectangle 200 50 100 275]")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of(
            "Shape[Rectangle 200 50 100 25]")).isReasonAbsent());

        Assertions.assertTrue(result1.hasReason());
        Assertions.assertEquals("You appear to be missing a few parameters for this command",
            result1.getReason().getTranslation(Language.EN_UK));

        Assertions.assertTrue(result2.hasValue());
        Assertions.assertEquals(20000D, result2.getValue());
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

        Assertions.assertTrue(command.parse(InvocationContext.of("")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("Hi")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("-1")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("a -1")).hasReason());

        Result<Object> result = command.parse(InvocationContext.of(""));
        Assertions.assertTrue(result.hasValue());
        Assertions.assertEquals("", result.getValue());
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
        Assertions.assertTrue(command.parse(InvocationContext.of("")).isReasonAbsent());
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

        Assertions.assertTrue(command.parse(InvocationContext.of("")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("1 2 3")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("[1 2 3]")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("1.0 2 3")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("[1 2 3")).hasReason());
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
        Assertions.assertTrue(command.parse(InvocationContext.of("[1 2 3]")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("1 2 3")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("1.0 2 3")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("[1 2 3")).hasReason());
    }

    @Command(alias = "test")
    private void commandWithVarargsMethodTest(int... values) {

    }

    @Test
    public void commandStringWithMultipleAnnotationsTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandStringWithMultipleAnnotationsMethodTest"));

        Assertions.assertEquals(4, command.getCommandTokens().size());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 x 3")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 x 2 [1 0 0 1]")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 2 [1 0 0 1]")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 x 2 1 0 0 1")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 2 1 0 0 1")).isReasonAbsent());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 2 x 1 0 0 1")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 2 1.2")).hasReason());
        Assertions.assertTrue(command.parse(InvocationContext.of("2 2 [1 2 0.0 3]")).hasReason());
    }

    @Command(alias = "test", command = "#int <x> #int #int[]")
    private void commandStringWithMultipleAnnotationsMethodTest(int rows, int columns, @Unrequired("[]") @Implicit int... values) {

    }

    @Test
    public void commandWithComplexCustomParameterMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Result<Object> result1 = command.parse(InvocationContext.of("Matrix[2 2 x 1 0 0 1]"));
        Result<Object> result2 = command.parse(InvocationContext.of("Matr[2 2 [1 2 0.0 3]]"));
        Result<Object> result3 = command.parse(InvocationContext.of("Matrix[2 2 [1 2 1 3]"));

        Assertions.assertEquals(1, command.getCommandTokens().size());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[2 x 3]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[2 x 2 [1 0 0 1]]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[2 2 [1 0 0 1]]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[2 x 2 1 0 0 1]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[2 2 1 0 0 1]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[2 2 1.2]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[2 2 [1 2 0.0 3]]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[2 x 3 [1 2 3 4 5 6]]")).hasValue());

        Assertions.assertFalse(result1.hasValue());
        Assertions.assertEquals("Expected surrounding '[ ]' when creating the list of double",
            result1.getReason().getTranslation(Language.EN_UK));

        Assertions.assertFalse(result2.hasValue());
        Assertions.assertEquals("The alias 'Matr', didn't match the expected Matrix",
            result2.getReason().getTranslation(Language.EN_UK));

        Assertions.assertFalse(result3.hasValue());
        Assertions.assertEquals("You seem to be missing a ']' when creating a Matrix",
            result3.getReason().getTranslation(Language.EN_UK));
    }

    @Test
    public void commandWithComplexCustomParameterInvocationTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Result<Object> result = command.parse(InvocationContext.of("Matrix[2 x 3 [1 2 3 4 5 6]]"));

        Assertions.assertTrue(result.hasValue());
        Assertions.assertEquals(3, result.getValue());
    }

    @Test
    public void commandWith2DArrayTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[[1 0 0 1]]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[[1 0] [0 1]]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix[[0 1] 0 1 2]")).isValueAbsent());
    }

    @Test
    public void commandMethodMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Result<Object> result = command.parse(InvocationContext.of("Matrix.yShear[4"));

        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix.scale[2]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix.roTaTe[45]")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix.xShear[2]")).hasValue());

        Assertions.assertFalse(result.hasValue());
    }

    @Test
    public void commandFieldMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix.UnitSquare")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix.uNiTsquAre")).hasValue());
        Assertions.assertTrue(command.parse(InvocationContext.of("Matrix.unitSquare()")).isValueAbsent());
    }

    @Command(alias = "test", reflections = Matrix.class)
    private int commandWithComplexCustomParameterMethodTest(Matrix matrix) {
        return matrix.getColumns();
    }




}
