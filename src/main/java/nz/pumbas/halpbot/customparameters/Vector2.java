package nz.pumbas.halpbot.customparameters;

import nz.pumbas.utilities.Utilities;

public class Vector2
{
    private final double x;
    private final double y;
    private final String units;

    public Vector2(double x, double y, String units)
    {
        this.x = x;
        this.y = y;
        this.units = units;
    }

    public Vector2(double magnitude, double angle, boolean fromX, String units)
    {
        if (!fromX) //Makes the angle from the x-axis
            angle += Utilities.quarterRotation;
        angle %= Utilities.fullRotation;

        double radians = Math.toRadians(angle);
        this.x = magnitude * Math.cos(radians);
        this.y = magnitude * Math.sin(radians);

        this.units = units;
    }

    public double getSqrMagnitude()
    {
        return this.x * this.x + this.y * this.y;
    }

    public double getMagnitude()
    {
        return Math.sqrt(this.getSqrMagnitude());
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public String getUnits()
    {
        return this.units;
    }

    @Override
    public String toString()
    {
        return String.format("%.1fi + %.1fj %s", this.getX(), this.getY(), this.getUnits());
    }
}
