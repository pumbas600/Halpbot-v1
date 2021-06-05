package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.CommandGroup;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.exceptions.UnimplementedFeatureException;
import nz.pumbas.halpbot.customparameters.Shape;
import nz.pumbas.halpbot.customparameters.Vector2;

@CommandGroup(defaultPrefix = "$")
public class HalpBotCommands
{

    @Command(alias = "halp")
    public void onHalp(MessageReceivedEvent event)
    {
        event.getChannel().sendMessage("I will try my very best!").queue();
    }

    @Command(alias = "source")
    public void onSource(MessageReceivedEvent event)
    {
        event.getChannel().sendMessage("You can see the source code for me here: " +
            "https://github.com/pumbas600/HalpBot :kissing_heart:").queue();
    }

    @Command(alias = "suggestion")
    public void onSuggestion(MessageReceivedEvent event)
    {
        event.getChannel().sendMessage("You can note issues and suggestions for me here: " +
            "https://github.com/pumbas600/HalpBot/issues").queue();
    }

    @Command(alias = "components", command = "DOUBLE<WORD> <at> DOUBLE <from> (x|y)",
             description = "Splits a force into its x and y components")
    public void onComponents(MessageReceivedEvent event, double magnitude, @Unrequired("N") String units, double angle,
                             String axis)
    {
        boolean fromX = "x".equals(axis) || "x-axis".equals(axis);
        Vector2 force = new Vector2(magnitude, angle, fromX, units);

        event.getChannel().sendMessage(force.toString()).queue();
    }

    @Command(alias = "ping")
    public void onPing()
    {
        throw new UnimplementedFeatureException("This is still a work in progress, we'll try and get it finished as soon as possible!");
    }

    @Command(alias = "unrequired", description = "Tests unrequired annotation")
    public void onOptional(MessageReceivedEvent event,
                           @Unrequired("No value passed") String a,
                           @Unrequired("No value passed") String b)
    {
        event.getChannel().sendMessage(a + " and " + b).queue();
    }
}
