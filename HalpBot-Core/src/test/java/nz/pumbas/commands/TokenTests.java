package nz.pumbas.commands;


import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.tokens.context.InvocationContext;
import nz.pumbas.commands.tokens.tokentypes.ArrayToken;
import nz.pumbas.commands.tokens.tokentypes.BuiltInTypeToken;
import nz.pumbas.commands.tokens.tokentypes.CommandToken;
import nz.pumbas.commands.tokens.tokentypes.MultiChoiceToken;
import nz.pumbas.commands.tokens.tokentypes.ObjectTypeToken;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.commands.tokens.tokentypes.PlaceholderToken;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.TokenSyntax;
//import nz.pumbas.halpbot.customparameters.Matrix;
//import nz.pumbas.halpbot.customparameters.Shape;
//import nz.pumbas.halpbot.customparameters.ShapeType;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Language;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.enums.Tristate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TokenTests {

    @Test
    public void arrayTokenDefaultValueTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[1 4.5 3.1]");

        float[] defaultValues = (float[]) token.getDefaultValue();

        assert null != defaultValues;

        Assertions.assertEquals(1F, defaultValues[0]);
        Assertions.assertEquals(4.5F, defaultValues[1]);
        Assertions.assertEquals(3.1F, defaultValues[2]);
    }

    @Test
    public void arrayTokenArrayTypeTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[1 4.5 3.1]");

        float[] defaultValues = (float[]) token.getDefaultValue();

        assert null != defaultValues;

        Assertions.assertTrue(float[].class.isAssignableFrom(defaultValues.getClass()));
    }

    @Test
    public void arrayTokenNullDefaultValueTest() {
        ArrayToken token = new ArrayToken(false, float[].class, null);
        ArrayToken anotherToken = new ArrayToken(false, float[].class, "null");

        Assertions.assertNull(token.getDefaultValue());
        Assertions.assertNull(anotherToken.getDefaultValue());
    }

    @Test
    public void arrayTokenInvalidDefaultValueTest() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new ArrayToken(false, float[].class, "[1 a 4.5]"));
    }

    @Test
    public void arrayTokenMatchesTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[1 4.5 3.1]");

        Result<Object> result1 = token.parse(InvocationContext.of("[1 alpha 2.5]"));
        Result<Object> result2 = token.parse(InvocationContext.of("[1.3 4.7 3]"));

        Assertions.assertFalse(result1.hasValue());
        Assertions.assertTrue(result1.hasReason());
        Assertions.assertEquals("The token 'alpha' doesn't match the required syntax for a float",
            result1.getReason().getTranslation(Language.EN_UK));

        Assertions.assertTrue(result2.hasValue());
        Assertions.assertFalse(result2.hasReason());
    }

    @Test
    public void arrayTokenEmptyArrayTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[]");
        float[] defaultValue = (float[]) token.getDefaultValue();

        Result<float[]> result = token.parse(InvocationContext.of("[]")).map(float[].class::cast);
        Assertions.assertTrue(result.hasValue());
        Assertions.assertEquals(0, ((float[])result.getValue()).length);
        Assertions.assertEquals(0, defaultValue.length);
    }

    @Test
    public void arrayTokenParsingTest() {
        ArrayToken token = new ArrayToken(false, float[].class, null);

        Result<float[]> result = token.parse(InvocationContext.of("[1.3 4.7 3]")).map(float[].class::cast);

        Assertions.assertTrue(result.hasValue());
        Assertions.assertEquals(1.3F, result.getValue()[0]);
        Assertions.assertEquals(4.7F, result.getValue()[1]);
        Assertions.assertEquals(3F, result.getValue()[2]);
    }

    @Test
    public void simpleCommandGenerationTest() {
        Class<?>[] types = new Class[] { Double.class, String.class, int[].class };
        String command = TokenManager.generateCommand(types, new Annotation[3][0]);

        Assertions.assertEquals("#double #String #int[]", command);
    }

    @Test
    public void optionalCommandGenerationTest() {
        Method method = Reflect.getMethod(TokenTests.class, "optionalCommandGenerationMethod");
        String command = TokenManager.generateCommand(method.getParameterTypes(), method.getParameterAnnotations());

        Assertions.assertEquals("<#double> #String #int[]", command);
    }

    private void optionalCommandGenerationMethod(@Unrequired("2") double valueA, String valueB, int[] valueC) {
        // Do something...
    }

    @Test
    public void simpleCommandParsingTest() {
        List<CommandToken> commandTokens = TokenManager.parseCommand(
                Reflect.getMethod(TokenTests.class, "simpleCommandParsingMethod"));
        int[] defaultValues = (int[]) ((ParsingToken) commandTokens.get(3)).getDefaultValue();


        Assertions.assertEquals(4, commandTokens.size());
        Assertions.assertFalse(commandTokens.get(0).isOptional());
        Assertions.assertTrue(commandTokens.get(1).isOptional());
        Assertions.assertFalse(commandTokens.get(2).isOptional());
        Assertions.assertTrue(commandTokens.get(3).isOptional());

        Assertions.assertTrue(commandTokens.get(0) instanceof BuiltInTypeToken);
        Assertions.assertTrue(commandTokens.get(1) instanceof PlaceholderToken);
        Assertions.assertTrue(commandTokens.get(2) instanceof BuiltInTypeToken);
        Assertions.assertTrue(commandTokens.get(3) instanceof ArrayToken);

        Assertions.assertEquals(0, defaultValues.length);
    }

    @Command(alias = "simpleParsingCommandTest", command = "#int <x> #int #int[]")
    private void simpleCommandParsingMethod(int matrixRows, int matrixHeight, @Unrequired("[]") int[] matrixValues) {

    }

    @Test
    public void multiChoiceTokenTest() {
        MultiChoiceToken token = new MultiChoiceToken(false, String.class, "x-axis",
                List.of("x", "x-axis", "y", "y-axis"));

        Result<String> result = token.parse(InvocationContext.of("axis")).map(String.class::cast);

        Assertions.assertTrue(token.parse(InvocationContext.of("x")).hasValue());
        Assertions.assertTrue(token.parse(InvocationContext.of("X-axis")).hasValue());
        Assertions.assertTrue(token.parse(InvocationContext.of("y")).hasValue());
        Assertions.assertTrue(token.parse(InvocationContext.of("y-Axis")).hasValue());
        Assertions.assertFalse(result.hasValue());
        Assertions.assertTrue(result.hasReason());
        Assertions.assertEquals("The token 'axis' didn't match one of the expected values [x, x-axis, y, y-axis]",
            result.getReason().getTranslation(Language.EN_UK));

        Assertions.assertEquals("x-axis", token.getDefaultValue());
    }

    @Test
    public void multiChoiceTokenSyntaxMatchesTest() {
        Assertions.assertTrue(TokenSyntax.MULTICHOICE.matches("[x-axis|y-axis]"));
        Assertions.assertFalse(TokenSyntax.MULTICHOICE.matches("axis"));
        Assertions.assertFalse(TokenSyntax.MULTICHOICE.matches("#[x-axis|y-axis]"));
    }

    @Test
    public void parsingMultiChoiceTokenTest() {
        List<CommandToken> commandTokens = TokenManager.parseCommand(
                "#double <from the> [x-axis|y-axis]", new Class<?>[] { Double.class, String.class },
            new Annotation[2][0]);
        ParsingToken multiChoiceToken = (ParsingToken) commandTokens.get(3);

        Assertions.assertEquals(4, commandTokens.size());
        Assertions.assertTrue(commandTokens.get(0) instanceof BuiltInTypeToken);
        Assertions.assertTrue(commandTokens.get(1) instanceof PlaceholderToken);
        Assertions.assertTrue(commandTokens.get(2) instanceof PlaceholderToken);
        Assertions.assertTrue(commandTokens.get(3) instanceof MultiChoiceToken);

        Assertions.assertTrue(multiChoiceToken.parse(InvocationContext.of("x-axis")).hasValue());
        Assertions.assertTrue(multiChoiceToken.parse(InvocationContext.of("Y-axis")).hasValue());
        Assertions.assertFalse(multiChoiceToken.parse(InvocationContext.of("axis")).hasValue());
    }

