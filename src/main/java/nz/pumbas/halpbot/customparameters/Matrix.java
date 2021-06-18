package nz.pumbas.halpbot.customparameters;

import java.util.Arrays;

import nz.pumbas.commands.annotations.ParameterConstruction;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.exceptions.ErrorMessageException;
import nz.pumbas.commands.validation.Implicit;

public class Matrix
{
    private final int rows;
    private final int columns;

    private final double[][] values;

    @ParameterConstruction(constructor = "#int <x> #int #int[]")
    public Matrix(int rows, int columns, @Unrequired("[]") @Implicit int... values)
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

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("```\n")
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
}
