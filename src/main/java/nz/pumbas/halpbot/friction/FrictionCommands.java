package nz.pumbas.halpbot.friction;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Optional;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.objects.keys.Key;
import nz.pumbas.objects.keys.Keys;

@CommandGroup(defaultPrefix = "$")
public class FrictionCommands
{
    public static final double Gravity = 9.81D;
    public static final Key<Long, FrictionData> FRICTION_DATA_KEY = Keys.storageKey();

    @Command(alias = "frictionmass", description = "Solves the 3 block with the top block tied friction questions")
    public void onFrictionBlocksMass(MessageReceivedEvent event, double angle, double massA, double Uab, double massB,
                                 double Ubc, double massC, double Ucg)
    {
        double weightA = massA * Gravity;
        double weightB = massB * Gravity;
        double weightC = massC * Gravity;

        this.onFrictionBlocksWeight(event, angle, weightA, Uab, weightB, Ubc, weightC, Ucg);
    }

    @Command(alias = "frictionweight", description = "Solves the 3 block with the top block tied friction questions")
    private void onFrictionBlocksWeight(MessageReceivedEvent event, double angle, double weightA, double Uab,
                                        double weightB, double Ubc, double weightC, double Ucg)
    {
        double angleInRad = Math.toRadians(angle);
        double normalA = weightA * Math.cos(angleInRad);
        double normalB = normalA + weightB * Math.cos(angleInRad);
        double normalC = normalA + (weightB + weightC) * Math.cos(angleInRad);

        double frictionAB = Uab * normalA;
        double frictionBC = Ubc * normalB;
        double frictionCG = Ucg * normalC;

        double case1 = frictionAB + frictionBC - weightB * Math.sin(angleInRad);
        double case2 = frictionAB + frictionCG - (weightB + weightC) * Math.sin(angleInRad);

        if (case1 < case2)
        {
            event.getChannel().sendMessage(String.format("The minimum force required is %.4f and will cause the " +
                "middle block to slide out from between the other blocks", case1)).queue();
        }
        else {
            event.getChannel().sendMessage(String.format("The minimum force required is %.4f and will cause the " +
                "bottom two blocks to slide as a unit", case2)).queue();
        }
    }

    private FrictionData getFrictionData(long userID) {
        Optional<FrictionData> oFrictionData = FRICTION_DATA_KEY.get(userID);
        if (oFrictionData.isPresent())
            return oFrictionData.get();
        else {
            FrictionData data = new FrictionData();
            FRICTION_DATA_KEY.set(userID, data);
            return data;
        }
    }

    @Command(alias = "addblock", description = "Adds a block below the existing blocks")
    private void onAddBlock(MessageReceivedEvent event, BlockData block) {
        FrictionData data = this.getFrictionData(event.getAuthor().getIdLong());
        data.addBlock(block);
        event.getChannel().sendMessage("Block added").queue();
    }

    @Command(alias = "setangle", description = "Sets the angle of inclination in degrees")
    private void onSetAngle(MessageReceivedEvent event, double angle) {
        FrictionData data = this.getFrictionData(event.getAuthor().getIdLong());
        data.setAngle(angle);
        event.getChannel().sendMessage("Angle set").queue();
    }

    @Command(alias = "set", command = "external force acting upwards",
             description = "Sets the external force to be acting upwards, rather than downwards")
    private void onSetExternalForceActingUpwards(MessageReceivedEvent event) {
        FrictionData data = this.getFrictionData(event.getAuthor().getIdLong());
        data.setExternalForceActingUpwards();
        event.getChannel().sendMessage("Set external force acting upwards").queue();
    }

    @Command(alias = "set", command = "external force at block INT",
             description = "Sets the external force to be acting on a particular block in a one-based index system")
    private void onSetExternalForceAtBlock(MessageReceivedEvent event, int blockIndex) {
        FrictionData data = this.getFrictionData(event.getAuthor().getIdLong());
        data.setExternalForceAtBlock(blockIndex);
        event.getChannel().sendMessage("Set block external force is acting at").queue();
    }

    @Command(alias = "solve", description = "Solves the problem with the given information")
    private void onSolve(MessageReceivedEvent event) {
        FrictionData data = this.getFrictionData(event.getAuthor().getIdLong());
        data.solve(event);
        FRICTION_DATA_KEY.set(event.getAuthor().getIdLong(), new FrictionData());
    }

    @Command(alias = "clearfriciton", description = "Clears the stored frictional problem data for a user")
    private void onClear(MessageReceivedEvent event) {
        FRICTION_DATA_KEY.set(event.getAuthor().getIdLong(), new FrictionData());
        event.getChannel().sendMessage(String.format("Cleared the frictional data for %s",
            event.getAuthor().getName())).queue();
    }
}
