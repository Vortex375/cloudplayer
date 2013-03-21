package de.pandaserv.music.client.math;

/**
 * Complex number type
 */
public class Complex {
    public double real;
    public double im;

    public Complex() {

    }

    public Complex(double real, double im) {
        this.real = real;
        this.im = im;
    }

    public double abs() {
        return Math.hypot(real, im);
    }

    public Complex copy() {
        return new Complex(real, im);
    }

    public Complex add(Complex c) {
        this.real = this.real + c.real;
        this.im = this.im + c.im;
        return this;
    }

    public Complex sub(Complex c) {
        this.real = this.real - c.real;
        this.im = this.im - c.im;
        return this;
    }

    public Complex mul(Complex c) {
        double a = this.real;
        double b = this.im;

        this.real = c.real * a - c.im * b;
        this.im = b * c.real + c.im * a;
        return this;
    }

    public Complex exp() {
        double a = this.real;
        double b = this.im;
        double exp = Math.exp(a);

        this.real = exp * Math.cos(b);
        this.im = exp * Math.sin(b);
        return this;
    }

    @Override
    public String toString() {
        return "(" + real + ", " + im + ")";
    }
}
