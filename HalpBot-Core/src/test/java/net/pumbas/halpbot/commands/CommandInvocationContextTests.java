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

import net.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;

import org.dockbox.hartshorn.application.Activator;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.data.annotations.UseConfigurations;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

@HartshornTest
@UseConfigurations
@Activator(scanPackages = "net.pumbas.halpbot")
public class CommandInvocationContextTests
{

    @InjectTest
    public void getNextSurroundedTest(InvocationContextFactory invocationContextFactory) {
        CommandInvocationContext invocationContext = invocationContextFactory.command("[1 2 3] [#Block[1 2 3] #Block[2 3 4]]");

        Result<String> oArray1 = invocationContext.nextSurrounded("[", "]");
        Result<String> oArray2 = invocationContext.nextSurrounded("[", "]");
        Result<String> oArray3 = invocationContext.nextSurrounded("[", "]");

        Assertions.assertTrue(oArray1.present());
        Assertions.assertTrue(oArray2.present());
        Assertions.assertFalse(oArray3.present());

        Assertions.assertEquals("1 2 3", oArray1.get());
        Assertions.assertEquals("#Block[1 2 3] #Block[2 3 4]", oArray2.get());
    }

    @InjectTest
    public void getNextSurroundedIncompleteTest(InvocationContextFactory invocationContextFactory) {
        CommandInvocationContext invocationContext1 = invocationContextFactory.command("[1 2 3] [#Block[1 2 3] #Block[2 3 4] ");
        CommandInvocationContext invocationContext2 = invocationContextFactory.command("#Block[1 2 3]]");

        Result<String> oArray1 = invocationContext1.nextSurrounded("[", "]");
        Result<String> oArray2 = invocationContext1.nextSurrounded("[", "]");
        Result<String> oArray3 = invocationContext2.nextSurrounded("[", "]");

        Assertions.assertTrue(oArray1.present());
        Assertions.assertFalse(oArray2.present());
        Assertions.assertFalse(oArray3.present());

        Assertions.assertEquals("1 2 3", oArray1.get());
    }

    @InjectTest
    public void getNextSurroundedStepPastTest(InvocationContextFactory invocationContextFactory) {
        CommandInvocationContext invocationContext = invocationContextFactory.command("#Block[1 2 3]");

        Result<String> oType = invocationContext.nextSurrounded("#", "[", false);
        Result<String> oParameters = invocationContext.nextSurrounded("[", "]");

        Assertions.assertTrue(oType.present());
        Assertions.assertTrue(oParameters.present());
        Assertions.assertFalse(invocationContext.hasNext());

        Assertions.assertEquals("Block", oType.get());
        Assertions.assertEquals("1 2 3", oParameters.get());
    }
}
