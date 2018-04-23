package org.excelsi.nausicaa.ca;


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
}
