package org.excelsi.nausicaa.ca;


import java.awt.image.*;
import java.awt.Image;
import java.io.*;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
    


public class FloatBlockPlane extends AbstractFloatPlane implements Sliceable {
    private final CA _ca;
    private final int _w;
    private final int _h;
    private final int _d;
    private final int _hstride;
    private final int _dstride;
    private final float[] _s;
    private final Palette _p;
    private final BufferedImage _i;
    private final WritableRaster _r;
    private final boolean _wrap;
    private final float _oob;
    private int _readDepthIdx;
    private int _writeDepthIdx;


    public FloatBlockPlane(CA ca, int w, int h, int d, Palette p, Float oob) {
        this(ca, w, h, d, p, oob, new float[w*h*d]);
    }

    public FloatBlockPlane(CA ca, int w, int h, int d, Palette p, Float oob, float[] s) {
        _ca = ca;
        _w = w;
        _h = h;
        _d = d;
        _p = p;
        _s = s;
        _hstride = w;
        _dstride = _hstride*_h;
        _i = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_ARGB);
        _r = _i.getRaster();
        if(oob!=null) {
            _wrap = false;
            _oob = oob.floatValue();
        }
        else {
            _wrap = true;
            _oob = 0;
        }
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

    @Override public void setCell(int x, int y, float v) {
        setCell(x, y, _writeDepthIdx, v);
    }

