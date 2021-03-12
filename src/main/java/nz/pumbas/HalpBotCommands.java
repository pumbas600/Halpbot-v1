package nz.pumbas;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.utilities.Vector2;

@CommandGroup(defaultPrefix = "$")
public class HalpBotCommands
{

    @Command(alias = "halp")
    public void onHalp(MessageReceivedEvent event) {
        event.getChannel().sendMessage("I will try my very best!").queue();
    }

    @Command(alias = "source")
    public void onSource(MessageReceivedEvent event) {
        event.getChannel().sendMessage("You can see the source code for me here: " +
                "https://github.com/pumbas600/HalpBot :kissing_heart:").queue();
    }

    @Command(alias = "components", command = "FLOATn?N? (?:at)? ?FLOAT (?:from)? ?(x|x-axis|y|y-axis)",
             help = "Splits a force into its x and y components:\n" +
                    "magnitude(N) [at] angle(degrees) [from] x|x-axis|y|y-axis")
    public void onComponents(MessageReceivedEvent event, float magnitude, float angle, String axis) {
        boolean fromX = "x".equals(axis) || "x-axis".equals(axis);
        Vector2 force = new Vector2(magnitude, angle, fromX);

        event.getChannel().sendMessage(force.toString()).queue();
    }
}
