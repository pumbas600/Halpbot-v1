package nz.pumbas.halpbot.common.annotations;

import org.dockbox.hartshorn.core.annotations.service.ServiceActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration;

@ServiceActivator(scanPackages = "nz.pumbas.halpbot")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bot
{
    Class<? extends DisplayConfiguration> displayConfiguration() default SimpleDisplayConfiguration.class;
}
