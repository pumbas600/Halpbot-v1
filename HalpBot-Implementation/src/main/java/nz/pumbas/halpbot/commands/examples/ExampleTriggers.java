package nz.pumbas.halpbot.commands.examples;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.awt.Color;

import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.utilities.Require;
import nz.pumbas.halpbot.triggers.Trigger;
import nz.pumbas.halpbot.triggers.TriggerStrategy;
import nz.pumbas.halpbot.utilities.Duration;

@Service
public class ExampleTriggers
{
    // Repond to any messages that contain 'my id' or 'discord id' anywhere with their id for 30 seconds
    @Trigger(value = { "my id", "discord id" }, strategy = TriggerStrategy.ANYWHERE, display = @Duration(30))
    public MessageEmbed usersId(@Source User user) {
        return new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                .setColor(Color.CYAN)
                .setDescription("Your id is: " + user.getId())
                .build();
    }

    // Respond to any messages that contains all the triggers. Require.ALL automatically sets the strategy to ANYWHERE
    @Trigger(value = {"found", "bug"}, require = Require.ALL)
    public String foundBug() {
        return this.howToReportIssue();
    }

    @Trigger(value = {"how", "report", "issue"}, require = Require.ALL)
    public String howToReportIssue() {
        return "You can report any issues you find here: https://github.com/pumbas600/Halpbot/issues/new/choose";
    }
}
