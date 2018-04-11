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


public interface Palette {
    public enum Mode {
        rgba,
        indexed
    };

    int getColorCount();

    int[] getColors();

    int color(int idx);

    boolean isBlack(int idx);

    int[][] unpack();

    Palette replace(int index, int newColor);

    IndexColorModel toColorModel();

    Palette matchCapacity(int colorCount, Random r);

    Palette ensureCapacity(int colorCount, Random r);

    Map<Integer,Integer> buildColormap();

    Palette cut(int div, Random om);

    void write(DataOutputStream dos) throws IOException;

    void write(PrintWriter w);

    public static Palette read(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        int[] colors = new int[len];
        for(int i=0;i<colors.length;i++) {
            colors[i] = dis.readInt();
        }
        return new IndexedPalette(colors);
    }

    public static Palette read(BufferedReader r, int version) throws IOException {
        int len = Integer.parseInt(r.readLine());
        int[] colors = new int[len];
        for(int i=0;i<colors.length;i++) {
            colors[i] = Integer.parseInt(r.readLine());
        }
        return new IndexedPalette(colors);
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
        return new IndexedPalette(packed);
    }

    public static Palette grey(int numColors) {
        int[] packed = new int[numColors];
        for(int i=0;i<numColors;i++) {
            int amt = (int) (255 * ((double) i / (double) numColors));
            packed[i] = Colors.pack(amt, amt, amt);
        }
        return new IndexedPalette(packed);
    }

    public static Palette shades(int numColors, int[] max) {
        int[] packed = new int[numColors];
        for(int i=0;i<numColors;i++) {
            int red = (int) (max[0] * ((double) i / (double) numColors));
            int green = (int) (max[1] * ((double) i / (double) numColors));
            int blue = (int) (max[2] * ((double) i / (double) numColors));
            packed[i] = Colors.pack(red, green, blue);
        }
        return new IndexedPalette(packed);
    }

    public static Palette allShades(int numColors, int[] max) {
        final int[] r = new int[]{255,127,127};
        final int[] g = new int[]{127,255,127};
        final int[] b = new int[]{127,127,255};
        int[] packed = new int[numColors];
        int tnc = numColors/3;
        int idx = 0;
        for(int i=0;i<numColors/3;i++) {
            int red = (int) (r[0] * ((double) i / (double) tnc));
            int green = (int) (r[1] * ((double) i / (double) tnc));
            int blue = (int) (r[2] * ((double) i / (double) tnc));
            packed[idx++] = Colors.pack(red, green, blue);
        }
        for(int i=0;i<numColors/3;i++) {
            int red = (int) (g[0] * ((double) i / (double) tnc));
            int green = (int) (g[1] * ((double) i / (double) tnc));
            int blue = (int) (g[2] * ((double) i / (double) tnc));
            packed[idx++] = Colors.pack(red, green, blue);
        }
        for(int i=0;i<numColors/3;i++) {
            int red = (int) (b[0] * ((double) i / (double) tnc));
            int green = (int) (b[1] * ((double) i / (double) tnc));
            int blue = (int) (b[2] * ((double) i / (double) tnc));
            packed[idx++] = Colors.pack(red, green, blue);
        }
        while(idx<numColors) {
            packed[idx++] = Colors.pack(max[0], max[1], max[2]);
        }
        return new IndexedPalette(packed);
    }

    static final int[][] SPECTRUM_RAINBOW = {
        {148,0,211},
        {75,0,130},
        {0,0,255},
        {0,255,0},
        {255,255,0},
        {255,127,0},
        {255,0,0}
    };

    public static Palette randomRainbow(Random om, int numColors, boolean black, int density) {
        final int[][] s = new int[2+om.nextInt(density)][3];
        for(int i=0;i<s.length;i++) {
            Colors.unpackRgb(Colors.randomColor(om), s[i]);
        }
        return rainbow(numColors, black, s);
    }

    public static Palette randomCutRainbow(Random om, int numColors, int div, boolean black, int density, int invisible) {
        Palette p = randomShinyRainbow(om, numColors, black, density, invisible);
        int[] colors = p.getColors();
        return randomCutRainbow(om, numColors, colors, div, black, density, invisible);
    }

