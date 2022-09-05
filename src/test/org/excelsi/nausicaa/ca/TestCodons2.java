package org.excelsi.nausicaa.ca;


import java.io.IOException;
// import org.junit.Test;
// import static org.junit.Assert.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.excelsi.nausicaa.ca.Codons.*;


public class TestCodons2 {
    private static final double T = 0.001d;
    private IntTape _i;
    private FloatTape _d;


    @BeforeEach
    protected void setUp() {
        _i = new IntTape(32768);
        _d = new FloatTape(32768);
    }

    /*
    @Test
    public void testAbs() {
        Abs a = new Abs();
        a.op(pat(-1), _i);
        assertEquals(0, _i.pop());

        _i.push(-1);
        a.op(pat(-1), _i);
        assertEquals(1, _i.pop());

        _i.push(1);
        a.op(pat(1), _i);
        assertEquals(1, _i.pop());

        a.op(pat(-1d), _d);
        assertEquals(0d, _d.pop(), T);
        _d.push(-1d);
        a.op(pat(-1d), _d);

        assertEquals(1d, _d.pop(), T);

        _d.push(1d);
        a.op(pat(1d), _d);
        assertEquals(1d, _d.pop(), T);
    }

    @Test
    public void testPushOI() {
        PushO o = new PushO();
        int[] p =   pat(0, 1, 0, 1, 0, 1, 1, 1, 1);
        int[] exp = pat(1, 1, 1, 1,    1, 0, 1, 0);
        int mid = 9/2;
        o.op(p, _i);
        for(int i=0;i<exp.length;i++) {
            assertEquals("wrong value at idx "+i+" with expected mid "+mid, exp[i], _i.pop());
        }
    }

    @Test
    public void testPushOD() {
        PushO o = new PushO();
        double[] p =   pat(0d, 1d, 0d, 1d, 0d, 1d, 1d, 1d, 1d);
        double[] exp = pat(1d, 1d, 1d, 1d,    1d, 0d, 1d, 0d);
        int mid = 9/2;
        o.op(p, _d);
        for(int i=0;i<exp.length;i++) {
            assertEquals("wrong value at idx "+i+" with expected mid "+mid, exp[i], _d.pop(), T);
        }
    }

    @Test
    public void testLeastI() {
        Least l = new Least(-1);
        int[] p = pat(1, 1, 1, 2, 2, 2, 5, 5, 8);
        _i.pushAll(p, p.length);
        l.op(p, _i);
        assertEquals(8, _i.pop());

        l = new Least(51);
        _i.pushAll(p, p.length);
        l.op(p, _i);
        assertEquals(8, _i.pop());
    }

    @Test
    public void testLeastD() {
        Least l = new Least(-1);
        double[] p = pat(1d, 1d, 1d, 2d, 2d, 2d, 5d, 5d, 8d);
        _d.pushAll(p, p.length);
        l.op(p, _d);
        assertEquals(8d, _d.pop(), T);

        l = new Least(5);
        _d.pushAll(p, p.length);
        l.op(p, _d);
        assertEquals(8d, _d.pop(), T);
    }

    @Test
    public void testMostI() {
        Most m = new Most(-1);
        int[] p = pat(1, 1, 1, 2, 3, 4, 5, 5, 8);
        _i.pushAll(p, p.length);
        m.op(p, _i);
        assertEquals(1, _i.pop());

        m = new Most(3);
        _i.pushAll(p, p.length);
        m.op(p, _i);
        assertEquals(5, _i.pop());
    }

    @Test
    public void testMostD() {
        Most m = new Most(-1);
        double[] p = pat(1d, 1d, 1d, 2d, 3d, 4d, 5d, 5d, 8d);
        _d.pushAll(p, p.length);
        m.op(p, _d);
        assertEquals(1d, _d.pop(), T);

        m = new Most(3);
        _d.pushAll(p, p.length);
        m.op(p, _d);
        assertEquals(5d, _d.pop(), T);
    }

    @Test
    public void testMaxI() {
        Max m = new Max(-1);
        int[] p = pat(1, 2, 3, 4, 5, 6, 10, 9, 8);
        _i.pushAll(p, p.length);
        m.op(p, _i);
        assertEquals(10, _i.pop());

        m = new Max(4);
        _i.pushAll(p, p.length);
        m.op(p, _i);
        assertEquals(10, _i.pop());
    }

    @Test
    public void testMaxD() {
        Max m = new Max(-1);
        double[] p = pat(1d, 2d, 3d, 4d, 5d, 6d, 10d, 9d, 8d);
        _d.pushAll(p, p.length);
        m.op(p, _d);
        assertEquals(10d, _d.pop(), T);

        m = new Max(4);
        _d.pushAll(p, p.length);
        m.op(p, _d);
        assertEquals(10d, _d.pop(), T);
    }

    @Test
    public void testMinI() {
        Min m = new Min(-1);
        int[] p = pat(3, 2, 1, 4, 5, 6, 10, 9, 8);
        _i.pushAll(p, p.length);
        m.op(p, _i);
        assertEquals(1, _i.pop());

        m = new Min(4);
        _i.pushAll(p, p.length);
        m.op(p, _i);
        assertEquals(6, _i.pop());
    }

    @Test
    public void testMinD() {
        Min m = new Min(-1);
        double[] p = pat(3d, 2d, 1d, 4d, 5d, 6d, 10d, 9d, 8d);
        _d.pushAll(p, p.length);
        m.op(p, _d);
        assertEquals(1d, _d.pop(), T);

        m = new Min(4);
        _d.pushAll(p, p.length);
        m.op(p, _d);
        assertEquals(6d, _d.pop(), T);
    }

    @Test
    public void testAvgI() {
        Avg a = new Avg(-1);
        int[] p = pat(0, 0, 5, 5, 10, 10);
        _i.pushAll(p, p.length);
        a.op(p, _i);
        assertEquals(5, _i.pop());

        a = new Avg(2);
        _i.pushAll(p, p.length);
        a.op(p, _i);
        assertEquals(10, _i.pop());
    }

    @Test
    public void testAvgD() {
        Avg a = new Avg(-1);
        double[] p = pat(0d, 0d, 5d, 5d, 10d, 10d);
        _d.pushAll(p, p.length);
        a.op(p, _d);
        assertEquals(5d, _d.pop(), T);

        a = new Avg(2);
        _d.pushAll(p, p.length);
        a.op(p, _d);
        assertEquals(10d, _d.pop(), T);
    }
    */

    @Test
    public void atan() {
        assertEquals(0d, Math.atan2(0,0), "atan");
    }

    @Test
    public void mandelbulb() {
        mandelbulb(0,0,0);
        mandelbulb(0.1,0.1,0.1);
        mandelbulb(1,1,1);
        mandelbulb(10,10,10);
        mandelbulb(100,100,100);
    }

    private void mandelbulb(double x, double y, double z) {
        Mandelbulb b = new Mandelbulb();
        _d.push(x);
        _d.push(y);
        _d.push(z);
        _d.push(50);
        _d.push(1d);
        b.op(new double[0], _d, null);
        double zz = _d.pop();
        System.err.println("z="+zz);
    }

    private int[] pat(int... is) {
        return is;
    }

    private double[] pat(double... ds) {
        return ds;
    }
}
