package it.unibs.pajc.redolfi.e09;

public record Square(double side) implements Shape2d {
    public double area() { return side * side; }
    public double perimeter() { return 4 * side; }
}
