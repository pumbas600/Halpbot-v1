package nz.pumbas.halpbot.commands;

import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.CommandGroup;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.halpbot.customparameters.Shape;

@CommandGroup(defaultPrefix = "$")
public class TokenCommands
{
    @Command(alias = "centroid", description = "finds the centroid defined by the specified shapes")
    public String findCentroid(@Implicit Shape[] shapes)
    {
        double sumAx = 0;
        double sumAy = 0;
        double totalA = 0;

        for (Shape shape : shapes) {
            sumAx += shape.getArea() * shape.getxPos();
            sumAy += shape.getArea() * shape.getyPos();
            totalA += shape.getArea();
        }

        return String.format("x: %.2f, y: %.2f", sumAx / totalA, sumAy / totalA);
    }

    @Command(alias = "next", description = "returns the next number")
    public int next(int number)
    {
        return number + 1;
    }

}
