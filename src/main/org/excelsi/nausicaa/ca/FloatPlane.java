package org.excelsi.nausicaa.ca;


public interface FloatPlane extends Plane {
    void setCell(int x, int y, float v);
    void setCell(int x, int y, int z, float v);
    void setRGBCell(int x, int y, int rgb);
    float getCell(int x, int y);
    float getCell(int x, int y, int z);
    float[] getRow(float[] into, int y, int offset);
    float[] getBlock(float[] into, int x, int y, int dx, int dy, int offset);
    float[] getCardinal(float[] into, int x, int y, int dx, int dy, int offset);
    float[] getCardinal(float[] into, int x, int y, int z, int dx, int dy, int dz, int offset);
    void setRow(float[] row, int y);
}
