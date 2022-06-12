package net.pumbas.halpbot.commands.bugs;

import org.dockbox.hartshorn.application.Activator;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@HartshornTest
@Activator(scanPackages = "net.pumbas.halpbot.commands.bugs")
public class FactoryTest {

    @Inject
    private HandlerFactory factory;

    @Test
    public void testFactoryIsInjected() {
        Assertions.assertNotNull(this.factory);
    }

    @Test
    public void testFactoryCreatesHandler() {
        final Handler handler = this.factory.create("Handler");
        Assertions.assertNotNull(handler);
        Assertions.assertEquals("Handler", handler.name());
        Assertions.assertInstanceOf(HalpbotHandler.class, handler);
    }
}
