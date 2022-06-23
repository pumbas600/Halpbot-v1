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

package net.pumbas.halpbot.commands;

import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.converters.annotations.parameter.Implicit;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import net.pumbas.halpbot.customparameters.math.Matrix;

import org.dockbox.hartshorn.component.Service;

import java.util.List;

@Service
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

    @Command(alias = { "multiply", "mult" }, description = "Multiplies two or more matrices")
    public Matrix multiply(Matrix matrixA, Matrix matrixB, @Unrequired("[]") @Implicit List<Matrix> otherMatricies) {
        Matrix matrix =  matrixA.multiply(matrixB);
        for (Matrix other : otherMatricies) {
            matrix = matrix.multiply(other);
        }
        return matrix;
    }
}
