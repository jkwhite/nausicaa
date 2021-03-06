package org.excelsi.nausicaa.ca;


import java.io.PrintWriter;
import com.google.gson.*;


public class RGBPalette extends RGBAPalette {
    public int getColorCount() {
        return 16777216;
    }

    public int color(int idx) {
        return Colors.opacify(idx);
    }

    public boolean isBlack(int idx) {
        return (idx & Colors.COLOR_MASK) == 0;
    }

    public void write(PrintWriter w) {
        w.println("rgb");
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","rgb");
        return o;
    }
}
