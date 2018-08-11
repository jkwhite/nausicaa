package org.excelsi.nausicaa.ca;


public interface IntPlane extends Plane {
    void setCell(int x, int y, int v);
    void setCell(int x, int y, int z, int v);
    void setRGBCell(int x, int y, int rgb);
    int getCell(int x, int y);
    int getCell(int x, int y, int z);
    int[] getRow(int[] into, int y, int offset);
    int[] getBlock(int[] into, int x, int y, int dx, int dy, int offset);
    int[] getCardinal(int[] into, int x, int y, int dx, int dy, int offset);
    int[] getCardinal(int[] into, int x, int y, int z, int dx, int dy, int dz, int offset);
    void setRow(int[] row, int y);
}
