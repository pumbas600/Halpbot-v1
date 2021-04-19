package nz.pumbas.halpbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;

@CommandGroup(defaultPrefix = "$")
public class FrictionCommands
{
    public static final double Gravity = 9.81D;

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
        double normalC = normalA + normalB + weightC * Math.cos(angleInRad);

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
}
