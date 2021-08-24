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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;

import nz.pumbas.halpbot.commands.annotations.Children;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.commands.tokens.context.ContextState;
import nz.pumbas.halpbot.commands.tokens.context.InvocationContext;
import nz.pumbas.halpbot.commands.annotations.Implicit;
import nz.pumbas.halpbot.objects.Exceptional;

public class InvocationContextTests
{

    @Test
    public void getNextSurroundedTest() {
        InvocationContext tokenInfo = InvocationContext.of("[1 2 3] [#Block[1 2 3] #Block[2 3 4]]");

        Exceptional<String> oArray1 = tokenInfo.getNextSurrounded("[", "]");
        Exceptional<String> oArray2 = tokenInfo.getNextSurrounded("[", "]");
        Exceptional<String> oArray3 = tokenInfo.getNextSurrounded("[", "]");

        Assertions.assertTrue(oArray1.present());
        Assertions.assertTrue(oArray2.present());
        Assertions.assertFalse(oArray3.present());

        Assertions.assertEquals("1 2 3", oArray1.get());
        Assertions.assertEquals("#Block[1 2 3] #Block[2 3 4]", oArray2.get());
    }

    @Test
    public void getNextSurroundedIncompleteTest() {
        InvocationContext tokenInfo1 = InvocationContext.of("[1 2 3] [#Block[1 2 3] #Block[2 3 4] ");
        InvocationContext tokenInfo2 = InvocationContext.of("#Block[1 2 3]]");

        Exceptional<String> oArray1 = tokenInfo1.getNextSurrounded("[", "]");
        Exceptional<String> oArray2 = tokenInfo1.getNextSurrounded("[", "]");
        Exceptional<String> oArray3 = tokenInfo2.getNextSurrounded("[", "]");

        Assertions.assertTrue(oArray1.present());
        Assertions.assertFalse(oArray2.present());
        Assertions.assertFalse(oArray3.present());

        Assertions.assertEquals("1 2 3", oArray1.get());
    }

    @Test
    public void getNextSurroundedStepPastTest() {
        InvocationContext tokenInfo = InvocationContext.of("#Block[1 2 3]");

        Exceptional<String> oType = tokenInfo.getNextSurrounded("#", "[", false);
        Exceptional<String> oParameters = tokenInfo.getNextSurrounded("[", "]");

        Assertions.assertTrue(oType.present());
        Assertions.assertTrue(oParameters.present());
        Assertions.assertFalse(tokenInfo.hasNext());

        Assertions.assertEquals("Block", oType.get());
        Assertions.assertEquals("1 2 3", oParameters.get());
    }

    @Test
    public void contextStateCopyTest() {
        ContextState contextState = new ContextState(Integer.class, new Annotation[0], new ArrayList<>(
            Arrays.asList(Unrequired.class, Children.class, Implicit.class)));

        ContextState copy = contextState.copyContextState();
        contextState.setType(Float.class);
        contextState.getAnnotationTypes().remove(Children.class);

        Assertions.assertFalse(contextState.getAnnotationTypes().contains(Children.class));
        Assertions.assertTrue(copy.getAnnotationTypes().contains(Children.class));

        Assertions.assertEquals(Float.class, contextState.getType());
        Assertions.assertEquals(Integer.class, copy.getType());

    }
}
