package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;


public class ByteInitializer implements Initializer {
    private final byte[] _encoded;


    public ByteInitializer(byte[] encoded) {
        _encoded = encoded;
    }

    @Override public void init(Plane plane, Rule rule, Random random) {
        final byte[] encodedInput = _encoded;

        int idx = 0;
        final int colors[] = rule.colors();
        switch(rule.dimensions()) {
            case 1:
                for(int i=0;i<plane.getWidth();i++) {
                    plane.setCell(i, 0, encodedInput[idx]);
                    idx = (idx+1) % encodedInput.length;
                }
                break;
            case 2:
            default:
                for(int j=0;j<plane.getHeight();j++) {
                    for(int i=0;i<plane.getWidth();i++) {
                        plane.setCell(i, j, encodedInput[idx]);
                        idx = (idx+1) % encodedInput.length;
                    }
                }
                break;
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
    }
}
