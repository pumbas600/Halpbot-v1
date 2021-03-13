package nz.pumbas;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Exceptions.UnimplementedFeatureException;
import nz.pumbas.customparameters.Shape;
import nz.pumbas.utilities.Vector2;

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

    @Command(alias = "components", command = "DOUBLE<n|N> <at> DOUBLE <from> (x|y)",
             description = "Splits a force into its x and y components:\n" +
                 "magnitude(N) <at> angle(degrees) <from> x|y")
    public void onComponents(MessageReceivedEvent event, double magnitude, double angle, String axis)
    {
        boolean fromX = "x".equals(axis) || "x-axis".equals(axis);
        Vector2 force = new Vector2(magnitude, angle, fromX);

        event.getChannel().sendMessage(force.toString()).queue();
    }

    @Command(alias = "shape", command = "SHAPE",
             description = "Creates a shape object from a name and a number of sides")
    public void onShape(MessageReceivedEvent event, Shape shape)
    {
        event.getChannel()
            .sendMessage("You defined the shape " + shape.getName() + " with " + shape.getSides() + " sides!").queue();
    }

    @Command(alias = "ping")
    public void onPing()
    {
        throw new UnimplementedFeatureException("This is still a work in progress, we'll try and get it finished as soon as possible!");
    }
}
