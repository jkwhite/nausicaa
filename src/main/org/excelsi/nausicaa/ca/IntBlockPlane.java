package org.excelsi.nausicaa.ca;


import java.awt.image.*;
import java.awt.Image;
import java.io.*;
import javafx.scene.image.WritableImage;


public class IntBlockPlane extends AbstractPlane {
    private final CA _ca;
    private final int _w;
    private final int _h;
    private final int _d;
    private final int _hstride;
    private final int _dstride;
    private final int[] _s;
    private final Palette _p;
    private final BufferedImage _i;
    private final WritableRaster _r;
    private final int[][] _unpacked;
    //private int _lockOwner = -1;


    public IntBlockPlane(CA ca, int w, int h, int d, Palette p) {
        this(ca, w, h, d, p, new int[w*h*d]);
    }

    public IntBlockPlane(CA ca, int w, int h, int d, Palette p, int[] s) {
        _ca = ca;
        _w = w;
        _h = h;
        _d = d;
        _p = p;
        _unpacked = p.unpack();
        _s = s;
        _hstride = w;
        _dstride = _hstride*_h;
        _i = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);
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

    public Palette getPalette() {
        return _p;
    }

    @Override public void init() {
    }

    @Override public void setCell(int x, int y, int v) {
        setCell(x, y, 0, v);
        //throw new UnsupportedOperationException();
    }

    public void setCell(int x, int y, int z, int v) {
        //try {
            _s[x+_hstride*y+_dstride*z] = v;
            //if(_m==Mode.indexed && z==0) {
                //_r.setSample(x,y,0,v);
            //}
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

    public int getCell(int x, int y, int z) {
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

    public int[] getBlock(int[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
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

    /*
    public int[] getBlockNew(final int[] into, final int x, final int y, final int z, final int dx, final int dy, final int dz, final int offset) {
        if(x<0||x+dx>=_w||y<0||y+dy>=_h||z<0||z+dz>=_d) {
            getBlockOld(into, x, y, z, dx, dy, dz, offset);
        }
        else {
            //System.err.println("x="+x+", y="+y+", z="+z+", dx="+dx+", dy="+dy+", dz="+dz);
            int idx=offset;
            for(int k=z;k<z+dz;k++) {
                for(int j=y;j<y+dy;j++) {
                    //for(int i=x;i<x+dx;i++) {
                        //System.err.println(i+", "+j+", "+k+" @ "+idx);
                        //into[idx++] = getCell(i,j,k);
                    //}
                    System.arraycopy(_s, x+_hstride*j+_dstride*k, into, idx, dx);
                    idx+=dx;
                }
            }
        }
        return into;
    }
    */

    @Override public void setRow(int[] row, int y) {
        throw new UnsupportedOperationException();
    }

    private final int[] _rgb = new int[3];
    private final boolean FIRST = true;
    @Override public java.awt.Image toImage() {
        if(false) {
            return _i;
        }
        else {
            //final BufferedImage p = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_INDEXED, _p.toColorModel());
            //final WritableRaster r = p.getRaster();
            //System.err.print(".");
            //final int[] rgba = new int[4];
            final int[] rgb = _rgb; //new int[3];
            final int[][] unpacked = _unpacked;
            for(int i=0;i<_w;i++) {
                for(int j=0;j<_h;j++) {
                    rgb[0]=0;
                    rgb[1]=0;
                    rgb[2]=0;
                    int mx = 0;
                    if(_d==1) {
                        int idx = getCell(i,j,0);
                        //if(idx<0) idx=-idx;
                        //if(idx>_p.getColorCount()) idx = idx % _p.getColorCount();
                        _i.setRGB(i,j,_p.color(idx));
                    }
                    else {
                        for(int k=0;k<_d;k++) {
                            //Colors.unpack(_p.color(getCell(i,j,k)), rgba);
                            int idx = getCell(i,j,k);
                            //if(idx<0) idx=-idx;
                            //if(idx>_p.getColorCount()) idx = idx % _p.getColorCount();
                            final int[] u = unpacked[idx];
                            //rgb[0] += (_d-k)*rgba[0];
                            //rgb[1] += (_d-k)*rgba[1];
                            //rgb[2] += (_d-k)*rgba[2];
                            //rgb[0] += (_d-k)*u[0];
                            //rgb[1] += (_d-k)*u[1];
                            //rgb[2] += (_d-k)*u[2];
                            float mult = ((_d-k)/(float)_d);
                            rgb[0] += (int)(mult*u[0]);
                            rgb[1] += (int)(mult*u[1]);
                            rgb[2] += (int)(mult*u[2]);
                            //if(u[0]>0) System.err.println("mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                            mx += (_d-k);
                            if(FIRST&&(u[0]>0||u[1]>0||u[2]>0)) break;
                        }
                        mx = 1;
                        _i.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
                    }
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
        int[] sc = new int[_s.length];
        System.arraycopy(_s, 0, sc, 0, _s.length);
        return new IntBlockPlane(_ca, _w, _h, _d, _p, sc);
    }

    @Override public Plane scale(float scale) {
        throw new UnsupportedOperationException();
    }

    @Override public Plane scale(float scale, boolean antialias) {
        throw new UnsupportedOperationException();
    }

    @Override public void save(String filename) throws IOException {
        Pipeline.write(toBufferedImage(), filename);
    }

    @Override public Plane subplane(int x1, int y1, int x2, int y2) {
        throw new UnsupportedOperationException();
    }

    @Override public byte next(int pattern) {
        throw new UnsupportedOperationException();
    }

    @Override public void tick() {
    }
//
    //@Override public synchronized boolean lock(int id) {
        //if(_lockOwner==id) {
            //return true;
        //}
        //int c = 0;
        //while(_lockOwner!=-1) {
            //if(++c%10000==0) System.err.println(System.identityHashCode(this)+" o"+_lockOwner+" w"+id);
        //}
        //_lockOwner = id;
        //return true;
    //}
//
    //@Override public void unlock() {
        //_lockOwner = -1;
    //}

    protected int[] getBuffer() {
        return _s;
    }

    private final int normX(int x) {
        if(x<0) {
            x = (_w+x)%_w;
            //if(x<0) throw new IllegalArgumentException("neg x: "+x);
        }
        else if(x>=_w) {
            x = x % _w;
        }
        return x;
    }

    private final int normY(int y) {
        if(y<0) {
            y = (_h+y)%_h;
        }
        else if(y>=_h) {
            y = y % _h;
        }
        return y;
    }

    private final int normZ(int z) {
        if(z<0) {
            z = (_d+z)%_d;
        }
        else if(z>=_d) {
            z = z % _d;
        }
        return z;
    }
}
