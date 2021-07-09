package nz.pumbas.halpbot.commands

import nz.pumbas.commands.annotations.Command
import nz.pumbas.halpbot.customparameters.Matrix

@nz.pumbas.commands.annotations.Command(reflections = [Matrix::class])
class MatrixCommands {

    @nz.pumbas.commands.annotations.Command(alias = "Matrix", description = "Displays a matrix")
    fun matrix(matrix: Matrix): Matrix {
        return matrix
    }

    @nz.pumbas.commands.annotations.Command(alias = "Transpose", description = "Transposes a matrix")
    fun transpose(matrix: Matrix): Matrix {
        return matrix.transpose()
    }

    @nz.pumbas.commands.annotations.Command(alias = "Multiply", description = "Multiplies two or more matrices")
    fun multiply(matrix: Matrix, other: Matrix): Matrix {
        return matrix.multiply(other)
    }

}