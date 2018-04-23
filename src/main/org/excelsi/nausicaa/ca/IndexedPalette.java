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


public final class IndexedPalette implements Palette {
    private final int[] _colors;
    private final int[][] _unpacked;

    public IndexedPalette(final int... colors) {
        _colors = colors.clone();
        _unpacked = unpack(_colors);
    }

    public IndexedPalette(final List<Integer> colors) {
        _colors = new int[colors.size()];
        for(int i=0;i<_colors.length;i++) {
            _colors[i] = colors.get(i);
        }
        _unpacked = unpack(_colors);
    }

    protected IndexedPalette(boolean dummy, final int... colors) {
        _colors = colors;
        _unpacked = unpack(_colors);
    }

    public int getColorCount() {
        return _colors.length;
    }

    public int[] getColors() {
        return _colors;
    }

    public int color(int idx) {
        return _colors[idx];
    }

    public boolean isBlack(int idx) {
        //System.err.print("col "+_colors[idx]+" ");
        //return _colors[idx]==0;
        return Colors.isBlack(_colors[idx]);
    }

    private static int[][] unpack(int[] colors) {
        final int[][] u = new int[colors.length][4];
        for(int i=0;i<u.length;i++) {
            Colors.unpack(colors[i], u[i]);
        }
        return u;
    }

    public int[][] unpack() {
        final int[][] u = new int[_colors.length][4];
        for(int i=0;i<u.length;i++) {
            Colors.unpack(_colors[i], u[i]);
        }
        return u;
    }

    @Override public int[] unpack(int idx, int[] rgba) {
        return _unpacked[idx];
    }

    public Palette replace(int index, int newColor) {
        int[] nc = _colors.clone();
        nc[index] = newColor;
        return new IndexedPalette(nc);
    }

    public IndexColorModel toColorModel() {
        byte[] r = new byte[_colors.length];
        byte[] g = new byte[_colors.length];
        byte[] b = new byte[_colors.length];
        for(int i=0;i<_colors.length;i++) {
            int[] u = Colors.unpack(_colors[i]);
            //System.err.println(i+"i: "+u[0]+", "+u[1]+", "+u[2]);
            //r[i] = (byte) (u[0]-128);
            //g[i] = (byte) (u[1]-128);
            //b[i] = (byte) (u[2]-128);
            b[i] = (byte) (u[0]);
            g[i] = (byte) (u[1]);
            r[i] = (byte) (u[2]);
            //System.err.println(i+"r: "+r[i]+", "+g[i]+", "+b[i]);
        }
        return new IndexColorModel(bitsPerPixel(_colors.length), _colors.length, r, g, b);
        //return new IndexColorModel(8, 2,
            //new byte[]{0, (byte)255},
            //new byte[]{0, (byte)255},
            //new byte[]{0, (byte)255}
        //);
    }

    public Palette matchCapacity(int colorCount, Random r) {
        if(colorCount==_colors.length) {
            return this;
        }
        else {
            int[] ncolors = new int[colorCount];
            System.arraycopy(_colors, 0, ncolors, 0, Math.min(_colors.length, ncolors.length));
            for(int i=_colors.length;i<ncolors.length;i++) {
                ncolors[i] = Colors.randomColor(r);
            }
            return new IndexedPalette(ncolors);
        }
    }

    public Palette ensureCapacity(int colorCount, Random r) {
        if(colorCount<=_colors.length) {
            return this;
        }
        else {
            int[] ncolors = new int[colorCount];
            System.arraycopy(_colors, 0, ncolors, 0, _colors.length);
            for(int i=_colors.length;i<ncolors.length;i++) {
                ncolors[i] = Colors.randomColor(r);
            }
            return new IndexedPalette(ncolors);
        }
    }

    public boolean hasColormap() {
        return true;
    }

    public Map<Integer,Integer> buildColormap() {
        Map<Integer,Integer> m = new HashMap<>(_colors.length);
        for(int i=0;i<_colors.length;i++) {
            m.put(_colors[i], i);
        }
        return m;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(_colors.length);
        for(int c:_colors) {
            dos.writeInt(c);
        }
    }

    public void write(PrintWriter w) {
        w.println(_colors.length);
        for(int c:_colors) {
            w.println(c);
        }
    }

    @Override public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for(int i=0;i<_colors.length;i++) {
            b.append(Colors.toColorString(_colors[i]));
            if(i<_colors.length-1) {
                b.append(",");
            }
        }
        b.append("]");
        return b.toString();
    }

    private int bitsPerPixel(int cols) {
        return cols <= 4 ? 2
            : cols <= 8 ? 4
            : 8;
    }

    public Palette cut(int div, Random om) {
        int[] colors = new int[_colors.length];
        int chance = div==0?0:Math.max(1,_colors.length/div);
        for(int i=0;i<colors.length;i++) {
            if(om.nextInt(colors.length)<chance) {
                int len = 1+om.nextInt(colors.length/100);
                for(int j=0;j<len&&i+j<colors.length;j++) {
                    colors[i+j] = 0;
                }
                i+=len;
            }
            else {
                colors[i] = _colors[i];
            }
        }
        return new IndexedPalette(true, colors);
    }
}
