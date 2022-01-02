package nz.pumbas.halpbot;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.testsuite.HartshornTest;

@Activator(scanPackages = "nz.pumbas.halpbot")
@HartshornTest
public class Tests
{
//    @InjectTest
//    public void logPriortityTest(LogDecoratorFactory factory) {
//        LogDecorator<?> decorator = factory.decorate(new HalpbotCommandInvokable(null, null),
//                new Log() {
//                    @Override
//                    public Class<? extends Annotation> annotationType() {
//                        return Log.class;
//                    }
//
//                    @Override
//                    public LogLevel value() {
//                        return LogLevel.DEBUG;
//                    }
//                });
//
//        Assertions.assertNotNull(decorator);
//        Assertions.assertInstanceOf(CustomLogDecorator.class, decorator);
//    }
}
