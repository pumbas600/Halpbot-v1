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

import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.commands.annotations.CustomConstructor;
import net.pumbas.halpbot.commands.annotations.Reflective;
import net.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import net.pumbas.halpbot.commands.exceptions.IllegalFormatException;
import net.pumbas.halpbot.commands.exceptions.UnimplementedFeatureException;
import net.pumbas.halpbot.converters.DefaultConverters;
import net.pumbas.halpbot.converters.TypeConverter;
import net.pumbas.halpbot.converters.annotations.parameter.Implicit;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import net.pumbas.halpbot.objects.DiscordObject;

import org.dockbox.hartshorn.component.Component;
import org.dockbox.hartshorn.util.Result;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
@Component
public class Matrix implements DiscordObject {

    public static final Matrix UnitSquare = new Matrix(2, 4, 0, 0, 1, 1, 0, 1, 0, 1);
    public static final Matrix XReflection = new Matrix(2, 2, 1, 0, 0, -1);
    public static final Matrix YReflection = new Matrix(2, 2, -1, 0, 0, 1);
    public static final TypeConverter<Matrix> MATRIX_CONVERTER = TypeConverter.builder(Matrix.class)
        .convert(invocationContext -> Result.of(() -> {
            invocationContext.assertNext('[');
            List<double[]> matrix = new ArrayList<>();

            while (!invocationContext.isNext(']')) {
                List<Double> matrixRow = new ArrayList<>();
                while (!invocationContext.isNext(';') && !invocationContext.isNext(']', false)) {
                    Result<Double> value = DefaultConverters.DOUBLE_CONVERTER.apply(invocationContext);
                    if (value.caught())
                        value.rethrowUnchecked();
                    matrixRow.add(value.get());
                }
                matrix.add(matrixRow.stream().mapToDouble(d -> d).toArray());
            }

            return new Matrix(matrix.toArray(new double[0][]));
        }))
        .build();
    private final int rows;
    private final int columns;
    private final double[][] values;

    @CustomConstructor(command = "Integer [x] Integer Double[]")
    public Matrix(final int rows, final int columns, @Unrequired("[]") @Implicit final double... values) {
        this.rows = rows;
        this.columns = columns;

        this.values = new double[rows][columns];

        if (this.rows * this.columns < values.length)
            throw new ErrorMessageException(
                String.format("You have too many values (%s), the matrix is only %s x %s.",
                    values.length, this.rows, this.columns));

        for (int i = 0; i < values.length; i++) {
            final int row = i / this.columns;
            final int column = i % this.columns;

            this.values[row][column] = values[i];
        }
    }

    @CustomConstructor
    public Matrix(@Unrequired("[]") @Implicit final double[]... values) {
        this.rows = values.length;
        this.columns = 0 < values.length ? values[0].length : 0;

        for (final double[] row : values)
            if (this.columns() != row.length)
                throw new IllegalFormatException(
                    "The rows in a matrix should have the same number of columns. Expected %d columns but found %d"
                        .formatted(this.columns(), row.length));

        this.values = values;
    }

    @Reflective
    @Command
    public static Matrix scale(final double scaleFactor) {
        return new Matrix(2, 2, scaleFactor, 0, 0, scaleFactor);
    }

    @Reflective
    @Command
    public static Matrix xStretch(final double stretchFactor) {
        return new Matrix(2, 2, stretchFactor, 0, 0, 1);
    }

    @Reflective
    @Command
    public static Matrix yStretch(final double stretchFactor) {
        return new Matrix(2, 2, 1, 0, 0, stretchFactor);
    }

    @Reflective
    @Command
    public static Matrix xShear(final double shearFactor) {
        return new Matrix(2, 2, 1, shearFactor, 0, 1);
    }

    @Reflective
    @Command
    public static Matrix yShear(final double shearFactor) {
        return new Matrix(2, 2, 1, 0, shearFactor, 1);
    }

    @Reflective
    @Command
    public static Matrix rotate(final double degrees) {
        final double radians = Math.toRadians(degrees);
        final double cos = Math.cos(radians);
        final double sin = Math.sin(radians);

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

    @Reflective
    @Command
    public static Matrix one(final int size) {
        final double[][] values = new double[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                values[row][column] = 1;
            }
        }
        return new Matrix(values);
    }

    @Reflective
    @Command
    public static Matrix zero(final int size) {
        final double[][] values = new double[size][size];
        return new Matrix(values);
    }

    public int length() {
        return this.rows() * this.columns();
    }

    public boolean isColumn() {
        return this.columns() == 1;
    }

    public double get(final int index) {
        return this.isRow() ? this.values[0][index] : this.values[index][0];
    }

    public boolean isRow() {
        return this.rows() == 1;
    }

