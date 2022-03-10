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

package net.pumbas.halpbot.customparameters.units;

import java.util.function.Function;

public class Unit
{
    static {
        System.out.println("Unit Loaded");
    }

    // Created from 100MPa
    // Unit.in(Giga)
    // Unit.as(Kelvin)

    private String baseUnit;
    private Prefix prefix;
    private double value;

    public Unit(double value, Prefix prefix, String baseUnit) {
        this.value = value;
        this.prefix = prefix;
        this.baseUnit = baseUnit;
    }

    public Unit to(Prefix newPrefix) {
        this.value *= this.prefix.getMultiplier() / newPrefix.getMultiplier();
        this.prefix = newPrefix;
        return this;
    }

    public Unit as(String newBaseUnit) {
        return this.as(newBaseUnit, Prefix.DEFAULT);
    }

    public Unit as(String newBaseUnit, Prefix newPrefix) {
        Function<Double, Double> converter = UnitManager.getConverter(this.baseUnit, newBaseUnit);
        this.to(Prefix.DEFAULT);
        this.value = converter.apply(this.value);
        this.baseUnit = newBaseUnit;
        this.to(newPrefix);
        return this;
    }

    @Override
    public String toString() {
        return this.value + this.prefix.getPrefix() + this.baseUnit;
    }
}
