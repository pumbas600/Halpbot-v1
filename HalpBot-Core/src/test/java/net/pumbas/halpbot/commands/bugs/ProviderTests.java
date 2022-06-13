package net.pumbas.halpbot.commands.bugs;

import net.pumbas.halpbot.buttons.ButtonAdapter;
import net.pumbas.halpbot.buttons.UseButtons;
import net.pumbas.halpbot.permissions.PermissionDecoratorFactory;

import org.dockbox.hartshorn.application.Activator;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@UseButtons
@HartshornTest
@Activator(scanPackages = "net.pumbas.halpbot.commands.bugs")
public class ProviderTests {

    @Inject
    private ApplicationContext applicationContext;

    @Test
    public void testIsSingleton() {
        final ButtonAdapter adapter = this.applicationContext.get(ButtonAdapter.class);
        final ButtonAdapter adapter2 = this.applicationContext.get(ButtonAdapter.class);
        Assertions.assertSame(adapter, adapter2);
    }

    @Test
    public void getDecoratorFactoryTest() {
        final PermissionDecoratorFactory factory = this.applicationContext.get(PermissionDecoratorFactory.class);
        Assertions.assertNotNull(factory);
    }
}
