package it.unibs.pajc.redolfi.e02;

public record Triangle(double a, double b, double c) implements Shape2d {
    public Triangle {
        if (a + b <= c || a + c <= b || b + c <= a)
            throw new IllegalArgumentException("non e' un triangolo: " + a + ", " + b + ", " + c);
    }
    public double area() {
        double s = perimeter() / 2;
        return Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }
    public double perimeter() { return a + b + c; }
}
