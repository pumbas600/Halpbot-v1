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

import org.dockbox.hartshorn.config.annotations.UseConfigurations;
import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.activate.UseServiceProvision;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.data.annotations.UsePersistence;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.commands.usage.NameUsageBuilder;
import nz.pumbas.halpbot.configurations.BotConfiguration;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;

@UseCommands
@UseConfigurations
@Activator(scanPackages = "nz.pumbas.halpbot")
@HartshornTest
public class UsageTests
{
    private MethodContext<?, ?> method(String name) {
        return TypeContext.of(UsageTests.class).methods()
                .stream()
                .filter(method -> method.name().equals(name))
                .findFirst()
                .get();
    }

    @InjectTest
    public void test(ApplicationContext applicationContext) {
        BotConfiguration config = applicationContext.get(BotConfiguration.class);
        Assertions.assertEquals("$", config.defaultPrefix());
    }

    @InjectTest
    public void generateTypeUsageTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                this.method("testMethod1"));

        Assertions.assertEquals("<String> <Integer>", usage);
    }

    @InjectTest
    public void generateTypeUsageExcludeEventTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                this.method("testMethod2"));

        Assertions.assertEquals("<Float>", usage);
    }

    @InjectTest
    public void generateTypeUsageExcludeSourceTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                this.method("testMethod3"));

        Assertions.assertEquals("", usage);
    }

    @InjectTest
    public void generateTypeUsageWithOptionalParameterTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                this.method("testMethod4"));

        Assertions.assertEquals("[User]", usage);
    }

    @InjectTest
    public void generateNameUsageTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new NameUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                this.method("testMethod1"));

        Assertions.assertEquals("<first> <second>", usage);
    }

    @InjectTest
    public void generateNameUsageExcludeEventTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new NameUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                this.method("testMethod2"));

        Assertions.assertEquals("<number>", usage);
    }

    @InjectTest
    public void generateNameUsageExcludeSourceTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new NameUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                this.method("testMethod3"));

        Assertions.assertEquals("", usage);
    }

    @InjectTest
    public void generateNameUsageWithOptionalParameterTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new NameUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                this.method("testMethod4"));

        Assertions.assertEquals("[user]", usage);
    }

    private void testMethod1(String first, int second) {}

    private void testMethod2(MessageReceivedEvent event, float number) {}

    private void testMethod3(@Source User author) {}

    private void testMethod4(@Unrequired User user) {}

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
