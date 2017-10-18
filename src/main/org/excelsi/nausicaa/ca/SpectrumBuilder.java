package org.excelsi.nausicaa.ca;


import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public class SpectrumBuilder {
    private int _numColors;
    private boolean _cut;
    private List<Key> _keyPoints = new ArrayList<>();


    public SpectrumBuilder(int nc) {
        _numColors = nc;
    }

    public SpectrumBuilder cut(boolean cut) {
        _cut = cut;
        return this;
    }

    public SpectrumBuilder key(float pct, int r, int g, int b) {
        int abs = Math.max(1,(int)(_numColors*pct));
        _keyPoints.add(new Key(abs, r, g, b));
        return this;
    }

    public SpectrumBuilder key(int abs, int r, int g, int b) {
        if(abs>0&&_keyPoints.isEmpty()) {
            _keyPoints.add(new Key(0, 0, 0, 0));
        }
        _keyPoints.add(new Key(abs, r, g, b));
        return this;
    }

    public Palette build() {
        int[] colors = new int[_numColors];
        int loc = 0;
        for(int i=0;i<_keyPoints.size()-1;i++) {
            Key k1 = _keyPoints.get(i);
            Key k2 = _keyPoints.get(i+1);
            int start = loc;
            int end = loc+k2.abs;
            populate(colors,
                start,
                new int[]{k1.r,k1.g,k1.b},
                end,
                new int[]{k2.r,k2.g,k2.b});
            loc = k2.abs;
        }
        if(loc<colors.length) {
            Key k1 = _keyPoints.get(_keyPoints.size()-1);
            Key k2 = _keyPoints.get(0);
            populate(colors,
                loc,
                new int[]{k1.r,k1.g,k1.b},
                colors.length,
                new int[]{k1.r,k1.g,k1.b});
        }
        if(_cut) {
            return Palette.randomCutRainbow(new Random(), colors.length, colors, 40, false, 0);
        }
        else {
            return new Palette(colors);
        }
    }

    private void populate(int[] colors, int p1, int[] c1, int p2, int[] c2) {
        int len = p2-p1;
        for(int i=p1;i<p2;i++) {
            double wgt = (double)(i-p1) / (double)(p2-p1);
            int red = (int) (c1[0]*(1d-wgt) + c2[0]*(wgt));
            int green = (int) (c1[1]*(1d-wgt) + c2[1]*(wgt));
            int blue = (int) (c1[2]*(1d-wgt) + c2[2]*(wgt));
            colors[i] = Colors.pack(red, green, blue);
        }
    }

    static class Key {
        public final int abs;
        public final int r;
        public final int g;
        public final int b;


        public Key(int abs, int r, int g, int b) {
            this.abs = abs;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
}
