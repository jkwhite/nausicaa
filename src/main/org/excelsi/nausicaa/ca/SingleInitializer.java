package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;


public class SingleInitializer implements Initializer {
    public void init(Plane plane, Rule rule, Random random) {
        int colors = rule.archetype().colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, 0);
                }
                plane.setCell(plane.getWidth()/2, 0, random.nextInt(colors));
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, 0);
                    }
                }
                plane.setCell(plane.getWidth()/2, plane.getHeight()/2, random.nextInt(colors));
                break;
            case 3:
                IntBlockPlane bp = (IntBlockPlane) plane;
                for(int y=0;y<bp.getHeight();y++) {
                    for(int x=0;x<bp.getWidth();x++) {
                        for(int z=0;z<bp.getDepth();z++) {
                            bp.setCell(x, y, z, 0);
                        }
                    }
                }
                bp.setCell(bp.getWidth()/2, bp.getHeight()/2, bp.getDepth()/2, random.nextInt(colors));
                break;
            default:
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.single.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.single.name());
    }

    public static SingleInitializer read(BufferedReader r, int version) throws IOException {
        return new SingleInitializer();
    }
}
