package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.IOException;


public class RandomInitializer implements Initializer {
    public void init(Plane plane, Rule rule, Random random) {
        int[] colors = rule.colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, colors[random.nextInt(colors.length)]);
                }
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, colors[random.nextInt(colors.length)]);
                        //plane.setCell(x, y, random.nextInt(colors.length));
                    }
                }
                break;
            default:
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.random.getId());
    }
}
