package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.awt.image.IndexColorModel;
import java.util.List;
import java.util.Random;


public class Palette {
    private final int[] _colors;


    public Palette(final int... colors) {
        _colors = colors.clone();
    }

    public Palette(final List<Integer> colors) {
        _colors = new int[colors.size()];
        for(int i=0;i<_colors.length;i++) {
            _colors[i] = colors.get(i);
        }
    }

    public int getColorCount() {
        return _colors.length;
    }

    public int[] getColors() {
        return _colors;
    }

    public Palette replace(int index, int newColor) {
        int[] nc = _colors.clone();
        nc[index] = newColor;
        return new Palette(nc);
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
            return new Palette(ncolors);
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
            return new Palette(ncolors);
        }
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(_colors.length);
        for(int c:_colors) {
            dos.writeInt(c);
        }
    }

    public static Palette read(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        int[] colors = new int[len];
        for(int i=0;i<colors.length;i++) {
            colors[i] = dis.readInt();
        }
        return new Palette(colors);
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

    public static Palette random(int numColors, Random rand) {
        return random(numColors, rand, false);
    }

    public static Palette random(int numColors, Random rand, boolean zeroBlack) {
        int[] packed = new int[numColors];
        for(int i=0;i<numColors;i++) {
            packed[i] = Colors.randomColor(rand);
        }
        if(zeroBlack) {
            packed[0] = Colors.pack(0, 0, 0);
        }
        return new Palette(packed);
    }

    public static Palette grey(int numColors) {
        int[] packed = new int[numColors];
        for(int i=0;i<numColors;i++) {
            int amt = (int) (255 * ((double) i / (double) numColors));
            packed[i] = Colors.pack(amt, amt, amt);
        }
        return new Palette(packed);
    }
}
