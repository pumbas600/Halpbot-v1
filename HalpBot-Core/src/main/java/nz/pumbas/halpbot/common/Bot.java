package nz.pumbas.halpbot.common;

import org.dockbox.hartshorn.core.annotations.inject.InjectConfig;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Service
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bot
{
    boolean includeBasePackage() default true;

    String[] scanPackages() default {};

    InjectConfig[] configs() default {};
}
