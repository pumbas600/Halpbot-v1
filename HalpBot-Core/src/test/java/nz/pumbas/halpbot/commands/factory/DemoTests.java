package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.config.annotations.UseConfigurations;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.ChronoUnit;
import java.util.List;

import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.bugtesting.DemoFactory;
import nz.pumbas.halpbot.bugtesting.DemoImplementation;
import nz.pumbas.halpbot.bugtesting.DemoInterface;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecorator;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecoratorFactory;
import nz.pumbas.halpbot.commands.CommandAdapter;
import nz.pumbas.halpbot.commands.actioninvokable.HalpbotCommandInvokable;
import nz.pumbas.halpbot.utilities.Duration;

@Demo
@Service
@UseConfigurations
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
        CooldownDecorator<?> decorator = factory.decorate(new HalpbotCommandInvokable(null, null), new Cooldown() {
            @Override
            public Duration duration() {
                return new Duration() {
                    @Override
                    public long value() {
                        return 10;
                    }

                    @Override
                    public ChronoUnit unit() {
                        return ChronoUnit.SECONDS;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Duration.class;
                    }
                };
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Cooldown.class;
            }
        });
        Assertions.assertNotNull(decorator);
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
//        Assertions.assertEquals(0, serviceA.id());
//        Assertions.assertEquals(0, serviceB.serviceA().id());
//        Assertions.assertEquals(serviceA, serviceB.serviceA());
//        Assertions.assertEquals(serviceB, serviceA.serviceB());
    }

    @InjectTest
    public void proxyEqualityTest(ApplicationContext applicationContext) {
        DemoServiceA serviceA1 = applicationContext.get(DemoServiceA.class);
        DemoServiceA serviceA2 = applicationContext.environment().manager().handler(serviceA1).get().instance().get();

        Assertions.assertEquals(serviceA1, serviceA2);
    }

    @InjectTest
    public void nonNullInjectionsTest(CommandAdapter commandAdapter) {
        Assertions.assertNotNull(commandAdapter);
        Assertions.assertNotNull(commandAdapter.applicationContext());
        Assertions.assertNotNull(commandAdapter.parameterAnnotationService());
    }

    @InjectTest
    public void demoPriortityTest(DemoFactory demoFactory) {
        DemoInterface demo = demoFactory.create("Test");

        Assertions.assertNotNull(demo);
        Assertions.assertInstanceOf(DemoImplementation.class, demo);
        Assertions.assertEquals("Test", demo.name());
    }

    @Test
    public void genericTypeTests() {
        ParameterContext<?> parameter = TypeContext.of(this).method("genericTestMethod", List.class)
                .get()
                .parameters()
                .get(0);

        TypeContext<?> genericType = parameter.genericType();

        Assertions.assertTrue(genericType.is(List.class));
        Assertions.assertEquals(1, genericType.typeParameters().size());

        genericType = genericType.typeParameters().get(0);
        Assertions.assertTrue(genericType.is(List.class));
        Assertions.assertEquals(1, genericType.typeParameters().size());

        genericType = genericType.typeParameters().get(0);
        Assertions.assertTrue(genericType.is(String.class));
        Assertions.assertEquals(0, genericType.typeParameters().size());
    }

    public void genericTestMethod(List<List<String>> nestedGeneric) { }

    @Test
    public void genericTypeContextTest() throws NoSuchMethodException {
        Parameter parameter = DemoTests.class.getDeclaredMethod("genericTestMethod", List.class)
                .getParameters()[0];
        Type type = parameter.getParameterizedType();

        Assertions.assertInstanceOf(ParameterizedType.class, type);
        final ParameterizedType parameterizedType = (ParameterizedType) type;

        TypeContext<?> genericType = TypeContext.of(parameterizedType);
        Assertions.assertTrue(genericType.is(List.class));
        Assertions.assertEquals(1, genericType.typeParameters().size());
    }
}
