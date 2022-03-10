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

package net.pumbas.halpbot.customparameters.math;

public class Plane {

    private final Vector3 normal;
    private final Vector3 point;

    public Plane(Vector3 normal, Vector3 point) {
        this.normal = normal;
        this.point = point;
    }

    public Plane(Vector3 point1, Vector3 point2, Vector3 point3) {
        this(point1, point2.subtract(point1).cross(point3.subtract(point1)));
    }

    public Vector3 getNormal() {
        return this.normal;
    }

    public Vector3 getPoint() {
        return this.point;
    }

    public boolean isOnPlane(Vector3 point) {
        return this.normal.dot(point) == this.normal.dot(this.point);
    }

    public Vector3 findIntercept(Line line) {
        double pSubqDotn = this.point.subtract(line.getPoint()).dot(this.normal);
        double uDotn = line.getGradient().dot(this.normal);

        return line.getPoint().add(line.getGradient().multiply(pSubqDotn / uDotn));
    }

    public Line findIntercept(Vector3 otherNormal, Vector3 pointOnIntercept) {
        return new Line(pointOnIntercept, otherNormal.cross(this.normal));
    }

    public Line findIntercept(Plane plane) {
        //TODO: Find intercept of two planes

        return null;
    }

    @Override
    public String toString() {
        return String.format("%.2fx + %.2fy + %.2fz = %s",
                this.normal.getX(), this.normal.getY(), this.normal.getZ(), this.normal.dot(this.point));
    }
}
