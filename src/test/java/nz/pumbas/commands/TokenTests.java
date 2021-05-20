package nz.pumbas.commands;


import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.Unrequired;
import nz.pumbas.commands.tokens.*;
import nz.pumbas.utilities.Utilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public class TokenTests {

    @Test
    public void arrayTokenDefaultValueTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[1 4.5 3.1]");

        Object[] defaultValues = token.getDefaultValue();

        assert defaultValues != null;

        Assertions.assertEquals(1F, defaultValues[0]);
        Assertions.assertEquals(4.5F, defaultValues[1]);
        Assertions.assertEquals(3.1F, defaultValues[2]);
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

        Assertions.assertFalse(token.matches("[1 alpha 2.5]"));
        Assertions.assertTrue(token.matches("[1.3 4.7 3]"));
    }

    @Test
    public void arrayTokenEmptyArrayTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[]");
        Object[] defaultValue = token.getDefaultValue();

        Assertions.assertTrue(token.matches("[]"));
        Assertions.assertEquals(0, defaultValue.length);
    }

    @Test
    public void arrayTokenParsingTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[1 4.5 3.1]");

        Object[] parsedValues = token.parse("[1.3 4.7 3]");
        Assertions.assertEquals(1.3F, parsedValues[0]);
        Assertions.assertEquals(4.7F, parsedValues[1]);
        Assertions.assertEquals(3F, parsedValues[2]);
    }

    @Test
    public void simpleCommandGenerationTest() {
        Class<?>[] types = new Class[] { Double.class, String.class, int[].class };
        String command = TokenManager.generateCommand(types, new Annotation[3][0]);

        Assertions.assertEquals("#Double #String #int[]", command);
    }

    @Test
    public void optionalCommandGenerationTest() {
        Method method = Utilities.getMethod(TokenTests.class, "optionalCommandGenerationMethod").get();
        String command = TokenManager.generateCommand(method.getParameterTypes(), method.getParameterAnnotations());

        Assertions.assertEquals("<#double> #String #int[]", command);
    }

    private void optionalCommandGenerationMethod(@Unrequired("2") double valueA, String valueB, int[] valueC) {
        // Do something...
    }

    @Test
    public void simpleCommandParsingTest() {
        List<CommandToken> commandTokens = TokenManager.parseCommand(
                Utilities.getMethod(TokenTests.class, "simpleCommandParsingMethod").get());

        Assertions.assertEquals(4, commandTokens.size());
        Assertions.assertFalse(commandTokens.get(0).isOptional());
        Assertions.assertTrue(commandTokens.get(1).isOptional());
        Assertions.assertFalse(commandTokens.get(2).isOptional());
        Assertions.assertTrue(commandTokens.get(3).isOptional());

        Assertions.assertTrue(commandTokens.get(0) instanceof BuiltInTypeToken);
        Assertions.assertTrue(commandTokens.get(1) instanceof PlaceholderToken);
        Assertions.assertTrue(commandTokens.get(2) instanceof BuiltInTypeToken);
        Assertions.assertTrue(commandTokens.get(3) instanceof ArrayToken);

        Assertions.assertEquals(0, ((ArrayToken) commandTokens.get(3)).getDefaultValue().length);
    }

    @Command(alias = "simpleParsingCommandTest", command = "#int <x> #int #int[]")
    private void simpleCommandParsingMethod(int matrixRows, int matrixHeight, @Unrequired("[]") int[] matrixValues) {

    }
}