//    @Test
//    public void enumTokenTest() {
//        BuiltInTypeToken token = new BuiltInTypeToken(false, ShapeType.class, null);
//
//        Result<ShapeType> result = token.parse(InvocationContext.of("Line")).map(ShapeType.class::cast);
//
//        Assertions.assertTrue(token.parse(InvocationContext.of("Square")).hasValue());
//        Assertions.assertTrue(token.parse(InvocationContext.of("Circle")).hasValue());
//        Assertions.assertFalse(token.parse(InvocationContext.of("1")).hasValue());
//        Assertions.assertFalse(result.hasValue());
//        Assertions.assertTrue(result.hasReason());
//        Assertions.assertEquals("The token 'Line' doesn't match any of the values for the enum ShapeType",
//            result.getReason().getTranslation(Language.EN_UK));
//    }
//
//    @Test
//    public void objectTypeTokenTest() {
//        ObjectTypeToken token = new ObjectTypeToken(false, Shape.class, null);
//
//        Result<Shape> squareResult = token.parse(InvocationContext.of("Shape[Square 2 0 0]")).cast();
//        Result<Shape> rectangleResult = token.parse(InvocationContext.of("Shape[Rectangle 2 1 0 0]")).cast();
//        Result<Object> falseResult1 = token.parse(InvocationContext.of("Shope[Square 2 0 0]"));
//        Result<Object> falseResult2 = token.parse(InvocationContext.of("Shape[2 0 0]"));
//
//        Assertions.assertTrue(squareResult.hasValue());
//        Assertions.assertTrue(rectangleResult.hasValue());
//
//        Assertions.assertFalse(falseResult1.hasValue());
//        Assertions.assertTrue(falseResult1.hasReason());
//        Assertions.assertEquals("The alias 'Shope', didn't match the expected Shape",
//            falseResult1.getReason().getTranslation(Language.EN_UK));
//
//        Assertions.assertFalse(falseResult2.hasValue());
//        Assertions.assertTrue(falseResult2.hasReason());
//        Assertions.assertEquals("The token '2' doesn't match any of the values for the enum ShapeType",
//            falseResult2.getReason().getTranslation(Language.EN_UK));
//
//        Assertions.assertEquals(4, squareResult.getValue().getArea());
//        Assertions.assertEquals(2, rectangleResult.getValue().getArea());
//    }
//
//    @Test
//    public void methodInvocationTest()
//    {
//        Result<Matrix> result = TokenManager.handleReflectionSyntax(
//            InvocationContext.of("Matrix.scale[2]"), Set.of(Matrix.class), Matrix.class).cast();
//
//        Assertions.assertTrue(result.hasValue());
//        Assertions.assertEquals(4, result.getValue().getDeterminant());
//    }

    @Test
    public void TwoDArrayTest() {
        ArrayToken oneDArrayToken = new ArrayToken(false, float[].class, null);
        ArrayToken twoDArrayToken = new ArrayToken(false, float[][].class, null);

        Result<float[]>   result1D = oneDArrayToken.parse(InvocationContext.of("[1 2 3]")).cast();
        Result<float[][]> result2D = twoDArrayToken.parse(InvocationContext.of("[[1 1 1] [2 2 2] [3 3]]")).cast();

        Result<float[][]> resultMissingBracket =
            twoDArrayToken.parse(InvocationContext.of("[[1 1 1] [2 2 2] [3 3]")).cast();

        Assertions.assertTrue(result1D.hasValue());
        Assertions.assertTrue(result2D.hasValue());

        Assertions.assertFalse(resultMissingBracket.hasValue());
        Assertions.assertEquals("Expected surrounding '[ ]' when creating the list of float[]",
            resultMissingBracket.getReason().getTranslation(Language.EN_UK));
    }

    @Test
    public void test() {
        System.out.println(Enum.class.isAssignableFrom(Tristate.class));
        System.out.println(Object[].class.isAssignableFrom(float[].class));
        System.out.println(Object.class.isAssignableFrom(Object[].class));
    }
}
