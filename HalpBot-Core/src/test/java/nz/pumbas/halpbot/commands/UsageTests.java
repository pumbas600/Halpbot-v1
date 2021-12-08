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

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;

@UseCommands
@HartshornTest
@Activator(scanPackages = "nz.pumbas.halpbot")
public class UsageTests
{
    private static final TypeContext<UsageTests> TYPE = TypeContext.of(UsageTests.class);

    @InjectTest
    public void generateUsageTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TYPE.method("testMethod1", String.class, int.class).get());

        Assertions.assertEquals("<string> <integer>", usage);
    }

    @InjectTest
    public void generateUsageExcludeEventTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TYPE.method("testMethod2", MessageReceivedEvent.class, float.class).get());

        Assertions.assertEquals("<float>", usage);
    }

    @InjectTest
    public void generateUsageExcludeSourceTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TYPE.method("testMethod3", User.class).get());

        Assertions.assertEquals("", usage);
    }

    public void testMethod1(String first, int second) {}

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
