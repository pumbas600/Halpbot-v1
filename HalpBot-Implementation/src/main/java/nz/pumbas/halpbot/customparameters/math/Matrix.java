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

package nz.pumbas.halpbot.customparameters.math;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Reflective;
import nz.pumbas.halpbot.commands.exceptions.IllegalFormatException;
import nz.pumbas.halpbot.converters.DefaultConverters;
import nz.pumbas.halpbot.converters.TypeConverter;
import nz.pumbas.halpbot.objects.DiscordObject;
import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import nz.pumbas.halpbot.commands.exceptions.UnimplementedFeatureException;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;

@Getter
@Service(singleton = false)
public class Matrix implements DiscordObject
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
            if (this.columns() != row.length)
                throw new IllegalFormatException(
                        "The rows in a matrix should have the same number of columns. Expected %d columns but found %d"
                                .formatted(this.columns(), row.length));

        this.values = values;
    }

    public static final TypeConverter<Matrix> MATRIX_CONVERTER = TypeConverter.builder(Matrix.class)
            .convert(invocationContext -> Exceptional.of(() -> {
                invocationContext.assertNext('[');
                List<double[]> matrix = new ArrayList<>();

                while (!invocationContext.isNext(']')) {
                    List<Double> matrixRow = new ArrayList<>();
                    while (!invocationContext.isNext(';') && !invocationContext.isNext(']', false)) {
                        Exceptional<Double> value = DefaultConverters.DOUBLE_CONVERTER.apply(invocationContext);
                        if (value.caught())
                            value.rethrowUnchecked();
                        matrixRow.add(value.get());
                    }
                    matrix.add(matrixRow.stream().mapToDouble(d -> d).toArray());
                }

                return new Matrix(matrix.toArray(new double[0][]));
            }))
            .build();

    public int length() {
        return this.rows() * this.columns();
    }

    public boolean isColumn() {
        return this.columns() == 1;
    }

    public boolean isRow() {
        return this.rows() == 1;
    }

    public double get(int index) {
        return this.isRow() ? this.values[0][index] : this.values[index][0];
    }

    public Matrix row(int row) {
        return new Matrix(this.values[row]);
    }

    public Matrix column(int column) {
        double[][] values = new double[this.rows][1];
        for (int row = 0; row < this.rows(); row++) {
            values[row][0] = this.values[row][column];
        }

        return new Matrix(values);
    }

    public Matrix transpose() {
        double[][] transposedValues = new double[this.columns][this.rows];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                transposedValues[column][row] = this.values[row][column];
            }
        }

        return new Matrix(transposedValues);
    }

    public boolean isSquare(int size) {
        return size == this.columns() && size == this.rows();
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

    public Matrix multiply(Matrix other) {
        if (this.columns() != other.rows())
            throw new IllegalArgumentException(
                    String.format("The matrix %s doesn't have the same number of columns as the matrix %s has rows",
                            this, other));

        double[][] values = new double[this.rows][other.columns];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < other.columns(); column++) {
                values[row][column] = this.row(row).dot(other.column(column).transpose());
            }
        }

        return new Matrix(values);
    }

    public Matrix multiply(double factor) {
        double[][] values = new double[this.rows()][this.columns()];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                values[row][column] = this.values[row][column] * factor;
            }
        }
        return new Matrix(values);
    }

    public Matrix divide(double factor) {
        if (factor == 0)
            return new Matrix(new double[this.rows()][this.columns()]);
        return this.multiply(1/factor);
    }

    public double dot(Matrix other) {
        this.assertDimensionsMatch(other);

        return this.accumulate(0,
                (dot, row, column) -> dot + this.values[row][column] * other.values[row][column]);
    }

    public double sqrtMagnitude() {
        return this.accumulate(0,
                (magnitude, row, column) -> magnitude + this.values[row][column] * this.values[row][column]);
    }

    public double magnitude() {
        return Math.sqrt(this.sqrtMagnitude());
    }

    public Matrix unitMatrix() {
        return this.divide(this.magnitude());
    }

    /**
     * Determines the angle between this matrix and the other matrix in radians. The result of this method doesn't
     * make sense unless you're dealing with either a column or row vector.
     *
     * @param other
     *      The other matrix to find the angle between
     *
     * @return The angle between this matrix and the other matrix in radians.
     */
    public double angleBetween(Matrix other) {
        return Math.acos(this.dot(other) / (this.magnitude() * other.magnitude()));
    }

    public Matrix parallelComponent(Matrix other) {
        // (u.v/v.v)v
        return other.multiply(this.dot(other)/other.dot(other));
    }

    public Matrix perpendicularComponent(Matrix other) {
        return this.subtract(this.parallelComponent(other));
    }

    public Matrix add(Matrix other) {
        this.assertDimensionsMatch(other);

        double[][] values = new double[this.rows()][this.columns()];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                values[row][column] = this.values[row][column] + other.values[row][column];
            }
        }
        return new Matrix(values);
    }

    public Matrix subtract(Matrix other) {
        this.assertDimensionsMatch(other);

        double[][] values = new double[this.rows()][this.columns()];
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                values[row][column] = this.values[row][column] - other.values[row][column];
            }
        }
        return new Matrix(values);
    }

    private void assertDimensionsMatch(Matrix other) {
        if (this.rows() != other.rows() || this.columns() != other.columns())
            throw new IllegalArgumentException(
                    "The matrix %s does not have the same dimensions as %s".formatted(other, this));
    }

    private double accumulate(double initialValue, TriFunction<Double, Integer, Integer, Double> triFunction) {
        double accumulation = initialValue;
        for (int row = 0; row < this.rows(); row++) {
            for (int column = 0; column < this.columns(); column++) {
                accumulation = triFunction.apply(accumulation, row, column);
            }
        }
        return accumulation;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.rows)
                .append(" x ")
                .append(this.columns)
                .append("\n");

        for (int i = 0; i < this.values.length; i++) {
            double[] row = this.values[i];
            if (i != 0) stringBuilder.append("\n");

            stringBuilder.append('[');
            for (int j = 0; j < this.columns(); j++) {
                double value = row[j];
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

    @Reflective
    @Command
    public static Matrix one(int size) {
        double[][] values = new double[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                values[row][column] = 1;
            }
        }
        return new Matrix(values);
    }

    @Reflective
    @Command
    public static Matrix zero(int size) {
        double[][] values = new double[size][size];
        return new Matrix(values);
    }
}
