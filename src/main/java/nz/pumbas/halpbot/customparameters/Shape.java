package nz.pumbas.halpbot.customparameters;

import nz.pumbas.commands.annotations.ParameterConstruction;

public class Shape
{
    private final String name;
    private final int sides;

    @ParameterConstruction
    public Shape(String name, int sides)
    {
        this.name = name;
        this.sides = sides;
    }

    @ParameterConstruction
    public Shape(int sides)
    {
        this.name = "shape";
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
