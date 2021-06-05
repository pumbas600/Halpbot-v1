package nz.pumbas.halpbot.commands;

import java.util.Arrays;

import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.CommandGroup;
import nz.pumbas.halpbot.customparameters.Shape;

@CommandGroup(defaultPrefix = "$")
public class TokenCommands
{
    @Command(alias = "centroid", description = "finds the centroid defined by the specified shapes")
    public String findCentroid(Shape[] shapes)
    {
        return Arrays.toString(shapes);
    }

}
