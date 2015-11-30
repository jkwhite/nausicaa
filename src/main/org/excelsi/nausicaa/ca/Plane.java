package org.excelsi.nausicaa.ca;


import java.io.IOException;
import javafx.scene.image.Image;


public interface Plane {
    int getWidth();
    int getHeight();
    void init();
    void setCell(int x, int y, int v);
    int getCell(int x, int y);
    //int[] getRow(int[] into, int y);
    int[] getRow(int[] into, int y, int offset);
    //int[] getBlock(int[] into, int x, int y, int d);
    //int[] getBlock(int[] into, int x, int y, int d, int offset);
    int[] getBlock(int[] into, int x, int y, int dx, int dy, int offset);
    void setRow(int[] row, int y);
    java.awt.Image toImage();
    java.awt.Image toImage(int width, int height);
    Image toJfxImage();
    CA creator();
    Plane copy();
    void save(String file) throws IOException;
}
