package nz.pumbas;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;

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

    @Command(alias = "components", command = "^([0-9]+)n?N? (?:from)? ?(x|x-axis|y|y-axis)",
             help = "Splits a force into its x and y components:\n" +
                    "magnitude [from] x|x-axis|y|y-axis")
    public void onComponents(MessageReceivedEvent event, int magnitude, String axis) {
        boolean fromX = "x".equals(axis) || "x-axis".equals(axis);
        
        event.getChannel().sendMessage(magnitude + " From X:" + fromX);
    }
}
