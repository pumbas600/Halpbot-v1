package nz.pumbas.halpbot.common;

import net.dv8tion.jda.api.JDABuilder;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.HalpbotCore;

public interface Bot
{
    JDABuilder initialise() throws ApplicationException;

    default void onCreation(ApplicationContext applicationContext, HalpbotCore halpbotCore) {}
}
