package nz.pumbas.halpbot;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

import java.lang.annotation.Annotation;

import nz.pumbas.halpbot.bugtesting.DemoFactory;
import nz.pumbas.halpbot.bugtesting.DemoInterface;
import nz.pumbas.halpbot.commands.actioninvokable.HalpbotCommandInvokable;
import nz.pumbas.halpbot.decorators.log.Log;
import nz.pumbas.halpbot.decorators.log.LogDecorator;
import nz.pumbas.halpbot.decorators.log.LogDecoratorFactory;
import nz.pumbas.halpbot.utilities.LogLevel;

@Activator(scanPackages = "nz.pumbas.halpbot")
@HartshornTest
public class Tests
{
    @InjectTest
    public void logPriortityTest(LogDecoratorFactory factory) {
        LogDecorator<?> decorator = factory.decorate(new HalpbotCommandInvokable(null, null),
                new Log() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Log.class;
                    }

                    @Override
                    public LogLevel value() {
                        return LogLevel.DEBUG;
                    }
                });

        Assertions.assertNotNull(decorator);
        Assertions.assertInstanceOf(CustomLogDecorator.class, decorator);
    }

    @InjectTest
    public void demoPriortityTest(DemoFactory demoFactory) {
        DemoInterface demo = demoFactory.create("Test");

        Assertions.assertNotNull(demo);
        Assertions.assertInstanceOf(DemoPriority.class, demo);
        Assertions.assertEquals("Test", demo.name());
    }
}
