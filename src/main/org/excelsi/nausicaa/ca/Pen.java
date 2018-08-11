package org.excelsi.nausicaa.ca;


public interface Pen {
    void setCell(int x, int y, int v);
    void setCell(int x, int y, int z, int v);
    void setCell(int x, int y, int z, float v);
}
