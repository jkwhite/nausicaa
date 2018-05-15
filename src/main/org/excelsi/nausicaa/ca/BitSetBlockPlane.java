package org.excelsi.nausicaa.ca;


import java.awt.image.*;
import java.awt.Image;
import java.io.*;
import java.util.*;
import javafx.scene.image.WritableImage;


public class BitSetBlockPlane extends AbstractPlane {
    private final CA _ca;
    private final int _w;
    private final int _h;
    private final int _d;
    private final int _hstride;
    private final int _dstride;
    private final BitSet _s;


    public BitSetBlockPlane(CA ca, int w, int h, int d) {
        this(ca, w, h, d, new BitSet(w*h*d));
    }

    public BitSetBlockPlane(CA ca, int w, int h, int d, BitSet s) {
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
        setCell(x, y, 0, v);
        //throw new UnsupportedOperationException();
    }

    public void setCell(int x, int y, int z, int v) {
        //try {
            if(v>0) {
                _s.set(x+_hstride*y+_dstride*z);
            }
            else {
                _s.clear(x+_hstride*y+_dstride*z);
            }
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
            final int idx = nx+_hstride*ny+_dstride*nz;
            //if(idx==12) {
                //System.err.println("("+x+","+y+","+z+") => ("+nx+","+ny+","+nz+") => "+idx);
            //}
            return _s.get(idx)?1:0;
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

    @Override public int[] getCardinal(int[] into, int x, int y, int dx, int dy, int offset) {
        into[offset++] = getCell(x+1,y,0);
        into[offset++] = getCell(x,y-1,0);
        into[offset++] = getCell(x,y,0);
        into[offset++] = getCell(x,y+1,0);
        into[offset++] = getCell(x-1,y,0);
        return into;
    }

    @Override public int[] getCardinal(int[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        into[offset++] = getCell(x,y+1,0);
        into[offset++] = getCell(x,y-1,0);
        into[offset++] = getCell(x+1,y,0);
        into[offset++] = getCell(x,y,0);
        into[offset++] = getCell(x-1,y,0);
        into[offset++] = getCell(x,y,z-1);
        into[offset++] = getCell(x,y,z+1);
        return into;
    }

    public int[] getBlock(int[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        //System.err.println("x="+x+", y="+y+", z="+z+", dx="+dx+", dy="+dy+", dz="+dz);
        int idx=offset;
        for(int i=x;i<x+dx;i++) {
            for(int j=y;j<y+dy;j++) {
                for(int k=z;k<z+dz;k++) {
                    //System.err.println(i+", "+j+", "+k+" @ "+idx);
                    final int v = getCell(i,j,k);
                    //if(x==4&&y==2&&z==0) {
                        //if(v!=0) {
                            //System.err.println("got value "+v+" for ("+x+","+y+","+z+")");
                        //}
                    //}
                    into[idx++] = v;
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

    @Override public java.awt.Image toImage(Rendering rend) {
        return toImage();
    }

    private final int[] _rgb = new int[3];
    private final boolean FIRST = true;
    @Override public java.awt.Image toImage() {
        final BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        //final WritableRaster r = i.getRaster();
        //System.err.print(".");
        //final int[] rgba = new int[4];
        final int[] rgb = _rgb; //new int[3];
        //final int[][] unpacked = _unpacked;
        int BLACK = Colors.pack(0,0,0);
        int WHITE = Colors.pack(255,255,255);
        int[] UBLACK = Colors.unpack(BLACK);
        int[] UWHITE = Colors.unpack(WHITE);
        for(int i=0;i<_w;i++) {
            for(int j=0;j<_h;j++) {
                //rgb[0]=0;
                //rgb[1]=0;
                //rgb[2]=0;
                int mx = 0;
                if(_d==1) {
                    int idx = getCell(i,j,0);
                    //if(idx<0) idx=-idx;
                    //if(idx>_p.getColorCount()) idx = idx % _p.getColorCount();
                    //i.setRGB(i,j,_p.color(idx));
                    if(idx==0) {
                        bi.setRGB(0,0,0);
                    }
                    else {
                        bi.setRGB(255,255,255);
                    }
                }
                else {
                    for(int k=0;k<_d;k++) {
                        //Colors.unpack(_p.color(getCell(i,j,k)), rgba);
                        int idx = getCell(i,j,k);
                        //if(idx<0) idx=-idx;
                        //if(idx>_p.getColorCount()) idx = idx % _p.getColorCount();
                        final int[] u = idx==1?UWHITE:UBLACK;
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
                        //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                        mx += (_d-k);
                        if(FIRST&&(u[0]>0||u[1]>0||u[2]>0)) break;
                    }
                    mx = 1;
                    bi.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
                }
            }
        }
        return bi;
    }

    @Override public java.awt.Image toImage(int width, int height) {
        return toImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override public java.awt.Image toImage(Rendering rend, int width, int height) {
        return toImage(width, height);
    }

    @Override public BufferedImage toBufferedImage() {
        return (BufferedImage) toImage();
    }

    @Override public BufferedImage toBufferedImage(Rendering rend) {
        return (BufferedImage) toImage(rend);
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
        //int[] sc = new int[_s.length];
        //System.arraycopy(_s, 0, sc, 0, _s.length);
        //return new IntBlockPlane(_ca, _w, _h, _d, _p, sc);
        BitSet s = (BitSet) _s.clone();
        return new BitSetBlockPlane(_ca, _w, _h, _d, s);
    }

    @Override public Plane withDepth(int d) {
        throw new UnsupportedOperationException();
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

    //protected int[] getBuffer() {
        //return _s;
    //}

    private final int normX(int x) {
        return norm(x, _w);
        /*
        if(x<0) {
            x = (_w+x)%_w;
            //if(x<0) throw new IllegalArgumentException("neg x: "+x);
        }
        else if(x>=_w) {
            x = x % _w;
        }
        return x;
        */
    }

    private final int normY(int y) {
        return norm(y, _h);
        /*
        if(y<0) {
            y = (_h+y)%_h;
        }
        else if(y>=_h) {
            y = y % _h;
        }
        return y;
        */
    }

    private final int normZ(int z) {
        return norm(z, _d);
        /*
        if(z<0) {
            z = (_d+z)%_d;
        }
        else if(z>=_d) {
            z = z % _d;
        }
        return z;
        */
    }

    private static final int norm(int v, final int m) {
        if(v<0) {
            //System.err.print("v="+v+", m="+m);
            v = (m+v)%m;
            //System.err.println(", nv="+v);
        }
        else if(v>=m) {
            //System.err.print("v="+v+", m="+m);
            v = v % m;
            //System.err.println(", nv="+v);
        }
        return v;
    }
}
