package nz.pumbas.commands;


import nz.pumbas.commands.Annotations.Unrequired;
import nz.pumbas.commands.tokens.ArrayToken;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.utilities.Utilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class TokenTests {

    @Test
    public void arrayTokenTest() {
        ArrayToken token = new ArrayToken(false, float[].class, "[1 4.5 3.1]");
        Object[] defaultValues = token.getDefaultValue();

        assert defaultValues != null;

        Assertions.assertEquals(1F, defaultValues[0]);
        Assertions.assertEquals(4.5F, defaultValues[1]);
        Assertions.assertEquals(3.1F, defaultValues[2]);

        Assertions.assertFalse(token.matches("[1 alpha 2.5]"));
        Assertions.assertTrue(token.matches("[1.3 4.7 3]"));

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
}
