package org.excelsi.nausicaa.ca;


import java.awt.image.*;
import java.awt.Image;
import java.io.*;
import javafx.scene.image.WritableImage;


public final class BlockPlane implements Plane {
    public enum Mode { indexed, argb };

    private final CA _ca;
    private final int _w;
    private final int _h;
    private final int _d;
    private final int _hstride;
    private final int _dstride;
    private final byte[] _s;
    private final Palette _p;
    private final BufferedImage _i;
    private final WritableRaster _r;
    private final Mode _m;


    public BlockPlane(CA ca, int w, int h, int d, Palette p, Mode m) {
        this(ca, w, h, d, p, m, new byte[w*h*d]);
    }

    public BlockPlane(CA ca, int w, int h, int d, Palette p, Mode m, byte[] s) {
        _ca = ca;
        _w = w;
        _h = h;
        _d = d;
        _p = p;
        _s = s;
        _hstride = w;
        _dstride = _hstride*_h;
        _m = m;
        switch(m) {
            case argb:
                _i = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);
                break;
            default:
            case indexed:
                _i = new BufferedImage(_w, _h, BufferedImage.TYPE_BYTE_INDEXED, _p.toColorModel());
                break;
        }
        _r = _i.getRaster();
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
        //try {
            _s[x+_hstride*y+_dstride*z] = (byte)v;
            if(_m==Mode.indexed && z==0) {
                _r.setSample(x,y,0,v);
            }
        //}
        //catch(ArrayIndexOutOfBoundsException e) {
            //System.err.println("x="+x+", y="+y+", z="+z+", hstride="+_hstride+", dstride="+_dstride);
            //throw new IllegalArgumentException(e);
        //}
    }

    @Override public void setRGBCell(int x, int y, int rgb) {
        throw new UnsupportedOperationException();
    }

    @Override public int getCell(int x, int y) {
        return getCell(x, y, 0);
    }

    public byte getCell(int x, int y, int z) {
        int nx = normX(x);
        int ny = normY(y);
        int nz = normZ(z);
        //try {
            return _s[nx+_hstride*ny+_dstride*nz];
        //}
        //catch(ArrayIndexOutOfBoundsException e) {
            //System.err.println("x="+x+", y="+y+", z="+z+", nx="+nx+", ny="+ny+", nz="+nz+", hstride="+_hstride+", dstride="+_dstride);
            //throw new IllegalArgumentException(e);
        //}
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
        if(_m==Mode.indexed) {
            return _i;
        }
        else {
            //final BufferedImage p = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_INDEXED, _p.toColorModel());
            //final WritableRaster r = p.getRaster();
            //System.err.print(".");
            final int[] rgba = new int[4];
            final int[] rgb = new int[3];
            for(int i=0;i<_w;i++) {
                for(int j=0;j<_h;j++) {
                    rgb[0]=0;
                    rgb[1]=0;
                    rgb[2]=0;
                    int mx = 0;
                    for(int k=0;k<_d;k++) {
                        Colors.unpack(_p.color(getCell(i,j,k)), rgba);
                        rgb[0] += (_d-k)*rgba[0];
                        rgb[1] += (_d-k)*rgba[1];
                        rgb[2] += (_d-k)*rgba[2];
                        mx += (_d-k);
                    }
                    //r.setSample(i, j, 0, getCell(i,j,0));
                    _i.setRGB(i,j,Colors.pack(rgb[0]/mx, rgb[1]/mx, rgb[2]/mx));
                }
            }
            return _i;
        }
    }

    @Override public java.awt.Image toImage(int width, int height) {
        return toImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override public BufferedImage toBufferedImage() {
        return (BufferedImage) toImage();
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
        return new BlockPlane(_ca, _w, _h, _d, _p, _m, sc);
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
