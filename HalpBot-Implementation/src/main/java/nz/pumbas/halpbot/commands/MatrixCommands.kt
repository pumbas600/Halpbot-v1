package nz.pumbas.halpbot.commands

import nz.pumbas.commands.annotations.Command
import nz.pumbas.halpbot.customparameters.math.Matrix

@Command(reflections = [Matrix::class])
class MatrixCommands {

    @Command(alias = "Matrix", description = "Displays a matrix")
    fun matrix(matrix: Matrix): Matrix {
        return matrix
    }

    @Command(alias = "Transpose", description = "Transposes a matrix")
    fun transpose(matrix: Matrix): Matrix {
        return matrix.transpose()
    }

    @Command(alias = "Multiply", description = "Multiplies two or more matrices")
    fun multiply(matrix: Matrix, other: Matrix): Matrix {
        return matrix.multiply(other)
    }

}