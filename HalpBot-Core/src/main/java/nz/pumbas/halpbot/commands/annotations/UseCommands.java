package nz.pumbas.halpbot.commands.annotations;

import org.dockbox.hartshorn.core.annotations.service.ServiceActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.commandadapters.HalpbotCommandAdapter;

@ServiceActivator
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseCommands
{
    Class<? extends CommandAdapter> adapter() default HalpbotCommandAdapter.class;
}
