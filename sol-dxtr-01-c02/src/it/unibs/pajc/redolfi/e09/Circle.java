package it.unibs.pajc.redolfi.e09;

public record Circle(double radius) implements Shape2d {
    public double area() { return Math.PI * radius * radius; }
    public double perimeter() { return 2 * Math.PI * radius; }
}
