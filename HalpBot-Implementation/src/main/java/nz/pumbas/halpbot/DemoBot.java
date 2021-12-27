package nz.pumbas.halpbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.common.Bot;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

// Indicates that the bot is a singleton and allows this class to be scanned for commands, etc
@Service

// Indicates that this class is an application activator
@Activator

// Activates the CommandAdapter ServicePreProcessors which scan for commands, converters, etc
@UseCommands
public class DemoBot implements Bot
{
    public static void main(String[] args) throws ApplicationException {
        /*
         * This starts up Hartshorn and automatically creates an instance of this class.
         *
         * It then calls initialise() so that you can configure the JDABuilder. Internally it uses this to add
         * the HalpbotAdapters as event listeners before building the JDA instance.
         *
         * Finally, onCreation(ApplicationContext, HalpbotCore) is called, which can be optionally implemented if
         * post-initialisation is required.
         */
        HalpbotBuilder.build(DemoBot.class, args);
    }

    @Override
    public JDABuilder initialise() {
        // Retrieve the discord token
        String token = HalpbotUtils.firstLine("Token.txt");

        // Create and configure the JDABuilder here
        return JDABuilder.createDefault(token)
                .setActivity(Activity.of(ActivityType.LISTENING, "to how cool Halpbot is!"));
    }
}
