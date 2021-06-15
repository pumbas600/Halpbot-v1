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
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.halpbot.customparameters.Shape;
import nz.pumbas.halpbot.customparameters.Vector3;
import nz.pumbas.utilities.Reflect;

public class TokenCommandTests
{
    @Test
    public void tokenCommandMatchesTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Assertions.assertTrue(tokenCommand.matches(InvocationTokenInfo.of("1 [2 3 4 1]")));
        Assertions.assertTrue(tokenCommand.matches(InvocationTokenInfo.of("2")));
        Assertions.assertFalse(tokenCommand.matches(InvocationTokenInfo.of("2 [1 a 2]")));
        Assertions.assertFalse(tokenCommand.matches(InvocationTokenInfo.of("abc [1 3 2]")));
        Assertions.assertFalse(tokenCommand.matches(InvocationTokenInfo.of("2 agf")));
    }

    @Test
    public void simpleTokenCommandInvokeTest() {
        TokenCommand tokenCommand = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "containedWithinArrayTestMethod"));

        Optional<Object> result1 = tokenCommand.invoke(InvocationTokenInfo.of("1 [2 1 4 3]"),
            null, null);
        Optional<Object> result2 = tokenCommand.invoke(InvocationTokenInfo.of("2 [9 5 4 3]"),
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

        Optional<Object> result1 = tokenCommand.invoke(InvocationTokenInfo.of("1"),
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

        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("2 [1 3 3]")));
        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("3")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("alpha")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("2 [1 4 c]")));

        Optional<Object> result = command.invoke(InvocationTokenInfo.of("2 [1 2 3]"),
            null, null);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue((boolean) result.get());

    }

    @Test
    public void customObjectTokenCommandTest() {
        TokenCommand command = TokenManager.generateTokenCommand(this,
            Reflect.getMethod(this, "customObjectTokenCommandMethodTest"));

        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("#Vector3[1 2 3]")));
        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("#Vector3[3 1]")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("Vector3[3 1]")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("[3 1 2]")));

        Optional<Object> result = command.invoke(InvocationTokenInfo.of("#Vector3[1 2 3]"),
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

        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("2 3 2 1 4 Heyo")));
        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("2 3 Hi")));
        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("2 [2 3 8] Hi")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("2 Hi")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("a 1 2 Hi")));

        Optional<Object> result = command.invoke(InvocationTokenInfo.of("2 3 2 1 4 Heyo"),
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

        Assertions.assertTrue(command.matches(InvocationTokenInfo.of(
            "#Shape[Rectangle 200 50 100 25] #Shape[Rectangle 50 200 25 150] #Shape[Rectangle 200 50 100 275]")));
        Assertions.assertTrue(command.matches(InvocationTokenInfo.of(
            "#Shape[Rectangle 200 50 100 25]")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("")));

        Optional<Object> result = command.invoke(InvocationTokenInfo.of("#Shape[Rectangle 200 50 100 25] " +
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

        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("")));
        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("Hi")));
        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("-1")));
        Assertions.assertFalse(command.matches(InvocationTokenInfo.of("a -1")));

        Optional<Object> oResult = command.invoke(InvocationTokenInfo.of(""), null, null);
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
        Assertions.assertTrue(command.matches(InvocationTokenInfo.of("")));
    }

    @Command(alias = "test")
    private void commandWithMessageReceivedEventParameterMethodTest(MessageReceivedEvent event) {

    }

}
