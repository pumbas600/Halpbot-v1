package nz.pumbas.halpbot.commands.annotations;

import org.dockbox.hartshorn.config.annotations.UseConfigurations;
import org.dockbox.hartshorn.core.annotations.service.ServiceActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@UseConfigurations
@ServiceActivator(scanPackages = "nz.pumbas.halpbot")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseCommands
{
}