    public boolean isSquare() {
        return this.columns() == this.rows();
    }

    public double getDeterminant() {
        if (this.isSquare(2))
            //ad - bc
            return this.values[0][0] * this.values[1][1] - this.values[0][1] * this.values[1][0];

        throw new UnimplementedFeatureException("Still implementing finding the determinant of matrixes larger than 2x2");
    }

    public boolean isSquare(final int size) {
        return size == this.columns() && size == this.rows();
    }

    public Matrix multiply(final Matrix other) {
        if (this.columns() != other.rows())
            throw new IllegalArgumentException(
                String.format("The matrix %s doesn't have the same number of columns as the matrix %s has rows",
                    this, other));

        final double[][] values = new double[this.rows][other.columns];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < other.columns(); column++) {
                values[row][column] = this.row(row).dot(other.column(column).transpose());
            }
        }

        return new Matrix(values);
    }

    public double dot(final Matrix other) {
        this.assertDimensionsMatch(other);

        return this.accumulate(0,
            (dot, row, column) -> dot + this.values[row][column] * other.values[row][column]);
    }

    public Matrix row(final int row) {
        return new Matrix(this.values[row]);
    }

    public Matrix transpose() {
        final double[][] transposedValues = new double[this.columns][this.rows];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                transposedValues[column][row] = this.values[row][column];
            }
        }

        return new Matrix(transposedValues);
    }

    public Matrix column(final int column) {
        final double[][] values = new double[this.rows][1];
        for (int row = 0; row < this.rows(); row++) {
            values[row][0] = this.values[row][column];
        }

        return new Matrix(values);
    }

    private void assertDimensionsMatch(final Matrix other) {
        if (this.rows() != other.rows() || this.columns() != other.columns())
            throw new IllegalArgumentException(
                "The matrix %s does not have the same dimensions as %s".formatted(other, this));
    }

    private double accumulate(final double initialValue, final TriFunction<Double, Integer, Integer, Double> triFunction) {
        double accumulation = initialValue;
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                accumulation = triFunction.apply(accumulation, row, column);
            }
        }
        return accumulation;
    }

    public Matrix unitMatrix() {
        return this.divide(this.magnitude());
    }

    public Matrix divide(final double factor) {
        if (factor == 0)
            return new Matrix(new double[this.rows()][this.columns()]);
        return this.multiply(1 / factor);
    }

    public double magnitude() {
        return Math.sqrt(this.sqrtMagnitude());
    }

    public Matrix multiply(final double factor) {
        final double[][] values = new double[this.rows()][this.columns()];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                values[row][column] = this.values[row][column] * factor;
            }
        }
        return new Matrix(values);
    }

    public double sqrtMagnitude() {
        return this.accumulate(0,
            (magnitude, row, column) -> magnitude + this.values[row][column] * this.values[row][column]);
    }

    /**
     * Determines the angle between this matrix and the other matrix in radians. The result of this method doesn't make
     * sense unless you're dealing with either a column or row vector.
     *
     * @param other
     *     The other matrix to find the angle between
     *
     * @return The angle between this matrix and the other matrix in radians.
     */
    public double angleBetween(final Matrix other) {
        return Math.acos(this.dot(other) / (this.magnitude() * other.magnitude()));
    }

    public Matrix parallelComponent(final Matrix other) {
        // (u.v/v.v)v
        return other.multiply(this.dot(other) / other.dot(other));
    }

    public Matrix perpendicularComponent(final Matrix other) {
        return this.subtract(this.parallelComponent(other));
    }

    public Matrix add(final Matrix other) {
        this.assertDimensionsMatch(other);

        final double[][] values = new double[this.rows()][this.columns()];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                values[row][column] = this.values[row][column] + other.values[row][column];
            }
        }
        return new Matrix(values);
    }

    public Matrix subtract(final Matrix other) {
        this.assertDimensionsMatch(other);

        final double[][] values = new double[this.rows()][this.columns()];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                values[row][column] = this.values[row][column] - other.values[row][column];
            }
        }
        return new Matrix(values);
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.rows)
            .append(" x ")
            .append(this.columns)
            .append("\n");

        for (int i = 0; i < this.values.length; i++) {
            final double[] row = this.values[i];
            if (i != 0) stringBuilder.append("\n");

            stringBuilder.append('[');
            for (int j = 0; j < this.columns(); j++) {
                final double value = row[j];
                if (j != 0) stringBuilder.append(", ");
                stringBuilder.append("%.2f".formatted(value));
            }
            stringBuilder.append("]");
        }

        return stringBuilder.toString();
    }

    @Override
    public String toDiscordString() {
        return "```csharp\n" + this + "```";
    }
}
