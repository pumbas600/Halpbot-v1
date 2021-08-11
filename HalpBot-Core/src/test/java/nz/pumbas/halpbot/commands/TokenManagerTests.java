package nz.pumbas.halpbot.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nz.pumbas.halpbot.commands.tokens.TokenManager;
import nz.pumbas.halpbot.utilities.Reflect;

public class TokenManagerTests
{
    @Test
    public void generateUsageTest() {
        String usage = TokenManager.generateUsage(Reflect.getMethod(this, "testMethod"));

        Assertions.assertEquals("<first{String}> <second{Integer}>", usage);
    }

    private void testMethod(String first, int second) {}
}
