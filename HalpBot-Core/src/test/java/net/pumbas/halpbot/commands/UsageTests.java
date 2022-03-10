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

package net.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.pumbas.halpbot.commands.annotations.UseCommands;
import net.pumbas.halpbot.commands.usage.NameUsageBuilder;
import net.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import net.pumbas.halpbot.commands.usage.UsageBuilder;
import net.pumbas.halpbot.converters.annotations.parameter.Source;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

@UseCommands
@HartshornTest
public class UsageTests
{
    @InjectTest
    public void generateTypeUsageTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TestUtil.method(UsageTests.class, "testMethod1"));

        Assertions.assertEquals("<String> <Integer>", usage);
    }

    @InjectTest
    public void generateTypeUsageExcludeEventTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TestUtil.method(UsageTests.class, "testMethod2"));

        Assertions.assertEquals("<Float>", usage);
    }

    @InjectTest
    public void generateTypeUsageExcludeSourceTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TestUtil.method(UsageTests.class, "testMethod3"));

        Assertions.assertEquals("", usage);
    }

    @InjectTest
    public void generateTypeUsageWithOptionalParameterTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new TypeUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TestUtil.method(UsageTests.class, "testMethod4"));

        Assertions.assertEquals("[User]", usage);
    }

    @InjectTest
    public void generateNameUsageTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new NameUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TestUtil.method(UsageTests.class, "testMethod1"));

        Assertions.assertEquals("<first> <second>", usage);
    }

    @InjectTest
    public void generateNameUsageExcludeEventTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new NameUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TestUtil.method(UsageTests.class, "testMethod2"));

        Assertions.assertEquals("<number>", usage);
    }

    @InjectTest
    public void generateNameUsageExcludeSourceTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new NameUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TestUtil.method(UsageTests.class, "testMethod3"));

        Assertions.assertEquals("", usage);
    }

    @InjectTest
    public void generateNameUsageWithOptionalParameterTest(ApplicationContext applicationContext) {
        UsageBuilder usageBuilder = new NameUsageBuilder();
        String usage = usageBuilder.buildUsage(
                applicationContext,
                TestUtil.method(UsageTests.class, "testMethod4"));

        Assertions.assertEquals("[user]", usage);
    }

    private void testMethod1(String first, int second) {}

    private void testMethod2(MessageReceivedEvent event, float number) {}

    private void testMethod3(@Source User author) {}

    private void testMethod4(@Unrequired User user) {}
}
