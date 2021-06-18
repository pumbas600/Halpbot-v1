package nz.pumbas.halpbot.commands

import nz.pumbas.commands.annotations.Command
import nz.pumbas.halpbot.customparameters.Matrix

class MatrixCommands {

    @Command(alias = "Matrix", description = "Displays a matrix")
    fun matrix(matrix: Matrix): Matrix {
        return matrix
    }

    @Command(alias = "Transpose", description = "Transposes a matrix")
    fun transpose(matrix: Matrix): Matrix {
        return matrix.transpose()
    }
}