package nz.pumbas.commands;


import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.commands.tokens.tokentypes.ArrayToken;
import nz.pumbas.commands.tokens.tokentypes.BuiltInTypeToken;
import nz.pumbas.commands.tokens.tokentypes.CommandToken;
import nz.pumbas.commands.tokens.tokentypes.MultiChoiceToken;
import nz.pumbas.commands.tokens.tokentypes.ObjectTypeToken;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.commands.tokens.tokentypes.PlaceholderToken;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.TokenSyntax;
import nz.pumbas.halpbot.customparameters.Matrix;
import nz.pumbas.halpbot.customparameters.Shape;
import nz.pumbas.halpbot.customparameters.ShapeType;
import nz.pumbas.utilities.Reflect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

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

        Assertions.assertFalse(token.matches(InvocationTokenInfo.of("[1 alpha 2.5]")));
        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("[1.3 4.7 3]")));
    }

    @Test
    public void arrayTokenEmptyArrayTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[]");
        float[] defaultValue = (float[]) token.getDefaultValue();

        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("[]")));
        Assertions.assertEquals(0, defaultValue.length);
    }

    @Test
    public void arrayTokenParsingTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[1 4.5 3.1]");

        float[] parsedValues = (float[]) token.parse(InvocationTokenInfo.of("[1.3 4.7 3]"));
        Assertions.assertEquals(1.3F, parsedValues[0]);
        Assertions.assertEquals(4.7F, parsedValues[1]);
        Assertions.assertEquals(3F, parsedValues[2]);
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

        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("x")));
        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("X-axis")));
        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("y")));
        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("y-Axis")));
        Assertions.assertFalse(token.matches(InvocationTokenInfo.of("axis")));
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
        CommandToken multiChoiceToken = commandTokens.get(3);

        Assertions.assertEquals(4, commandTokens.size());
        Assertions.assertTrue(commandTokens.get(0) instanceof BuiltInTypeToken);
        Assertions.assertTrue(commandTokens.get(1) instanceof PlaceholderToken);
        Assertions.assertTrue(commandTokens.get(2) instanceof PlaceholderToken);
        Assertions.assertTrue(commandTokens.get(3) instanceof MultiChoiceToken);

        Assertions.assertTrue(multiChoiceToken.matches(InvocationTokenInfo.of("x-axis")));
        Assertions.assertTrue(multiChoiceToken.matches(InvocationTokenInfo.of("Y-axis")));
        Assertions.assertFalse(multiChoiceToken.matches(InvocationTokenInfo.of("axis")));
    }

    @Test
    public void enumTokenTest() {
        BuiltInTypeToken token = new BuiltInTypeToken(false, ShapeType.class, null);

        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("Square")));
        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("Circle")));
        Assertions.assertFalse(token.matches(InvocationTokenInfo.of("Line")));
        Assertions.assertFalse(token.matches(InvocationTokenInfo.of("1")));
    }

    @Test
    public void objectTypeTokenTest() {
        ObjectTypeToken token = new ObjectTypeToken(false, Shape.class, null);

        Shape square = (Shape) token.parse(InvocationTokenInfo.of("#Shape[Square 2 0 0]"));
        Shape rectangle = (Shape) token.parse(InvocationTokenInfo.of("#Shape[Rectangle 2 1 0 0]"));

        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("#Shape[Square 2 0 0]")));
        Assertions.assertTrue(token.matches(InvocationTokenInfo.of("#Shape[Rectangle 2 1 0 0]")));
        Assertions.assertFalse(token.matches(InvocationTokenInfo.of("#Shope[Square 2 0 0]")));
        Assertions.assertFalse(token.matches(InvocationTokenInfo.of("#Shape[2 0 0]")));

        Assertions.assertNotNull(square);
        Assertions.assertNotNull(rectangle);
        Assertions.assertEquals(4, square.getArea());
        Assertions.assertEquals(2, rectangle.getArea());
    }

    @Test
    public void methodInvocationTest()
    {
        Optional<Object> oObject = TokenManager.handleReflectionSyntax(
            InvocationTokenInfo.of("#Matrix.scale(2)"), List.of(Matrix.class), Matrix.class);

        Assertions.assertTrue(oObject.isPresent());
        Assertions.assertTrue(oObject.get() instanceof Matrix);
        Assertions.assertEquals(4, ((Matrix)oObject.get()).getDeterminant());
    }
}
