package net.pumbas.halpbot.common;

import org.dockbox.hartshorn.component.processing.ServiceActivator;
import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.inject.binding.InjectConfig;
import org.dockbox.hartshorn.util.reflect.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Extends(Service.class)
@ServiceActivator(scanPackages = "net.pumbas.halpbot",
                  processors = { EventListenerServicePreProcessor.class })
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bot
{
    boolean includeBasePackage() default true;

    String[] scanPackages() default {};

    InjectConfig[] configs() default {};
}
