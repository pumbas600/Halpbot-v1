package nz.pumbas.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;

import nz.pumbas.commands.annotations.Children;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.tokens.context.ContextState;
import nz.pumbas.commands.tokens.context.InvocationContext;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.utilities.Exceptional;

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
        contextState.type(Float.class);
        contextState.annotationTypes().remove(Children.class);

        Assertions.assertFalse(contextState.annotationTypes().contains(Children.class));
        Assertions.assertTrue(copy.annotationTypes().contains(Children.class));

        Assertions.assertEquals(Float.class, contextState.type());
        Assertions.assertEquals(Integer.class, copy.type());

    }
}
