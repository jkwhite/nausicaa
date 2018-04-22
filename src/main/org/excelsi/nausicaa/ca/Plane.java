package org.excelsi.nausicaa.ca;


import java.awt.image.BufferedImage;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;


public interface Plane /*extends Pattern*/ {
    int getWidth();
    int getHeight();
    int getDepth();
    void init();
    void setCell(int x, int y, int v);
    void setCell(int x, int y, int z, int v);
    void setRGBCell(int x, int y, int rgb);
    int getCell(int x, int y);
    int getCell(int x, int y, int z);
    int[] getRow(int[] into, int y, int offset);
    int[] getBlock(int[] into, int x, int y, int dx, int dy, int offset);
    int[] getCardinal(int[] into, int x, int y, int z, int offset);
    void setRow(int[] row, int y);
    java.awt.Image toImage();
    java.awt.Image toImage(int width, int height);
    BufferedImage toBufferedImage();
    Image toJfxImage();
    Image toJfxImage(WritableImage jfxImage);
    CA creator();
    Plane copy();
    Plane withDepth(int d);
    Plane scale(float scale);
    Plane scale(float scale, boolean antialias);
    void save(String file) throws IOException;
    Plane subplane(int x1, int y1, int x2, int y2);
    byte next(int pattern);
    void tick();
    void lockRead();
    void unlockRead();
    void lockWrite();
    void unlockWrite();
}
