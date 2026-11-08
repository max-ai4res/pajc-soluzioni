package it.unibs.pajc.redolfi.e09;

public sealed interface Shape2d permits Circle, Rectangle, Square {
    double area();
    double perimeter();
}
