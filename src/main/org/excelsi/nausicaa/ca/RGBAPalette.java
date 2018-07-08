package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.awt.image.*;
import javax.imageio.*;
import com.google.gson.*;


public class RGBAPalette implements Palette {
    public int getColorCount() {
        //return 4294967296;
        return 2147483647;
    }

    public int[] getColors() {
        throw new UnsupportedOperationException();
    }

    public int color(int idx) {
        return idx;
    }

    public boolean isBlack(int idx) {
        return (idx & Colors.ALPHA_MASK) == 0 || (idx & Colors.COLOR_MASK) == 0;
    }

    public int[][] unpack() {
        throw new UnsupportedOperationException();
    }

    @Override public int[] unpack(int idx, int[] rgba) {
        Colors.unpack(idx, rgba);
        return rgba;
    }

    public Palette replace(int index, int newColor) {
        throw new UnsupportedOperationException();
    }

    public IndexColorModel toColorModel() {
        throw new UnsupportedOperationException();
    }

    public Palette matchCapacity(int colorCount, Random r) {
        return this;
    }

    public Palette ensureCapacity(int colorCount, Random r) {
        return this;
    }

    public boolean hasColormap() {
        return false;
    }

    public Map<Integer,Integer> buildColormap() {
        throw new UnsupportedOperationException();
    }

    public Palette cut(int div, Random om) {
        throw new UnsupportedOperationException();
    }

    public void write(DataOutputStream dos) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void write(PrintWriter w) {
        w.println("rgba");
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","rgba");
        return o;
    }
}
