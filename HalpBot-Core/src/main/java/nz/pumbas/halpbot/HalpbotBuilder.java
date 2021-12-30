package nz.pumbas.halpbot;

import net.dv8tion.jda.api.JDABuilder;

import org.dockbox.hartshorn.core.Modifiers;
import org.dockbox.hartshorn.core.boot.HartshornApplication;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.common.Bot;
import nz.pumbas.halpbot.utilities.ErrorManager;

public final class HalpbotBuilder
{
    private HalpbotBuilder() {}

    public static ApplicationContext build(Class<? extends Bot> bot, String[] args, Modifiers... modifiers)
            throws ApplicationException
    {
        ApplicationContext applicationContext = HartshornApplication.create(bot, args, modifiers);
        applicationContext.get(ErrorManager.class); // Create an instance of the ErrorManager
        HalpbotCore halpbotCore = applicationContext.get(HalpbotCore.class);
        Bot botInstance = applicationContext.get(bot);

        JDABuilder builder = botInstance.initialise();
        halpbotCore.build(builder);
        botInstance.onCreation(applicationContext, halpbotCore);

        return applicationContext;
    }
}
