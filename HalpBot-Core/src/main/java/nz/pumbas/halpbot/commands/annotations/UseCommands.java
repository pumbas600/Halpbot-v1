package nz.pumbas.halpbot.commands.annotations;

import org.dockbox.hartshorn.core.annotations.service.ServiceActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.commandadapters.HalpbotCommandAdapter;

@ServiceActivator
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseCommands
{
    /**
     * Note this {@link CommandAdapter} should also extend {@link HalpbotAdapter}. If you don't this will cause an
     * error to be thrown when It's attempted to be registered.
     */
    Class<? extends CommandAdapter> adapter() default HalpbotCommandAdapter.class;
}
