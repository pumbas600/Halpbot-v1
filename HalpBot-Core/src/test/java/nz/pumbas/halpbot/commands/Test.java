package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

@HartshornTest
public class Test
{
    private void testMethod(String arg) {

    }

    @InjectTest
    public void testA(ApplicationContext applicationContext) {
        Assertions.assertTrue(TypeContext.of(Test.class)
                .method("testMethod", String.class)
                .present());
    }

    @InjectTest
    public void testB(ApplicationContext applicationContext) {
        Assertions.assertTrue(TypeContext.of(Test.class)
                .method("testMethod", String.class)
                .present());
    }
}
