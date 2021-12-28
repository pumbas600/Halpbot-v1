package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import nz.pumbas.halpbot.bugtesting.DemoFactory;
import nz.pumbas.halpbot.bugtesting.DemoImplementation;
import nz.pumbas.halpbot.bugtesting.DemoInterface;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecorator;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecoratorFactory;
import nz.pumbas.halpbot.commands.actioninvokable.HalpbotCommandInvokable;
import nz.pumbas.halpbot.decorators.log.Log;
import nz.pumbas.halpbot.decorators.log.LogDecorator;
import nz.pumbas.halpbot.decorators.log.LogDecoratorFactory;
import nz.pumbas.halpbot.decorators.log.LogLevel;

@Demo
@Service
@HartshornTest
public class DemoTests
{
    @Test
    public void genericFactoryTest() {
        Exceptional<String> test = this.test(1);
        Assertions.assertTrue(test.present());
        Assertions.assertEquals(1, test.get());
    }

    @SuppressWarnings("unchecked")
    private <T> Exceptional<T> test(Integer test) {
        return Exceptional.of((T) test);
    }

    @InjectTest
    public void inheritedFactoryTest(CooldownDecoratorFactory factory) {
        CooldownDecorator decorator = factory.decorate(null, null);
        Assertions.assertNotNull(decorator);
        Assertions.assertEquals("Cooldown decorator", decorator.toString());
    }

    @Test
    public void genericReturnTypeTest() {
        MethodContext<?, ?> methodContext = TypeContext.of(CooldownDecoratorFactory.class).methods().get(0);
        Assertions.assertEquals(CooldownDecorator.class, methodContext.returnType().type());
        Assertions.assertEquals(CooldownDecorator.class, methodContext.genericReturnType().type());
    }

    @InjectTest
    public void injectionTest(ApplicationContext applicationContext) {
        DemoServiceA serviceA = applicationContext.get(DemoServiceA.class);
        DemoServiceB serviceB = applicationContext.get(DemoServiceB.class);

        Assertions.assertNotNull(serviceA);
        Assertions.assertNotNull(serviceB);
        Assertions.assertEquals(serviceA, serviceB.serviceA());
        Assertions.assertEquals(serviceB, serviceA.serviceB());
    }

    @InjectTest
    public void demoPriortityTest(DemoFactory demoFactory) {
        DemoInterface demo = demoFactory.create("Test");

        Assertions.assertNotNull(demo);
        Assertions.assertInstanceOf(DemoImplementation.class, demo);
        Assertions.assertEquals("Test", demo.name());
    }

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
}
