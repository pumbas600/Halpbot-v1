package nz.pumbas.halpbot.commands;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.customparameters.math.Matrix;

@Command(reflections = Matrix.class)
public class MatrixCommands {

    @Command(description = "Displays a matrix")
    public Matrix matrix(Matrix matrix) {
        return matrix;
    }

    @Command(description = "Transposes a matrix")
    public Matrix transpose(Matrix matrix) {
        return matrix.transpose();
    }

    @Command(description = "Multiplies two or more matrices")
    public Matrix multiply(Matrix matrix, Matrix other) {
        return matrix.multiply(other);
    }
}
