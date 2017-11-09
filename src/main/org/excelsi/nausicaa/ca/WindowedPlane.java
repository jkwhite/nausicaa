package org.excelsi.nausicaa.ca;


import javax.swing.ImageIcon;
import java.io.IOException;
import java.awt.Image;
import java.awt.image.BufferedImage;


public class WindowedPlane extends AbstractPlane {
    private final Plane _p;
    private final int _x1;
    private final int _y1;
    private final int _x2;
    private final int _y2;


    public WindowedPlane(final Plane p, final int x1, final int y1, final int x2, final int y2) {
        _p = p;
        _x1 = x1;
        _y1 = y1;
        _x2 = x2;
        _y2 = y2;
    }

    @Override public int getWidth() {
        return _x2 - _x1;
    }

    @Override public int getHeight() {
        return _y2 - _y1;
    }

    @Override public int getDepth() {
        return _p.getDepth();
    }

    @Override public void init() {
        _p.init();
    }

    @Override public void setCell(int x, int y, int v) {
        _p.setCell(x+_x1, y+_y1, v);
    }

    @Override public void setRGBCell(int x, int y, int rgb) {
        _p.setRGBCell(x+_x1, y+_y1, rgb);
    }

    @Override public int getCell(int x, int y) {
        return _p.getCell(x+_x1, y+_y1);
    }

    @Override public int[] getRow(int[] into, int y, int offset) {
        return _p.getRow(into, y+_y1, offset);
    }

    @Override public int[] getBlock(int[] into, int x, int y, int dx, int dy, int offset) {
        return _p.getBlock(into, x+_x1, y+_y1, dx, dy, offset);
    }

    @Override public void setRow(int[] row, int y) {
        _p.setRow(row, y+_y1);
    }

    @Override public java.awt.Image toImage() {
        return _p.toImage();
    }

    @Override public java.awt.Image toImage(int width, int height) {
        return _p.toImage(width, height);
    }

    @Override public BufferedImage toBufferedImage() {
        return _p.toBufferedImage();
    }

    @Override public javafx.scene.image.Image toJfxImage() {
        return _p.toJfxImage();
    }

    @Override public javafx.scene.image.Image toJfxImage(javafx.scene.image.WritableImage img) {
        return _p.toJfxImage(img);
    }

    @Override public CA creator() {
        return _p.creator();
    }

    @Override public Plane copy() {
        return new WindowedPlane(_p.copy(), _x1, _y1, _x2, _y2);
    }

    @Override public Plane scale(float scale) {
        return new WindowedPlane(_p.scale(scale), _x1, _y1, _x2, _y2);
    }

    @Override public Plane scale(float scale, boolean antialias) {
        return new WindowedPlane(_p.scale(scale, antialias), _x1, _y1, _x2, _y2);
    }

    @Override public void save(String file) throws IOException {
        _p.save(file);
    }

    @Override public Plane subplane(int x1, int y1, int x2, int y2) {
        return new WindowedPlane(this, x1, y1, x2, y2);
    }

    @Override public byte next(final int pattern) {
        final int y = pattern/getWidth();
        final int x = pattern%getWidth();
        return (byte) getCell(x, y);
    }

    @Override public void tick() {
    }
//
    //@Override public boolean lock(int id) {
        //return _p.lock(id);
    //}
//
    //@Override public void unlock() {
        //_p.unlock();
    //}
}
