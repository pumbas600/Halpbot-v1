/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.customparameters.math;

import net.pumbas.halpbot.utilities.HalpbotUtils;

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
            angle += HalpbotUtils.quarterRotation;
        angle %= HalpbotUtils.fullRotation;

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
