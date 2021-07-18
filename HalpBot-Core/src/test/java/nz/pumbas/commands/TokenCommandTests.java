package nz.pumbas.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.objects.Matrix;
import nz.pumbas.objects.Shape;
import nz.pumbas.objects.Vector3;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.Reflect;

public class TokenCommandTests
{
    @Test
    public void tokenCommandMatchesTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Assertions.assertTrue(tokenCommand.parse(ParsingContext.of("1 [2 3 4 1]")).present());
        Assertions.assertTrue(tokenCommand.parse(ParsingContext.of("2")).present());
        Assertions.assertTrue(tokenCommand.parse(ParsingContext.of("2 [1 a 2]")).absent());
        Assertions.assertTrue(tokenCommand.parse(ParsingContext.of("abc [1 3 2]")).absent());
        Assertions.assertTrue(tokenCommand.parse(ParsingContext.of("2 agf")).absent());
    }

    @Test
    public void simpleTokenCommandInvokeTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Exceptional<Boolean> result1 = tokenCommand.parse(ParsingContext.of("1 [2 1 4 3]")).map(o -> (Boolean) o);
        Exceptional<Boolean> result2 = tokenCommand.parse(ParsingContext.of("2 [9 5 4 3]")).map(o -> (Boolean) o);
        Assertions.assertTrue(result1.present());
        Assertions.assertTrue(result2.present());
        Assertions.assertTrue(result1.get());
        Assertions.assertFalse(result2.get());
    }

    @Test
    public void testDefaultValueForLastElementTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Exceptional<Boolean> result = tokenCommand.parse(ParsingContext.of("1")).map(o -> (Boolean) o);

        Assertions.assertTrue(result.present());
        Assertions.assertFalse(result.get());
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

        Assertions.assertTrue(command.parse(ParsingContext.of("2 [1 3 3]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("3")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("")).absent());
        Assertions.assertTrue(command.parse(ParsingContext.of("alpha")).absent());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 [1 4 c]")).absent());

        Exceptional<Boolean> result = command.parse(ParsingContext.of("2 [1 2 3]")).map(o -> (Boolean) o);
        Assertions.assertTrue(result.present());
        Assertions.assertTrue(result.get());

    }

    @Test
    public void customObjectTokenCommandTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "customObjectTokenCommandMethodTest"));

        Assertions.assertTrue(command.parse(ParsingContext.of("Vector3[1 2 3]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Vector3[3 1]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("#Vector3[3 1]")).absent());
        Assertions.assertTrue(command.parse(ParsingContext.of("[3 1 2]")).absent());

        Exceptional<Double> result = command.parse(ParsingContext.of("Vector3[1 2 3]")).map(o -> (Double) o);
        Assertions.assertTrue(result.present());
        Assertions.assertEquals(2, result.get());

    }

    @Command(alias = "CustomObject", description = "Tests if it successfully parses a custom object")
    private double customObjectTokenCommandMethodTest(Vector3 vector3) {
        return vector3.getY();
    }

    @Test
    public void implicitArrayTokenTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this,"implicitArrayTokenMethodTest"));

        Assertions.assertTrue(command.parse(ParsingContext.of("2 3 2 1 4 Heyo")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 3 Hi")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 [2 3 8] Hi")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 Hi")).absent());
        Assertions.assertTrue(command.parse(ParsingContext.of("a 1 2 Hi")).absent());

        Exceptional<Object> result = command.parse(ParsingContext.of("2 3 2 1 4 Heyo"));
        Assertions.assertTrue(result.present());
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

        Exceptional<Object> result1 = command.parse(ParsingContext.of(""));
        Exceptional<Double> result2 = command.parse(ParsingContext.of(
            "Shape[Rectangle 200 50 100 25] Shape[Rectangle 50 200 25 150]")).map(o -> (Double) o);

        Assertions.assertTrue(command.parse(ParsingContext.of(
            "Shape[Rectangle 200 50 100 25] Shape[Rectangle 50 200 25 150] Shape[Rectangle 200 50 100 275]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of(
            "Shape[Rectangle 200 50 100 25]")).present());

        Assertions.assertTrue(result1.absent());
//        Assertions.assertEquals("You appear to be missing a few parameters for this command",
//            Exceptional1.getReason().getTranslation(Language.EN_UK));

        Assertions.assertTrue(result2.present());
        Assertions.assertEquals(20000D, result2.get());
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

        Assertions.assertTrue(command.parse(ParsingContext.of("")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Hi")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("-1")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("a -1")).absent());

        Exceptional<Object> result = command.parse(ParsingContext.of(""));
        Assertions.assertTrue(result.present());
        Assertions.assertEquals("default value", result.get());
    }

    @Command(alias = "test")
    private String stringDefaultValueMethodTest(@Unrequired("default value") String string) {
        return string;
    }

    @Test
    public void commandWithMessageReceivedEventParameterTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithMessageReceivedEventParameterMethodTest"));

        Assertions.assertEquals(1, command.getCommandTokens().size());
        Assertions.assertTrue(command.parse(ParsingContext.of("")).present());
    }

    @Command(alias = "test")
    private void commandWithMessageReceivedEventParameterMethodTest(MessageReceivedEvent event) {

    }

    @Test
    public void commandWithMultipleAnnotationsTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithMultipleAnnotationsMethodTest"));

        Assertions.assertEquals(1, command.getCommandTokens().size());
        Assertions.assertEquals(2, ((ParsingToken) command.getCommandTokens().get(0)).annotations().length);

        Assertions.assertTrue(command.parse(ParsingContext.of("")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("1 2 3")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("[1 2 3]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("1.0 2 3")).absent());
        Assertions.assertTrue(command.parse(ParsingContext.of("[1 2 3")).absent());
    }

    @Command(alias = "test")
    private void commandWithMultipleAnnotationsMethodTest(@Unrequired("[]") @Implicit int[] array) {

    }

    @Test
    public void commandWithVarargsTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithVarargsMethodTest"));

        Assertions.assertEquals(1, command.getCommandTokens().size());
        Assertions.assertTrue(command.parse(ParsingContext.of("[1 2 3]")).isErrorAbsent());
        Assertions.assertTrue(command.parse(ParsingContext.of("")).caught());
        Assertions.assertTrue(command.parse(ParsingContext.of("1 2 3")).caught());
        Assertions.assertTrue(command.parse(ParsingContext.of("1.0 2 3")).caught());
        Assertions.assertTrue(command.parse(ParsingContext.of("[1 2 3")).caught());
    }

    @Command(alias = "test")
    private void commandWithVarargsMethodTest(int... values) {

    }

    @Test
    public void commandStringWithMultipleAnnotationsTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandStringWithMultipleAnnotationsMethodTest"));

        Assertions.assertEquals(4, command.getCommandTokens().size());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 x 3")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 x 2 [1 0 0 1]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 2 [1 0 0 1]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 x 2 1 0 0 1")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 2 1 0 0 1")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 2 x 1 0 0 1")).absent());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 2 1.2")).absent());
        Assertions.assertTrue(command.parse(ParsingContext.of("2 2 [1 2 0.0 3]")).absent());
    }

    @Command(alias = "test", command = "#Integer <x> #Integer #Integer[]")
    private void commandStringWithMultipleAnnotationsMethodTest(int rows, int columns, @Unrequired("[]") @Implicit int... values) {

    }

    @Test
    public void commandWithComplexCustomParameterMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Exceptional<Object> result1 = command.parse(ParsingContext.of("Matrix[2 2 x 1 0 0 1]"));
        Exceptional<Object> result2 = command.parse(ParsingContext.of("Matr[2 2 [1 2 0.0 3]]"));
        Exceptional<Object> result3 = command.parse(ParsingContext.of("Matrix[2 2 [1 2 1 3]"));

        Assertions.assertEquals(1, command.getCommandTokens().size());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[2 x 3]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[2 x 2 [1 0 0 1]]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[2 2 [1 0 0 1]]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[2 x 2 1 0 0 1]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[2 2 1 0 0 1]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[2 2 1.2]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[2 2 [1 2 0.0 3]]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[2 x 3 [1 2 3 4 5 6]]")).present());

        Assertions.assertFalse(result1.present());
        Assertions.assertEquals("Expected surrounding '[ ]' when creating the list of double",
            result1.error().getMessage());

        Assertions.assertFalse(result2.present());
        Assertions.assertEquals("The alias 'Matr', didn't match the expected Matrix",
            result2.error().getMessage());

        Assertions.assertFalse(result3.present());
        Assertions.assertEquals("You seem to be missing a ']' when creating a Matrix",
            result3.error().getMessage());
    }

    @Test
    public void commandWithComplexCustomParameterInvocationTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Exceptional<Object> result = command.parse(ParsingContext.of("Matrix[2 x 3 [1 2 3 4 5 6]]"));

        Assertions.assertTrue(result.present());
        Assertions.assertEquals(3, result.get());
    }

    @Test
    public void commandWith2DArrayTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[[1 0 0 1]]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[[1 0] [0 1]]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix[[0 1] 0 1 2]")).absent());
    }

    @Test
    public void commandMethodMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Exceptional<Object> result = command.parse(ParsingContext.of("Matrix.yShear[4"));

        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix.scale[2]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix.roTaTe[45]")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix.xShear[2]")).present());

        Assertions.assertFalse(result.present());
    }

    @Test
    public void commandFieldMatchesTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "commandWithComplexCustomParameterMethodTest"));

        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix.UnitSquare")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix.uNiTsquAre")).present());
        Assertions.assertTrue(command.parse(ParsingContext.of("Matrix.unitSquare()")).absent());
    }

    @Command(alias = "test", reflections = Matrix.class)
    private int commandWithComplexCustomParameterMethodTest(Matrix matrix) {
        return matrix.getColumns();
    }
}