    public void setCell(int x, int y, int z, float v) {
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

    @Override public float getCell(int x, int y) {
        return getCell(x, y, _readDepthIdx);
    }

    public float getCell(int x, int y, int z) {
        int nx = normX(x);
        int ny = normY(y);
        int nz = normZ(z);
        if(!_wrap && (nx!=x||ny!=y||nz!=z)) {
            return _oob;
        }
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

    @Override public float[] getRow(float[] into, int y, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override public float[] getBlock(float[] into, int x, int y, int dx, int dy, int offset) {
        //throw new UnsupportedOperationException();
        return getBlock(into, x, y, /*z*/ _readDepthIdx, dx, dy, /*dz*/ 1, offset);
    }

    @Override public float[] getCardinal(float[] into, int x, int y, int dx, int dy, int offset) {
        into[offset++] = getCell(x+1,y,_readDepthIdx);
        into[offset++] = getCell(x,y-1,_readDepthIdx);
        into[offset++] = getCell(x,y,_readDepthIdx);
        into[offset++] = getCell(x,y+1,_readDepthIdx);
        into[offset++] = getCell(x-1,y,_readDepthIdx);
        return into;
    }

    @Override public float[] getCardinal(float[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        into[offset++] = getCell(x,y+1,z);
        into[offset++] = getCell(x,y-1,z);
        into[offset++] = getCell(x+1,y,z);
        into[offset++] = getCell(x,y,z);
        into[offset++] = getCell(x-1,y,z);
        into[offset++] = getCell(x,y,z-1);
        into[offset++] = getCell(x,y,z+1);
        return into;
    }

    public float[] getBlock(float[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        //System.err.println("x="+x+", y="+y+", z="+z+", dx="+dx+", dy="+dy+", dz="+dz);
        int idx=offset;
        for(int i=x;i<x+dx;i++) {
            for(int j=y;j<y+dy;j++) {
                for(int k=z;k<z+dz;k++) {
                    //System.err.println(i+", "+j+", "+k+" @ "+idx);
                    final float v = getCell(i,j,k);
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

    @Override public void setRow(float[] row, int y) {
        throw new UnsupportedOperationException();
    }

    private final float[] _rgb = new float[4];
    private final int[] _irgb = new int[4];
    private final int[] _unpack = new int[4];
    private final boolean FIRST = true;

    //private final Composition _cmode = Composition.front;
    private final Rendering DEFAULT_RENDERING = new Rendering();
    @Override public java.awt.Image toImage() {
        return toImage(DEFAULT_RENDERING);
    }

    private int computeColor2(final float idx) {
        if(_ca.archetype().colors()<_p.getColorCount()) {
            final int pc = _p.getColorCount();
            final int ac = _ca.archetype().colors();
            final float acp = idx/ac;
            final int pcp = (int)(pc*acp);
            return _p.color(pcp);
        }
        else {
            return computeColor(idx);
        }
    }

    private int computeColor(final float idx) {
        final float ceil = (float)Math.ceil(idx);
        final float floor = (float)Math.floor(idx);
        final int p1 = _p.color((int)ceil);
        final int p2 = _p.color((int)floor);
        int c = Colors.avg(p1,p2,1f-(ceil-idx));
        //System.err.println("to rgb color: "+c+" for p1="+p1+",p2="+p2+",idx="+idx);
        return c;
        //final int pc = _p.getColorCount();
        //final int ac = _ca.archetype().colors();
        //final float acp = idx/ac;
        //final int pcp = (int)(pc*acp);
        //return _p.color(pcp);
    }

    @Override public java.awt.Image toImage(Rendering rend) {
        final Rendering.Composition comp = rend.composition();
        if(false) {
            return _i;
        }
        else {
            final float[] rgb = _rgb;
            final int[] irgb = _irgb;
            for(int i=0;i<_w;i++) {
                for(int j=0;j<_h;j++) {
                    rgb[0]=0;
                    rgb[1]=0;
                    rgb[2]=0;
                    irgb[0]=0;
                    irgb[1]=0;
                    irgb[2]=0;
                    int mx = 0;
                    if(_d==1) {
                        float idx = getCell(i,j,0);
                        _i.setRGB(i,j,computeColor2(idx));
                        //_i.setRGB(i,j,_p.color(idx));
                    }
                    else {
                        //System.err.println("comp: "+comp+", d: "+_d);
                        if(comp==Rendering.Composition.channel) {
                            final float max = creator().archetype().colors()-1f;
                            if(_d==3) {
                                rgb[0] = getCell(i,j,0);
                                rgb[1] = getCell(i,j,1);
                                rgb[2] = getCell(i,j,2);
                                final int pkd = Colors.packBounded(rgb[0]/max,rgb[1]/max,rgb[2]/max);
                                //if(pkd!=0) Colors.dump(pkd, "3col");
                                _i.setRGB(i,j,pkd);
                            }
                            else if(_d==4) {
                                rgb[0] = getCell(i,j,0);
                                rgb[1] = getCell(i,j,1);
                                rgb[2] = getCell(i,j,2);
                                rgb[3] = getCell(i,j,3);
                                _i.setRGB(i,j,Colors.packBounded(rgb[0]/max,rgb[1]/max,rgb[2]/max,rgb[3]/max));
                            }
                        }
                        else if(comp==Rendering.Composition.front||comp==Rendering.Composition.wavg) {
                            for(int k=0;k<_d;k++) {
                                float idx = getCell(i,j,k);
                                int packed = computeColor2(idx);
                                //Colors.dump(packed, "float0");
                                //final int[] u = _p.unpack(idx, _unpack);
                                final int[] u = Colors.unpack(packed, _unpack);
                                //System.err.println("u: "+java.util.Arrays.toString(u));
                                float mult = ((_d-k)/(float)_d);
                                //System.err.println("mult: "+mult+", k: "+k);
                                irgb[0] += (int)(mult*u[0]);
                                irgb[1] += (int)(mult*u[1]);
                                irgb[2] += (int)(mult*u[2]);
                                //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                                mx += (_d-k);
                                if(rend.composition()==Rendering.Composition.front&&(u[0]>0||u[1]>0||u[2]>0)) break;
                            }
                            mx = 1;
                            int pkd = Colors.pack(irgb[2], irgb[1], irgb[0]);
                            //System.err.println("rgb: "+java.util.Arrays.toString(irgb));
                            //Colors.dump(pkd, "float1");
                            _i.setRGB(i,j,pkd);
                        }
                        /*
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
                        */
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
        float[] sc = new float[_s.length];
        System.arraycopy(_s, 0, sc, 0, _s.length);
        return new FloatBlockPlane(_ca, _w, _h, _d, _p, oob(), sc);
    }

    @Override public Plane withDepth(int d) {
        FloatBlockPlane p = new FloatBlockPlane(_ca, _w, _h, d, _p, oob());
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
        //return new BufferedImagePlane((BufferedImage)toImage()).scale(scale);
        throw new UnsupportedOperationException();
    }

    @Override public Plane scale(float scale, boolean antialias) {
        //return new BufferedImagePlane((BufferedImage)toImage()).scale(scale, antialias);
        throw new UnsupportedOperationException();
    }

    @Override public void save(String filename, Rendering r) throws IOException {
        Pipeline.write(toBufferedImage(r), filename);
    }

    @Override public Plane subplane(int x1, int y1, int x2, int y2) {
        throw new UnsupportedOperationException();
    }

    @Override public byte next(int pattern) {
        throw new UnsupportedOperationException();
    }

    @Override public void tick() {
    }

    @Override public void setReadDepth(int idx) {
        _readDepthIdx = idx;
    }

    @Override public void setWriteDepth(int idx) {
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

    protected float[] getBuffer() {
        return _s;
    }

    protected Float oob() {
        return _wrap?null:_oob;
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
