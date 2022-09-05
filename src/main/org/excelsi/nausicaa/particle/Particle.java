package org.excelsi.nausicaa.particle;


public final class Particle {
    private final double _t;
    private double _x;
    private double _y;
    private final double[] _v = new double[2];
    private final double[] _a = new double[2];


    public Particle(double type) {
        _t = type;
    }

    public double getX() {
        return _x;
    }

    public double getY() {
        return _y;
    }

    public void moveTo(double x, double y) {
        _x = x;
        _y = y;
    }

    public void setVelocity(double dx, double dy) {
        _v[0] = dx;
        _v[1] = dy;
    }

    public void setAcceleration(double ax, double ay) {
        _a[0] = ax;
        _a[1] = ay;
    }

    public void interact(Particle p, double[] dv) {
        final double F = 0.001d;
        double d2 = _x*p.getX() + _y*p.getY();
        // SOHCAHTOA
        double dx = F*(1d/Math.cos(p.getX()-_x));
        double dy = F*(1d/Math.sin(p.getY()-_y));
        dv[0] += dx;
        dv[1] += dy;
    }

    public void commit(double[] dv) {
        _v[0] = dv[0];
        _v[1] = dv[1];
        _x += _v[0];
        _y += _v[1];
    }
}