    public static Palette randomCutRainbow(Random om, int numColors, int[] colors, int div, boolean black, int density, int invisible) {
        int chance = div==0?0:Math.max(1,numColors/div);
        for(int i=0;i<colors.length;i++) {
            if(om.nextInt(numColors)<chance) {
                int len = 1+om.nextInt(numColors/100);
                for(int j=0;j<len&&i+j<numColors;j++) {
                    colors[i+j] = 0;
                }
                i+=len;
            }
        }
        return new IndexedPalette(colors);
    }

    public static Palette randomShinyRainbow(Random om, int numColors, boolean black, int density) {
        return randomShinyRainbow(om, numColors, black, density, 0);
    }

    public static Palette randomShinyRainbow(Random om, int numColors, boolean black, int density, int invisible) {
        final int[][] s = new int[2+om.nextInt(density)][3];
        for(int i=0;i<s.length;i++) {
            if(false&&om.nextInt(2+density)<=density/10) {
                Colors.unpackRgb(Colors.pack(255,255,255), s[i]);
            }
            else if(om.nextInt(101)<invisible) {
                Colors.unpackRgb(Colors.pack(0,0,0), s[i]);
            }
            else {
                Colors.unpackRgb(Colors.randomColor(om, 32), s[i]);
            }
        }
        return rainbow(numColors, black, s);
    }

    public static Palette randomWrappedRainbow(Random om, int numColors, boolean black, int density) {
        final int[][] s = new int[3+om.nextInt(density)][3];
        for(int i=0;i<s.length-1;i++) {
            Colors.unpackRgb(Colors.randomColor(om), s[i]);
        }
        s[s.length-1][0] = s[0][0];
        s[s.length-1][1] = s[0][1];
        s[s.length-1][2] = s[0][2];
        return rainbow(numColors, black, s);
    }

    public static Palette rainbow(int numColors, boolean black) {
        return rainbow(numColors, black, SPECTRUM_RAINBOW);
    }

    public static Palette rainbow(int numColors, boolean black, int[][] spectrum) {
        int[] packed = new int[numColors];
        int start = black?1:0;
        for(int i=start;i<numColors;i++) {
            double p = (double)(i-start) / (double) (numColors-start);
            double s = p * spectrum.length;
            int low = (int) Math.floor(s);
            int high = Math.min(spectrum.length-1, (int) Math.ceil(s));
            double wgt = s-low;

            int red = (int) (spectrum[low][0]*(1d-wgt) + spectrum[high][0]*(wgt));
            int green = (int) (spectrum[low][1]*(1d-wgt) + spectrum[high][1]*(wgt));
            int blue = (int) (spectrum[low][2]*(1d-wgt) + spectrum[high][2]*(wgt));
            packed[i] = Colors.pack(red, green, blue);
        }
        return new IndexedPalette(packed);
    }

    public static Palette fromImage(BufferedImage img) {
        class Key implements Comparable {
            public final int c;
            public final int sort;

            public Key(int c, int sort) {
                this.c = c;
                this.sort = sort;
            }

            @Override public int hashCode() {
                return c;
            }

            @Override public boolean equals(Object o) {
                final Key k = (Key)o;
                return k.c==c;
            }

            @Override public int compareTo(Object o) {
                final Key k = (Key)o;
                return sort-k.sort;
            }
        }

        Set<Key> c = new HashSet<>();
        Raster r = img.getRaster();
        final int[] unpacked = new int[4];
        for(int i=0;i<img.getWidth();i++) {
            for(int j=0;j<img.getHeight();j++) {
                //final int p = r.getSample(i,j,0);
                final int p = img.getRGB(i,j);
                Colors.unpack(p, unpacked);
                Key k = new Key(p, unpacked[0]+unpacked[1]+unpacked[2]);
                if(!c.contains(k)) {
                    c.add(k);
                }
            }
        }
        System.err.println("found "+c.size()+" colors");
        final List<Key> sort = new ArrayList<>(c);
        c.clear();
        Collections.sort(sort);
        int[] colors = new int[sort.size()];
        int i = 0;
        for(Key k:sort) {
            colors[i++] = k.c;
        }
        System.err.println("*** color zero: "+colors[0]);
        return new IndexedPalette(true, colors);
    }
}
