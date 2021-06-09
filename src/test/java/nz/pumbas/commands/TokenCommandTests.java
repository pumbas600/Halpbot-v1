package nz.pumbas.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.utilities.Reflect;

public class TokenCommandTests
{
    @Test
    public void tokenCommandMatchesTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(InvocationTokenInfoTests.class, "containedWithinTestMethod"));

        Assertions.assertTrue(tokenCommand.matches(InvocationTokenInfo.of("1 [2 3 4 1]")));
        Assertions.assertTrue(tokenCommand.matches(InvocationTokenInfo.of("2")));
        Assertions.assertFalse(tokenCommand.matches(InvocationTokenInfo.of("2 [1 a 2]")));
        Assertions.assertFalse(tokenCommand.matches(InvocationTokenInfo.of("abc [1 3 2]")));
        Assertions.assertFalse(tokenCommand.matches(InvocationTokenInfo.of("2 agf")));
    }

    @Test
    public void simpleTokenCommandInvokeTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(TokenCommandTests.class, "containedWithinTestMethod"));

        Optional<Object> result1 = tokenCommand.invoke(InvocationTokenInfo.of("1 [2 1 4 3]"), null);
        Optional<Object> result2 = tokenCommand.invoke(InvocationTokenInfo.of("2 [9 5 4 3]"), null);

        Assertions.assertTrue(result1.isPresent());
        Assertions.assertTrue(result2.isPresent());
        Assertions.assertTrue((boolean) result1.get());
        Assertions.assertFalse((boolean) result2.get());
    }

    @Command(alias = "contained")
    private boolean containedWithinTestMethod(int num, @Unrequired("[]") int[] numbers) {
        for (int element : numbers) {
            if (num == element)
                return true;
        }
        return false;
    }
}
