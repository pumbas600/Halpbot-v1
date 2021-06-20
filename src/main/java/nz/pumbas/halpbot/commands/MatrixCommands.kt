package nz.pumbas.halpbot.commands

import nz.pumbas.commands.annotations.Command
import nz.pumbas.commands.validation.Implicit
import nz.pumbas.halpbot.customparameters.Matrix

class MatrixCommands {

    @Command(alias = "Matrix", description = "Displays a matrix", reflections = [Matrix::class])
    fun matrix(matrix: Matrix): Matrix {
        return matrix
    }

    @Command(alias = "Transpose", description = "Transposes a matrix", reflections = [Matrix::class])
    fun transpose(matrix: Matrix): Matrix {
        return matrix.transpose()
    }

    @Command(alias = "Multiply", description = "Multiplies two or more matrices", reflections = [Matrix::class])
    fun multiply(matrix: Matrix, @Implicit otherMatrices: Array<Matrix>): Matrix {
        var result = matrix

        for (otherMatrix in otherMatrices) {
            result = result.multiply(otherMatrix)
        }

        return result
    }

}