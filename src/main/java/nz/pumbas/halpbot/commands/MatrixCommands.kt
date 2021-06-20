package nz.pumbas.halpbot.commands

import nz.pumbas.commands.annotations.Command
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

    @Command(alias = "Multiply", description = "Multiplies two matrices", reflections = [Matrix::class])
    fun multiply(matrix1: Matrix, matrix2: Matrix): Matrix {
        return matrix1.multiply(matrix2)
    }

}