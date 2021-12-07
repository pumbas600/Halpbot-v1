/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.commands.commandmethods.SimpleCommand;
import nz.pumbas.halpbot.textfixtures.HartshornTest;
import nz.pumbas.halpbot.utilities.Reflect;

@HartshornTest
public class UsageTests
{
    private static final TypeContext<UsageTests> TYPE = TypeContext.of(UsageTests.class);
    @Inject private ApplicationContext applicationContext;

    @Test
    public void generateUsageTest() {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(this.applicationContext,
                TYPE.method("testMethod1").get());

        Assertions.assertEquals("<String> <Integer>", usage);
    }

    @Test
    public void generateUsageExcludeEventTest() {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(this.applicationContext,
                TYPE.method("testMethod2").get());

        Assertions.assertEquals("<Float>", usage);
    }

    @Test
    public void generateUsageExcludeSourceTest() {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(this.applicationContext,
                TYPE.method("testMethod3").get());

        Assertions.assertEquals("", usage);
    }

    private void testMethod1(String first, int second) {}

    private void testMethod2(MessageReceivedEvent event, float number) {}

    private void testMethod3(@Source User author) {}

//    @Test
//    public void commandUsesMethodNameIfAliasUndefinedTest() {
//        Method method = Reflect.getMethod(this, "add");
//
//        SimpleCommand command = CommandManager.generateCommandMethod(this, method, method.getAnnotation(Command.class));
//        Assertions.assertEquals("add", command.alias());
//    }
//
//    @Command(description = "Adds two numbers together")
//    private int add(int a, int b) {
//        return a + b;
//    }
}
