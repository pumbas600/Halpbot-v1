package nz.pumbas.halpbot;

import net.dv8tion.jda.api.JDABuilder;

import org.dockbox.hartshorn.core.Modifier;
import org.dockbox.hartshorn.core.boot.HartshornApplication;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.common.Bot;

public final class HalpbotBuilder
{
    private HalpbotBuilder() {}

    public static ApplicationContext build(Class<? extends Bot> bot, String[] args, Modifier... modifiers)
            throws ApplicationException
    {
        ApplicationContext applicationContext = HartshornApplication.create(bot, args, modifiers);
        HalpbotCore halpbotCore = applicationContext.get(HalpbotCore.class);
        Bot botInstance = applicationContext.get(bot);

        JDABuilder builder = botInstance.initialise();
        halpbotCore.build(builder);
        botInstance.onCreation(applicationContext, halpbotCore);

        return applicationContext;
    }
}
