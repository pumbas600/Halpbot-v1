package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.CommandGroup;
import nz.pumbas.halpbot.customparameters.Vector3;

@CommandGroup(defaultPrefix = "$")
public class VectorCommands
{

    @Command(alias = "add")
    public void onAdd(MessageReceivedEvent event, Vector3 a, Vector3 b) {
        event.getChannel().sendMessage(a.add(b).toString()).queue();
    }

    @Command(alias = "subtract")
    public void onSub(MessageReceivedEvent event, Vector3 a, Vector3 b) {
        event.getChannel().sendMessage(a.subtract(b).toString()).queue();
    }

    @Command(alias = "multiply")
    public void onMultiply(MessageReceivedEvent event, Vector3 a, double b) {
        event.getChannel().sendMessage(a.multiply(b).toString()).queue();
    }

    @Command(alias = "divide")
    public void onDivide(MessageReceivedEvent event, Vector3 a, double b) {
        event.getChannel().sendMessage(a.divide(b).toString()).queue();
    }

    @Command(alias = "unit")
    public void onUnitVector(MessageReceivedEvent event, Vector3 a) {
        event.getChannel().sendMessage(a.getUnitVector().toString()).queue();
    }

    @Command(alias = "magnitude")
    public void onMagnitude(MessageReceivedEvent event, Vector3 a) {
        event.getChannel().sendMessage(String.valueOf(a.getMagnitude())).queue();
    }

    @Command(alias = "dot")
    public void onDot(MessageReceivedEvent event, Vector3 a, Vector3 b) {
        event.getChannel().sendMessage(String.valueOf(a.dot(b))).queue();
    }

    @Command(alias = "angle")
    public void onAngle(MessageReceivedEvent event, Vector3 a, Vector3 b) {
        event.getChannel().sendMessage(String.valueOf(a.getAngleBetween(b))).queue();
    }

    @Command(alias = "cross")
    public void onCross(MessageReceivedEvent event, Vector3 a, Vector3 b) {
        event.getChannel().sendMessage(a.cross(b).toString()).queue();
    }

    @Command(alias = "project")
    public void onProject(MessageReceivedEvent event, Vector3 a, Vector3 b) {
        event.getChannel().sendMessage(a.getParallelComponent(b).toString()).queue();
    }
}
