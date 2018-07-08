package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import com.google.gson.*;


public class NeapolitanInitializer implements Initializer {
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

    @Override public void write(PrintWriter w) {
        w.println(Initializers.neapolitan.name());
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","neapolitan");
        return o;
    }

    public static NeapolitanInitializer read(BufferedReader r, int version) throws IOException {
        return new NeapolitanInitializer();
    }

    public static NeapolitanInitializer fromJson(JsonElement e) {
        return new NeapolitanInitializer();
    }
}
