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

package nz.pumbas.halpbot.commands.objects;

import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import nz.pumbas.halpbot.commands.exceptions.UnimplementedFeatureException;

public class Shape
{
    private final ShapeType shapeType;
    private final double area;
    private final double xPos;
    private final double yPos;

    @CustomConstructor
    public Shape(ShapeType shapeType, double length, double xPos, double yPos) {
        this.shapeType = shapeType;

        if (ShapeType.CIRCLE == shapeType) {
            this.area = Math.PI * length * length;
            this.xPos = xPos;
            this.yPos = yPos;
        } else if (ShapeType.SQUARE == shapeType) {
            this.area = length * length;
            this.xPos = xPos + length / 2;
            this.yPos = yPos + length / 2;
        } else throw new ErrorMessageException("Only circles and squares can define 1 length.");

    }

    @CustomConstructor
    public Shape(ShapeType shapeType, double width, double height, double xPos, double yPos) {
        this.shapeType = shapeType;

        if (ShapeType.RECTANGLE == shapeType || ShapeType.SQUARE == shapeType) {
            this.area = width * height;
            this.xPos = xPos + width / 2;
            this.yPos = yPos + height / 2;
        } else throw new UnimplementedFeatureException("Still working on the other shapes...");
    }

    public double getArea() {
        return this.area;
    }

    public double getxPos() {
        return this.xPos;
    }

    public double getyPos() {
        return this.yPos;
    }

    @Override
    public String toString() {
        return "Shape{" +
            "shapeType=" + this.shapeType +
            ", area=" + this.area +
            ", xPos=" + this.xPos +
            ", yPos=" + this.yPos +
            '}';
    }
}

