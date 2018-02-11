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


public final class RGBAPalette implements Palette {
    public int getColorCount() {
        return 16777216;
    }

    public int[] getColors() {
        throw new UnsupportedOperationException();
    }

    public int color(int idx) {
        return idx;
    }

    public boolean isBlack(int idx) {
        return idx==0;
    }

    public int[][] unpack() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }
}
