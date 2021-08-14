package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.tokens.TokenManager;
import nz.pumbas.halpbot.utilities.Reflect;

public class TokenManagerTests
{
    @Test
    public void generateUsageTest() {
        String usage = TokenManager.generateUsage(Reflect.getMethod(this, "testMethod1"));

        Assertions.assertEquals("<first{String}> <second{Integer}>", usage);
    }

    @Test
    public void generateUsageExcludeEventTest() {
        String usage = TokenManager.generateUsage(Reflect.getMethod(this, "testMethod2"));

        Assertions.assertEquals("<number{Float}>", usage);
    }

    @Test
    public void generateUsageExcludeSourceTest() {
        String usage = TokenManager.generateUsage(Reflect.getMethod(this, "testMethod3"));

        Assertions.assertEquals("", usage);
    }

    private void testMethod1(String first, int second) {}

    private void testMethod2(MessageReceivedEvent event, float number) {}

    private void testMethod3(@Source User author) {}
}
