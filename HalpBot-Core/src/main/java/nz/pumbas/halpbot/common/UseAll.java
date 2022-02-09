package nz.pumbas.halpbot.common;

import org.dockbox.hartshorn.core.annotations.activate.ServiceActivator;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.buttons.UseButtons;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.triggers.UseTriggers;

@Service
@UseButtons
@UseCommands
@UseTriggers
@ServiceActivator(scanPackages = "nz.pumbas.halpbot")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseAll
{
}
