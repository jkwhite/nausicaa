package org.excelsi.nausicaa.ca;


public interface FloatPlane extends Plane {
    void setCell(int x, int y, double v);
    void setCell(int x, int y, int z, double v);
    void setRGBCell(int x, int y, int rgb);
    double getCell(int x, int y);
    double getCell(int x, int y, int z);
    double[] getRow(double[] into, int y, int offset);
    double[] getBlock(double[] into, int x, int y, int dx, int dy, int offset);
    double[] getBlock(double[] into, int x, int y, int z, int dx, int dy, int dz, int offset);
    double[] getCoords(double[] into, int x, int y, int[][] coords, int offset);
    double[] getCardinal(double[] into, int x, int y, int dx, int dy, int offset);
    double[] getCardinal(double[] into, int x, int y, int z, int dx, int dy, int dz, int offset);
    void setRow(double[] row, int y);
    int computeRgbColor(double idx);
}
