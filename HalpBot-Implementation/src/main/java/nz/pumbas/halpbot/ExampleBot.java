package nz.pumbas.halpbot;

import com.google.common.io.Files;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.common.Bot;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
@Activator
@UseCommands
public class ExampleBot extends ListenerAdapter implements Bot
{
    @Inject private ApplicationContext applicationContext;

    public static void main(String[] args) throws ApplicationException {
        HalpbotBuilder.build(ExampleBot.class, args);
    }

    @Override
    public void onReady(ReadyEvent event) {
        this.applicationContext.log()
                .info("The bot is initialised and running in %d servers".formatted(event.getGuildTotalCount()));
    }

    @Override
    public JDABuilder initialise() {
        String token = HalpbotUtils.firstLine("Token.txt");
        return JDABuilder.createDefault(token)
                .addEventListeners(this)
                .setActivity(Activity.of(ActivityType.LISTENING, "to how cool Halpbot is!"));
    }

    @Command(description = "Pongs a user")
    public String ping(@Source User user) {
        return "Pong %s".formatted(user.getAsMention());
    }
}
