package it.unibs.pajc.redolfi.e02;

public sealed interface Shape2d permits Circle, Rectangle, Triangle {
    double area();
    double perimeter();
}
