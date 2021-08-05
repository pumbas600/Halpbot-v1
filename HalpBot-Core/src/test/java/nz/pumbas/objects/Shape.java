package nz.pumbas.objects;

import nz.pumbas.commands.annotations.ParameterConstruction;
import nz.pumbas.commands.exceptions.ErrorMessageException;
import nz.pumbas.commands.exceptions.UnimplementedFeatureException;

public class Shape
{
    private final ShapeType shapeType;
    private final double area;
    private final double xPos;
    private final double yPos;

    @ParameterConstruction
    public Shape(ShapeType shapeType, double length, double xPos, double yPos)
    {
        this.shapeType = shapeType;

        if (ShapeType.CIRCLE == shapeType) {
            this.area = Math.PI * length * length;
            this.xPos = xPos;
            this.yPos = yPos;
        }
        else if (ShapeType.SQUARE == shapeType) {
            this.area = length * length;
            this.xPos = xPos + length / 2;
            this.yPos = yPos + length / 2;
        }
        else throw new ErrorMessageException("Only circles and squares can define 1 length.");

    }

    @ParameterConstruction
    public Shape(ShapeType shapeType, double width, double height, double xPos, double yPos)
    {
        this.shapeType = shapeType;

        if (ShapeType.RECTANGLE == shapeType || ShapeType.SQUARE == shapeType) {
            this.area = width * height;
            this.xPos = xPos + width / 2;
            this.yPos = yPos + height / 2;
        }

        else throw new UnimplementedFeatureException("Still working on the other shapes...");
    }

    public double getArea()
    {
        return this.area;
    }

    public double getxPos()
    {
        return this.xPos;
    }

    public double getyPos()
    {
        return this.yPos;
    }

    @Override
    public String toString()
    {
        return "Shape{" +
            "shapeType=" + this.shapeType +
            ", area=" + this.area +
            ", xPos=" + this.xPos +
            ", yPos=" + this.yPos +
            '}';
    }
}

