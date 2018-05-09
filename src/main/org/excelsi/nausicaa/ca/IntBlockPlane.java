package org.excelsi.nausicaa.ca;


import java.awt.image.*;
import java.awt.Image;
import java.io.*;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
    


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
    //private final int[][] _unpacked;
    private int _readDepthIdx;
    private int _writeDepthIdx;
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
        //_unpacked = p.unpack();
        _s = s;
        _hstride = w;
        _dstride = _hstride*_h;
        _i = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);
        _r = _i.getRaster();
        //_readDepthIdx = -1;
        //_writeDepthIdx = -1;
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
        setCell(x, y, _writeDepthIdx, v);
    }

    public void setCell(int x, int y, int z, int v) {
        //try {
            //_s[x+_hstride*y+_dstride*z] = v;
            _s[normX(x)+_hstride*normY(y)+_dstride*normZ(z)] = v;
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
        return getCell(x, y, _readDepthIdx);
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
            return _s[idx];
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
        //throw new UnsupportedOperationException();
        return getBlock(into, x, y, /*z*/ _readDepthIdx, dx, dy, /*dz*/ 1, offset);
    }

    @Override public int[] getCardinal(int[] into, int x, int y, int z, int offset) {
        into[0] = getCell(x-1,y,z);
        into[1] = getCell(x+1,y,z);
        into[2] = getCell(x,y-1,z);
        into[3] = getCell(x,y+1,z);
        into[4] = getCell(x,y,z-1);
        into[5] = getCell(x,y,z+1);
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

    private final int[] _rgb = new int[3];
    private final int[] _unpack = new int[4];
    private final boolean FIRST = true;

    //private final Composition _cmode = Composition.front;
    private final Rendering DEFAULT_RENDERING = new Rendering();
    @Override public java.awt.Image toImage() {
        return toImage(DEFAULT_RENDERING);
    }

    @Override public java.awt.Image toImage(Rendering rend) {
        final Rendering.Composition comp = rend.composition();
        if(false) {
            return _i;
        }
        else {
            final int[] rgb = _rgb;
            for(int i=0;i<_w;i++) {
                for(int j=0;j<_h;j++) {
                    rgb[0]=0;
                    rgb[1]=0;
                    rgb[2]=0;
                    int mx = 0;
                    if(_d==1) {
                        int idx = getCell(i,j,0);
                        _i.setRGB(i,j,_p.color(idx));
                    }
                    else {
                        if(comp==Rendering.Composition.front||comp==Rendering.Composition.wavg) {
                            for(int k=0;k<_d;k++) {
                                int idx = getCell(i,j,k);
                                final int[] u = _p.unpack(idx, _unpack);
                                float mult = ((_d-k)/(float)_d);
                                rgb[0] += (int)(mult*u[0]);
                                rgb[1] += (int)(mult*u[1]);
                                rgb[2] += (int)(mult*u[2]);
                                //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                                mx += (_d-k);
                                if(rend.composition()==Rendering.Composition.front&&(u[0]>0||u[1]>0||u[2]>0)) break;
                            }
                            mx = 1;
                            _i.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
                        }
                        else if(comp==Rendering.Composition.back||comp==Rendering.Composition.revwavg) {
                            for(int k=_d-1;k>=0;k--) {
                                int idx = getCell(i,j,k);
                                final int[] u = _p.unpack(idx, _unpack);
                                float mult = ((1+k)/(float)_d);
                                rgb[0] += (int)(mult*u[0]);
                                rgb[1] += (int)(mult*u[1]);
                                rgb[2] += (int)(mult*u[2]);
                                //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                                mx += (_d-k);
                                if(rend.composition()==Rendering.Composition.back&&(u[0]>0||u[1]>0||u[2]>0)) break;
                            }
                            mx = 1;
                            _i.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
                        }
                        else if(comp==Rendering.Composition.avg) {
                            for(int k=0;k<_d;k++) {
                                int idx = getCell(i,j,k);
                                final int[] u = _p.unpack(idx, _unpack);
                                //float mult = ((1+k)/(float)_d);
                                rgb[0] += u[0];
                                rgb[1] += u[1];
                                rgb[2] += u[2];
                                //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                                mx += (_d-k);
                                //if(rend.composition()==Rendering.Composition.back&&(u[0]>0||u[1]>0||u[2]>0)) break;
                            }
                            mx = _d;
                            _i.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
                        }
                    }
                }
            }
            return _i;
        }
    }

    @Override public java.awt.Image toImage(int width, int height) {
        return toImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override public java.awt.Image toImage(Rendering rend, int width, int height) {
        return toImage(rend).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override public BufferedImage toBufferedImage() {
        return (BufferedImage) toImage();
    }

    @Override public BufferedImage toBufferedImage(Rendering rend) {
        return (BufferedImage) toImage(rend);
    }

    @Override public javafx.scene.image.Image toJfxImage() {
        return SwingFXUtils.toFXImage(toBufferedImage(), null);
        //throw new UnsupportedOperationException();
    }

    @Override public javafx.scene.image.Image toJfxImage(WritableImage jfxImage) {
        //throw new UnsupportedOperationException();
        return SwingFXUtils.toFXImage(toBufferedImage(), jfxImage);
    }

    @Override public CA creator() {
        return _ca;
    }

    @Override public Plane copy() {
        int[] sc = new int[_s.length];
        System.arraycopy(_s, 0, sc, 0, _s.length);
        return new IntBlockPlane(_ca, _w, _h, _d, _p, sc);
    }

    @Override public Plane withDepth(int d) {
        IntBlockPlane p = new IntBlockPlane(_ca, _w, _h, d, _p);
        int md = _d<d?_d:d;
        for(int i=0;i<_w;i++) {
            for(int j=0;j<_h;j++) {
                for(int k=0;k<md;k++) {
                    p.setCell(i, j, k, getCell(i, j, k));
                }
            }
        }
        return p;
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

    public void setReadDepth(int idx) {
        _readDepthIdx = idx;
    }

    public void setWriteDepth(int idx) {
        _writeDepthIdx = idx;
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
