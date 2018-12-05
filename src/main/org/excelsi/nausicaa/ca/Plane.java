package org.excelsi.nausicaa.ca;


import java.awt.image.BufferedImage;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.JsonElement;


public interface Plane {
    Values type();
    void init();
    CA creator();
    Plane copy();
    int getWidth();
    int getHeight();
    int getDepth();
    Plane withDepth(int d);
    java.awt.Image toImage();
    java.awt.Image toImage(Rendering rend);
    java.awt.Image toImage(int width, int height);
    java.awt.Image toImage(Rendering rend, int width, int height);
    BufferedImage toBufferedImage();
    BufferedImage toBufferedImage(Rendering rend);
    Image toJfxImage();
    Image toJfxImage(WritableImage jfxImage);
    Plane scale(float scale);
    Plane scale(float scale, boolean antialias);
    Plane subplane(int x1, int y1, int x2, int y2);
    void lockRead();
    void unlockRead();
    void lockWrite();
    void unlockWrite();
    JsonElement toJson();
    void export(PrintWriter w) throws IOException;
    void save(String file, Rendering r) throws IOException;
    byte next(int pattern);
    void tick();
    Pen pen();
    Probe probe();
}
