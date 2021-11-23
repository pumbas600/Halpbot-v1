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

import nz.pumbas.halpbot.commands.annotations.CustomConstructor;

public class Line {

    private final Vector3 point;
    private final Vector3 gradient;

    @CustomConstructor
    public Line(Vector3 point, Vector3 gradient) {
        this.point = point;
        this.gradient = gradient;
    }

    public static Line ofPoints(Vector3 point1, Vector3 point2) {
        return new Line(point1, point1.subtract(point2));
    }

    public Vector3 getPoint() {
        return this.point;
    }

    public Vector3 getGradient() {
        return this.gradient;
    }

    public Vector3 getPoint(double t) {
        return this.point.add(this.gradient.multiply(t));
    }

    @Override
    public String toString() {
        return String.format("x = %.2f + %.2ft\ny = %.2f + %.2ft\nz = %.2f + %.2ft",
                this.point.getX(), this.gradient.getX(), this.point.getY(), this.gradient.getY(), this.point.getZ(), this.gradient.getZ());
    }
}
