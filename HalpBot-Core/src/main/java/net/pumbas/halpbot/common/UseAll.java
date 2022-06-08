package net.pumbas.halpbot.common;

import net.pumbas.halpbot.buttons.UseButtons;
import net.pumbas.halpbot.commands.annotations.UseCommands;
import net.pumbas.halpbot.triggers.UseTriggers;

import org.dockbox.hartshorn.component.processing.ServiceActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@UseButtons
@UseCommands
@UseTriggers
@ServiceActivator(scanPackages = "net.pumbas.halpbot")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseAll
{
}
