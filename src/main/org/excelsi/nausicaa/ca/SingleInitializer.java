package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.IOException;


public class SingleInitializer implements Initializer {
    public void init(Plane plane, Rule rule, Random random) {
        int[] colors = rule.colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, colors[0]);
                }
                plane.setCell(plane.getWidth()/2, 0, colors[1]);
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, colors[0]);
                    }
                }
                plane.setCell(plane.getWidth()/2, plane.getHeight()/2, colors[1]);
                break;
            case 3:
                BlockPlane bp = (BlockPlane) plane;
                for(int y=0;y<bp.getHeight();y++) {
                    for(int x=0;x<bp.getWidth();x++) {
                        for(int z=0;z<bp.getDepth();z++) {
                            bp.setCell(x, y, z, colors[0]);
                        }
                    }
                }
                bp.setCell(bp.getWidth()/2, bp.getHeight()/2, bp.getDepth()/2, colors[1]);
                break;
            default:
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.single.getId());
    }
}
