package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.config.annotations.UseConfigurations;
import org.dockbox.hartshorn.core.annotations.activate.ServiceActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@UseConfigurations
@ServiceActivator(scanPackages = "nz.pumbas.halpbot")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseButtons
{
}
