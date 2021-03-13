package nz.pumbas.customparameters;

import nz.pumbas.commands.Annotations.CustomParameter;

@CustomParameter
public class Shape
{
    private final String name;
    private final int sides;

    public Shape(String name, int sides)
    {
        this.name = name;
        this.sides = sides;
    }

    public String getName()
    {
        return this.name;
    }

    public int getSides()
    {
        return this.sides;
    }
}
