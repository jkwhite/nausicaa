package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import com.google.gson.*;


public class ByteInitializer implements Initializer {
    private final byte[] _encoded;


    public ByteInitializer(byte[] encoded) {
        _encoded = encoded;
    }

    @Override public void init(Plane plane, Rule rule, Random random) {
        final byte[] encodedInput = _encoded;

        int idx = 0;
        final int colors[] = rule.colors();
        Pen pen = plane.pen();
        switch(rule.dimensions()) {
            case 1:
                for(int i=0;i<plane.getWidth();i++) {
                    pen.setCell(i, 0, encodedInput[idx]);
                    idx = (idx+1) % encodedInput.length;
                }
                break;
            case 2:
            default:
                for(int j=0;j<plane.getHeight();j++) {
                    for(int i=0;i<plane.getWidth();i++) {
                        pen.setCell(i, j, encodedInput[idx]);
                        idx = (idx+1) % encodedInput.length;
                    }
                }
                break;
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
    }

    @Override public void write(PrintWriter w) {
        w.println("byte");
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","byte");
        return o;
    }

    @Override public String humanize() {
        return "ByteInitializer";
    }
}
