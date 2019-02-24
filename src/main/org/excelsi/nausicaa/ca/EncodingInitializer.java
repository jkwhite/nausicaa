package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import com.google.gson.*;


public class EncodingInitializer implements Initializer {
    private final Encoder _e;
    private final Object _o;


    public EncodingInitializer(Encoder e, Object o) {
        _e = e;
        _o = o;
    }

    @Override public void init(Plane plane, Rule rule, Random random) {
        final byte[] encodedInput = _e.encode(_o);

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
    }

    @Override public String humanize() {
        return "EncodingInitializer";
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","encode");
        return o;
    }
}
