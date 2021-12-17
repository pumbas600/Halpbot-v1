package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

@Demo
@Service
@HartshornTest
@Activator(scanPackages = "nz.pumbas.halpbot")
public class DemoTests
{
    @InjectTest
    public void someTest(ApplicationContext applicationContext) {
        // The PersonServicePreProcessorShould process this class twice:

        Assertions.assertNotNull(applicationContext);
    }
}
