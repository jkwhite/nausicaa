package org.excelsi.nausicaa.ca;


import java.awt.image.*;
import java.awt.Image;
import java.io.*;
import javafx.scene.image.WritableImage;


public final class BlockPlane implements Plane {
    private final CA _ca;
    private final int _w;
    private final int _h;
    private final int _d;
    private final int _hstride;
    private final int _dstride;
    private final byte[] _s;


    public BlockPlane(CA ca, int w, int h, int d) {
        this(ca, w, h, d, new byte[w*h*d]);
    }

    public BlockPlane(CA ca, int w, int h, int d, byte[] s) {
        _ca = ca;
        _w = w;
        _h = h;
        _d = d;
        _s = s;
        _hstride = w;
        _dstride = _hstride*_h;
    }

    @Override public int getWidth() {
        return _w;
    }

    @Override public int getHeight() {
        return _h;
    }

    public int getDepth() {
        return _d;
    }

    @Override public void init() {
    }

    @Override public void setCell(int x, int y, int v) {
        throw new UnsupportedOperationException();
    }

    public void setCell(int x, int y, int z, int v) {
        try {
            _s[x+_hstride*y+_dstride*z] = (byte)v;
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.err.println("x="+x+", y="+y+", z="+z+", hstride="+_hstride+", dstride="+_dstride);
            throw new IllegalArgumentException(e);
        }
    }

    @Override public void setRGBCell(int x, int y, int rgb) {
        throw new UnsupportedOperationException();
    }

    @Override public int getCell(int x, int y) {
        throw new UnsupportedOperationException();
    }

    public byte getCell(int x, int y, int z) {
        int nx = normX(x);
        int ny = normY(y);
        int nz = normZ(z);
        try {
            return _s[nx+_hstride*ny+_dstride*nz];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.err.println("x="+x+", y="+y+", z="+z+", nx="+nx+", ny="+ny+", nz="+nz+", hstride="+_hstride+", dstride="+_dstride);
            throw new IllegalArgumentException(e);
        }
    }

    @Override public int[] getRow(int[] into, int y, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override public int[] getBlock(int[] into, int x, int y, int dx, int dy, int offset) {
        throw new UnsupportedOperationException();
    }

    public byte[] getBlock(byte[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        //System.err.println("x="+x+", y="+y+", z="+z+", dx="+dx+", dy="+dy+", dz="+dz);
        int idx=offset;
        for(int i=x;i<x+dx;i++) {
            for(int j=y;j<y+dy;j++) {
                for(int k=z;k<z+dz;k++) {
                    //System.err.println(i+", "+j+", "+k+" @ "+idx);
                    into[idx++] = getCell(i,j,k);
                }
            }
        }
        return into;
    }

    @Override public void setRow(int[] row, int y) {
        throw new UnsupportedOperationException();
    }

    @Override public java.awt.Image toImage() {
        throw new UnsupportedOperationException();
    }

    @Override public java.awt.Image toImage(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override public BufferedImage toBufferedImage() {
        throw new UnsupportedOperationException();
    }

    @Override public javafx.scene.image.Image toJfxImage() {
        throw new UnsupportedOperationException();
    }

    @Override public javafx.scene.image.Image toJfxImage(WritableImage jfxImage) {
        throw new UnsupportedOperationException();
    }

    @Override public CA creator() {
        return _ca;
    }

    @Override public Plane copy() {
        byte[] sc = new byte[_s.length];
        System.arraycopy(_s, 0, sc, 0, _s.length);
        return new BlockPlane(_ca, _w, _h, _d, sc);
    }

    @Override public Plane scale(float scale) {
        throw new UnsupportedOperationException();
    }

    @Override public Plane scale(float scale, boolean antialias) {
        throw new UnsupportedOperationException();
    }

    @Override public void save(String file) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public Plane subplane(int x1, int y1, int x2, int y2) {
        throw new UnsupportedOperationException();
    }

    @Override public byte next(int pattern) {
        throw new UnsupportedOperationException();
    }

    @Override public void tick() {
    }

    private final int normX(int x) {
        if(x<0) {
            x = _w-1;
        }
        else if(x>=_w) {
            x = x % _w;
        }
        return x;
    }

    private final int normY(int y) {
        if(y<0) {
            y = _h-1;
        }
        else if(y>=_h) {
            y = y % _h;
        }
        return y;
    }

    private final int normZ(int z) {
        if(z<0) {
            z = _d-1;
        }
        else if(z>=_d) {
            z = z % _d;
        }
        return z;
    }
}
