package nz.pumbas.halpbot.friction;

import nz.pumbas.halpbot.commands.annotations.ParameterConstruction;

public class BlockData
{
    private final double mass;
    private final double frictionalConstant;
    private final double weight;
    private double normal;

    @ParameterConstruction
    public BlockData(double mass, double frictionalConstant)
    {
        this.mass = mass;
        this.frictionalConstant = frictionalConstant;
        this.weight = mass * FrictionCommands.Gravity;
    }

    public double getMass()
    {
        return this.mass;
    }

    public double getFrictionalConstant()
    {
        return this.frictionalConstant;
    }

    public double getWeight()
    {
        return this.weight;
    }

    public void setNormal(double normal)
    {
        this.normal = normal;
    }

    public double getMaxFriction()
    {
        return this.normal * this.frictionalConstant;
    }
}
