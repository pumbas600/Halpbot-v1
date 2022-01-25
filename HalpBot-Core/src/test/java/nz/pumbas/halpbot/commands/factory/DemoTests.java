package nz.pumbas.halpbot.commands.factory;

import net.dv8tion.jda.api.Permission;

import org.dockbox.hartshorn.config.annotations.UseConfigurations;
import org.dockbox.hartshorn.core.annotations.activate.Activator;
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

import nz.pumbas.halpbot.actions.cooldowns.Cooldown;
import nz.pumbas.halpbot.actions.cooldowns.CooldownType;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecorator;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecoratorFactory;
import nz.pumbas.halpbot.commands.CommandAdapter;
import nz.pumbas.halpbot.commands.TestService;
import nz.pumbas.halpbot.commands.actioninvokable.HalpbotCommandInvokable;
import nz.pumbas.halpbot.permissions.PermissionDecoratorFactory;
import nz.pumbas.halpbot.utilities.Require;
import nz.pumbas.halpbot.permissions.PermissionService;
import nz.pumbas.halpbot.permissions.Permissions;
import nz.pumbas.halpbot.utilities.Duration;

@Demo
@Service
@UseConfigurations
@Activator(scanPackages = "nz.pumbas.halpbot")
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
        CooldownDecorator<?> decorator = factory.decorate(new HalpbotCommandInvokable(null, null), new TestCooldown());
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
    public void permissionService(PermissionService permissionService) {
        Assertions.assertNotNull(permissionService);
    }

    @InjectTest
    public void permissionDecoratorTest(PermissionDecoratorFactory factory) {
        ActionInvokable<?> actionInvokable = new HalpbotCommandInvokable(null, null);
        ActionInvokable<?> permissionDecorator = factory.decorate(actionInvokable, new TestPermissions());

        Assertions.assertNotNull(permissionDecorator);
    }

    @InjectTest
    public void permissionDecorator(DemoServiceA<?> serviceA) {
        Assertions.assertNotNull(serviceA);
        Assertions.assertNotNull(serviceA.permissionService());
    }

    @InjectTest
    public void proxyThisEquality(TestService testService) {
        Assertions.assertEquals(testService, testService);
        Assertions.assertTrue(testService.test(testService));
    }

    @InjectTest
    public void proxyEquality(TestService testService1, TestService testService2) {
        Assertions.assertEquals(testService1, testService2);
    }

    private static class TestCooldown implements Cooldown {

        @Override
        public Duration duration() {
            return new TestDuration();
        }

        @Override
        public CooldownType type() {
            return CooldownType.MEMBER;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Cooldown.class;
        }
    }

    private static class TestDuration implements Duration {

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
    }

    private static class TestPermissions implements Permissions
    {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Permissions.class;
        }

        @Override
        public String[] permissions() {
            return new String[] { "halpbot.example.test" };
        }

        @Override
        public Permission[] user() {
            return new Permission[0];
        }

        @Override
        public Permission[] self() {
            return new Permission[0];
        }

        @Override
        public Require merger() {
            return Require.ALL;
        }
    }
}
