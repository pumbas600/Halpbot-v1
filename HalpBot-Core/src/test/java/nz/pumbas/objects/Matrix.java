package nz.pumbas.objects;

import java.util.Arrays;

import nz.pumbas.commands.annotations.ParameterConstruction;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.exceptions.ErrorMessageException;
import nz.pumbas.commands.exceptions.UnimplementedFeatureException;
import nz.pumbas.commands.validation.Implicit;

public class Matrix
{
    public static final Matrix UnitSquare = new Matrix(2, 4, 0, 0, 1, 1, 0, 1, 0, 1);
    public static final Matrix XReflection = new Matrix(2, 2, 1, 0, 0, -1);
    public static final Matrix YReflection = new Matrix(2, 2, -1, 0, 0, 1);


    private final int rows;
    private final int columns;

    private final double[][] values;

    @ParameterConstruction(constructor = "#Integer <x> #Integer #Double[]")
    public Matrix(int rows, int columns, @Unrequired("[]") @Implicit double... values)
    {
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

    @ParameterConstruction
    public Matrix(@Unrequired("[]") @Implicit double[]... values)
    {
        this.rows = values.length;
        this.columns = 0 < values.length ? values[0].length : 0;

        for (double[] row : values)
            if (this.getColumns() != row.length)
                throw new ErrorMessageException("The matrix should be a uniform size");

        this.values = values;
    }

    public int getRows()
    {
        return this.rows;
    }

    public int getColumns()
    {
        return this.columns;
    }

    public double[][] getValues()
    {
        return this.values;
    }

    public Vector getRow(int row)
    {
        return new Vector(this.values[row]);
    }

    public Vector getColumn(int column)
    {
        double[] values = new double[this.rows];
        for (int row = 0; row < this.getRows(); row++) {
            values[row] = this.values[row][column];
        }

        return new Vector(values);
    }

    public Matrix transpose()
    {
        double[][] transposedValues = new double[this.columns][this.rows];
        for (int row = 0; row < this.getRows(); row++) {
            for (int column = 0; column < this.getColumns(); column++) {
                transposedValues[column][row] = this.values[row][column];
            }
        }

        return new Matrix(transposedValues);
    }

    public boolean isSquare(int size)
    {
        return size == this.getColumns() && size == this.getRows();
    }

    public boolean isSquare()
    {
        return this.getColumns() == this.getRows();
    }

    public double getDeterminant() {
        if (this.isSquare(2))
            //ad - bc
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
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("```csharp\n")
            .append(this.rows)
            .append(" x ")
            .append(this.columns)
            .append("\n");

        for (double[] row : this.values)
        {
            stringBuilder.append(Arrays.toString(row)).append("\n");
        }
        stringBuilder.append("```");

        return stringBuilder.toString();
    }

    public static Matrix scale(double scaleFactor)
    {
        return new Matrix(2, 2, scaleFactor, 0, 0, scaleFactor);
    }

    public static Matrix xStretch(double stretchFactor)
    {
        return new Matrix(2, 2, stretchFactor, 0, 0, 1);
    }

    public static Matrix yStretch(double stretchFactor)
    {
        return new Matrix(2, 2, 1, 0, 0, stretchFactor);
    }

    public static Matrix xShear(double shearFactor)
    {
        return new Matrix(2, 2, 1, shearFactor, 0, 1);
    }

    public static Matrix yShear(double shearFactor)
    {
        return new Matrix(2, 2, 1, 0, shearFactor, 1);
    }

    public static Matrix rotate(double degrees)
    {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        return new Matrix(2, 2, cos, -sin, sin, cos);
    }
}
