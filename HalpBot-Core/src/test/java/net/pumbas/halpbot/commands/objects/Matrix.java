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

package net.pumbas.halpbot.commands.objects;

import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.commands.annotations.CustomConstructor;
import net.pumbas.halpbot.commands.annotations.Reflective;
import net.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import net.pumbas.halpbot.commands.exceptions.UnimplementedFeatureException;
import net.pumbas.halpbot.converters.annotations.parameter.Implicit;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;

import org.dockbox.hartshorn.component.Service;

import java.util.Arrays;

@Service(singleton = false)
public class Matrix
{
    public static final Matrix UnitSquare = new Matrix(2, 4, 0, 0, 1, 1, 0, 1, 0, 1);
    public static final Matrix XReflection = new Matrix(2, 2, 1, 0, 0, -1);
    public static final Matrix YReflection = new Matrix(2, 2, -1, 0, 0, 1);

    private final int rows;
    private final int columns;

    private final double[][] values;

    @CustomConstructor(command = "Integer [x] Integer Double[]")
    public Matrix(int rows, int columns, @Unrequired("[]") @Implicit double... values) {
        this.rows = rows;
        this.columns = columns;

        this.values = new double[rows][columns];

        if (this.rows * this.columns < values.length)
            throw new ErrorMessageException(
                String.format("You have too many values (%s), the matrix is only %s x %s.",
                    values.length, this.rows, this.columns));

        for (int i = 0; i < values.length; i++) {
            int row = i / this.columns;
            int column = i % this.columns;

            this.values[row][column] = values[i];
        }
    }

    @CustomConstructor
    public Matrix(@Unrequired("[]") @Implicit double[]... values) {
        this.rows = values.length;
        this.columns = 0 < values.length ? values[0].length : 0;

        for (double[] row : values)
            if (this.getColumns() != row.length)
                throw new ErrorMessageException("The matrix should be a uniform size");

        this.values = values;
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public double[][] getValues() {
        return this.values;
    }

    public Vector getRow(int row) {
        return new Vector(this.values[row]);
    }

    public Vector getColumn(int column) {
        double[] values = new double[this.rows];
        for (int row = 0; row < this.getRows(); row++) {
            values[row] = this.values[row][column];
        }

        return new Vector(values);
    }

    public Matrix transpose() {
        double[][] transposedValues = new double[this.columns][this.rows];
        for (int row = 0; row < this.getRows(); row++) {
            for (int column = 0; column < this.getColumns(); column++) {
                transposedValues[column][row] = this.values[row][column];
            }
        }

        return new Matrix(transposedValues);
    }

    public boolean isSquare(int size) {
        return size == this.getColumns() && size == this.getRows();
    }

    public boolean isSquare() {
        return this.getColumns() == this.getRows();
    }

    public double getDeterminant() {
        if (this.isSquare(2))
            // ad - bc
            return this.values[0][0] * this.values[1][1] - this.values[0][1] * this.values[1][0];

        throw new UnimplementedFeatureException("Still implementing finding the determinant of matrixes larger than 2x2");
    }

    public Matrix multiply(Matrix other) {
        if (this.getColumns() != other.getRows())
            throw new IllegalArgumentException(
                String.format("The matrix %s doesn't have the same number of columns as the matrix %s has rows",
                    this, other));

        double[][] newValues = new double[this.rows][other.columns];
        for (int row = 0; row < this.getRows(); row++) {
            for (int column = 0; column < other.getColumns(); column++) {
                newValues[row][column] = this.getRow(row).dot(other.getColumn(column));
            }
        }

        return new Matrix(newValues);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("```csharp\n")
            .append(this.rows)
            .append(" x ")
            .append(this.columns)
            .append("\n");

        for (double[] row : this.values) {
            stringBuilder.append(Arrays.toString(row)).append("\n");
        }
        stringBuilder.append("```");

        return stringBuilder.toString();
    }

    @Reflective
    @Command
    public static Matrix scale(double scaleFactor) {
        return new Matrix(2, 2, scaleFactor, 0, 0, scaleFactor);
    }

    @Reflective
    @Command
    public static Matrix xStretch(double stretchFactor) {
        return new Matrix(2, 2, stretchFactor, 0, 0, 1);
    }

    @Reflective
    @Command
    public static Matrix yStretch(double stretchFactor) {
        return new Matrix(2, 2, 1, 0, 0, stretchFactor);
    }

    @Reflective
    @Command
    public static Matrix xShear(double shearFactor) {
        return new Matrix(2, 2, 1, shearFactor, 0, 1);
    }

    @Reflective
    @Command
    public static Matrix yShear(double shearFactor) {
        return new Matrix(2, 2, 1, 0, shearFactor, 1);
    }

    @Reflective
    @Command
    public static Matrix rotate(double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        return new Matrix(2, 2, cos, -sin, sin, cos);
    }

    @Reflective
    @Command(alias = {"xReflection", "mirrorX"})
    public static Matrix xReflection() {
        return XReflection;
    }

    @Reflective
    @Command(alias = {"yReflection", "mirrorY"})
    public static Matrix yReflection() {
        return YReflection;
    }

    @Reflective
    @Command(alias = {"unitSquare", "us"})
    public static Matrix unitSquare() {
        return UnitSquare;
    }
}
