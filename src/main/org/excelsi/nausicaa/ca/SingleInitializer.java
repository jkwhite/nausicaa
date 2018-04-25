package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;


public class SingleInitializer implements Initializer {
    private final int _x;
    private final int _y;
    private final int _z;
    private final int _color;


    public SingleInitializer() {
        this(-1, -1, -1, 1);
    }

    public SingleInitializer(int color, int x, int y, int z) {
        _color = color;
        _x = x;
        _y = y;
        _z = z;
    }

    public void init(Plane plane, Rule rule, Random random) {
        int colors = rule.archetype().colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, 0);
                }
                plane.setCell(coordX(plane, _x), 0, color(colors, random, _color));
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, 0);
                    }
                }
                plane.setCell(coordX(plane, _x), coordY(plane, _y), color(colors, random, _color));
                break;
            case 3:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        for(int z=0;z<plane.getDepth();z++) {
                            plane.setCell(x, y, z, 0);
                        }
                    }
                }
                plane.setCell(coordX(plane, _x), coordY(plane, _y), coordZ(plane, _z), color(colors, random, _color));
                break;
            default:
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.single.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.single.name());
        w.println(_color);
        w.println(_x);
        w.println(_y);
        w.println(_z);
    }

    private int coordX(Plane p, int v) {
        return v==-1?p.getWidth()/2:v%p.getWidth();
    }

    private int coordY(Plane p, int v) {
        return v==-1?p.getHeight()/2:v%p.getHeight();
    }

    private int coordZ(Plane p, int v) {
        return v==-1?p.getDepth()/2:v%p.getDepth();
    }

    private int color(int colors, Random r, int v) {
        return v==-1?(1+r.nextInt(colors-1)):v%colors;
    }

    public static SingleInitializer read(BufferedReader r, int version) throws IOException {
        if(version<4) {
            return new SingleInitializer();
        }
        else {
            return new SingleInitializer(
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine())
            );
        }
    }
}
