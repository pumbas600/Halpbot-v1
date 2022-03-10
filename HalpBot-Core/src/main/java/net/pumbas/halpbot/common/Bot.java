package net.pumbas.halpbot.common;

import org.dockbox.hartshorn.core.annotations.Extends;
import org.dockbox.hartshorn.core.annotations.activate.ServiceActivator;
import org.dockbox.hartshorn.core.annotations.inject.InjectConfig;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Extends(Service.class)
@ServiceActivator(scanPackages = "net.pumbas.halpbot")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bot
{
    boolean includeBasePackage() default true;

    String[] scanPackages() default {};

    InjectConfig[] configs() default {};
}
