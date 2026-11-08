package it.unibs.pajc.redolfi.e02;

public record Rectangle(double width, double height) implements Shape2d {
    public double area() { return width * height; }
    public double perimeter() { return 2 * (width + height); }
}
