package org.excelsi.nausicaa.ca;


import java.awt.image.*;
import java.awt.Image;
import java.io.*;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
    

public class CompositeFloatPlane extends AbstractFloatPlane implements Sliceable, CompositePlane {
    //private final FloatPlane[] _ps;
    private final Plane[] _ps;
    //private final Plane[] _planes;
    private BufferedImage _i;
    private WritableRaster _r;
    private int _readDepthIdx;
    private int _writeDepthIdx;


    public CompositeFloatPlane(Plane[] ps) {
        _ps = ps;
    }

    @Override public Plane[] planes() {
        return _ps;
    }

    @Override public CompositePlane emptyCopy() {
        return new CompositeFloatPlane(new Plane[_ps.length]);
    }

    @Override public void setReadDepth(int idx) {
        _readDepthIdx = idx;
    }

    @Override public void setWriteDepth(int idx) {
        _writeDepthIdx = idx;
    }

    @Override public void setCell(int x, int y, double v) {
        pw().setCell(x, y, v);
    }

    @Override public void setCell(int x, int y, int z, double v) {
        pw().setCell(x, y, z, v);
    }

    @Override public void setRGBCell(int x, int y, int rgb) {
        pw().setRGBCell(x, y, rgb);
    }

    @Override public double getCell(int x, int y) {
        return pr().getCell(x, y);
    }

    @Override public double getCell(int x, int y, int z) {
        return pr().getCell(x, y, z);
    }

    @Override public double[] getRow(double[] into, int y, int offset) {
        return pr().getRow(into, y, offset);
    }

    @Override public double[] getCoords(double[] into, int x, int y, int[][] coords, int offset) {
        return pr().getCoords(into, x, y, coords, offset);
    }

    @Override public double[] getBlock(double[] into, int x, int y, int dx, int dy, int offset) {
        return pr().getBlock(into, x, y, dx, dy, offset);
    }

    @Override public double[] getBlock(double[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        return pr().getBlock(into, x, y, z, dx, dy, dz, offset);
    }

    @Override public double[] getCardinal(double[] into, int x, int y, int dx, int dy, int offset) {
        return pr().getCardinal(into, x, y, dx, dy, offset);
    }

    @Override public double[] getCardinal(double[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        return pr().getCardinal(into, x, y, z, dx, dy, dz, offset);
    }

    @Override public void setRow(double[] row, int y) {
        pw().setRow(row, y);
    }

    @Override public int getWidth() {
        return pr().getWidth();
    }

    @Override public int getHeight() {
        return pr().getHeight();
    }

    @Override public int getDepth() {
        return pr().getDepth();
    }

    private final Rendering DEFAULT_RENDERING = new Rendering();
    @Override public java.awt.Image toImage() {
        return toImage(DEFAULT_RENDERING);
    }

    private final double[] _rgb = new double[3];
    private final int[] _irgb = new int[4];
    private final int[] _unpack = new int[4];
    @Override public java.awt.Image toImage(Rendering rend) {
        if(_i==null) {
            _i = new BufferedImage(_ps[0].getWidth(), _ps[0].getHeight(), BufferedImage.TYPE_INT_ARGB);
            _r = _i.getRaster();
        }
        final Rendering.Composition comp = rend.composition();
        final double[] rgb = _rgb;
        final int[] irgb = _irgb;
        for(int i=0;i<getWidth();i++) {
            for(int j=0;j<getHeight();j++) {
                rgb[0]=0;
                rgb[1]=0;
                rgb[2]=0;
                irgb[0]=0;
                irgb[1]=0;
                irgb[2]=0;
                int mx = 0;
                /*
                if(_d==1) {
                    int idx = getCell(i,j,0);
                    _i.setRGB(i,j,_p.color(idx));
                }
                else { */
                    if(comp==Rendering.Composition.front||comp==Rendering.Composition.truefront||comp==Rendering.Composition.wavg) {
                        for(int k=0;k<_ps.length;k++) {
                            final FloatPlane fp = p(k);
                            double idx = fp.getCell(i,j);
                            int packed = fp.computeRgbColor(idx);
                            //final int[] u = _ps[k].creator().getPalette().unpack(idx, _unpack);
                            final int[] u = Colors.unpack(packed, _unpack);
                            double mult = ((_ps.length-k)/(double)_ps.length);
                            irgb[0] += (int)(mult*u[0]);
                            irgb[1] += (int)(mult*u[1]);
                            irgb[2] += (int)(mult*u[2]);
                            //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                            mx += (_ps.length-k);
                            if(comp==Rendering.Composition.truefront||comp==Rendering.Composition.front&&(u[0]>0||u[1]>0||u[2]>0)) break;
                        }
                        mx = 1;
                        _i.setRGB(i,j,Colors.pack(irgb[2]/mx, irgb[1]/mx, irgb[0]/mx));
                    }
                    else if(comp==Rendering.Composition.back||comp==Rendering.Composition.revwavg) {
                        for(int k=_ps.length-1;k>=0;k--) {
                            final FloatPlane fp = p(k);
                            double idx = fp.getCell(i,j);
                            int packed = fp.computeRgbColor(idx);
                            //final int[] u = _ps[k].creator().getPalette().unpack(idx, _unpack);
                            final int[] u = Colors.unpack(packed, _unpack);
                            double mult = ((1+k)/(double)_ps.length);
                            irgb[0] += (int)(mult*u[0]);
                            irgb[1] += (int)(mult*u[1]);
                            irgb[2] += (int)(mult*u[2]);
                            //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                            mx += (_ps.length-k);
                            if(rend.composition()==Rendering.Composition.back&&(u[0]>0||u[1]>0||u[2]>0)) break;
                        }
                        mx = 1;
                        _i.setRGB(i,j,Colors.pack(irgb[2]/mx, irgb[1]/mx, irgb[0]/mx));
                    }
                    else if(comp==Rendering.Composition.multiply||comp==Rendering.Composition.avg||comp==Rendering.Composition.difference) {
                        for(int k=0;k<_ps.length;k++) {
                            final FloatPlane fp = p(k);
                            double idx = fp.getCell(i,j);
                            int packed = fp.computeRgbColor(idx);
                            //final int[] u = _ps[k].creator().getPalette().unpack(idx, _unpack);
                            final int[] u = Colors.unpack(packed, _unpack);
                            //double mult = ((1+k)/(double)_d);
                            switch(comp) {
                                case avg:
                                default:
                                    irgb[0] += u[0];
                                    irgb[1] += u[1];
                                    irgb[2] += u[2];
                                    break;
                                case multiply:
                                    if(k!=0) {
                                        irgb[0] *= u[0];
                                        irgb[1] *= u[1];
                                        irgb[2] *= u[2];
                                    }
                                    else {
                                        irgb[0] = u[0];
                                        irgb[1] = u[1];
                                        irgb[2] = u[2];
                                    }
                                    break;
                                case difference:
                                    if(k!=0) {
                                        irgb[0] += (int)Math.abs(rgb[0] - u[0]);
                                        irgb[1] += (int)Math.abs(rgb[1] - u[1]);
                                        irgb[2] += (int)Math.abs(rgb[2] - u[2]);
                                    }
                                    else {
                                        irgb[0] = u[0];
                                        irgb[1] = u[1];
                                        irgb[2] = u[2];
                                    }
                                    break;
                            }
                            //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                            mx += (_ps.length-k);
                            //if(rend.composition()==Rendering.Composition.back&&(u[0]>0||u[1]>0||u[2]>0)) break;
                        }
                        mx = _ps.length;
                        if(comp==Rendering.Composition.multiply) {
                            _i.setRGB(i,j,Colors.pack(irgb[2]/(255*(mx-1)), irgb[1]/(255*(mx-1)), irgb[0]/(255*(mx-1))));
                        }
                        else if(comp==Rendering.Composition.difference) {
                            _i.setRGB(i,j,Colors.pack(irgb[2]/mx, irgb[1]/mx, irgb[0]/mx));
                        }
                        else {
                            _i.setRGB(i,j,Colors.pack(irgb[2]/mx, irgb[1]/mx, irgb[0]/mx));
                        }
                    }
                    else if(comp==Rendering.Composition.channel) {
                        if(_ps.length==4) {
                            _i.setRGB(i,j,Colors.pack(
                                        extractColor(3,i,j),
                                        extractColor(2,i,j),
                                        extractColor(1,i,j),
                                        extractColor(0,i,j)));
                        }
                        else if(_ps.length==3) {
                            _i.setRGB(i,j,Colors.pack(
                                        extractColor(2,i,j),
                                        extractColor(1,i,j),
                                        extractColor(0,i,j)));
                        }
                        else if(_ps.length==2) {
                            _i.setRGB(i,j,Colors.pack(
                                        extractColor(1,i,j),
                                        extractColor(0,i,j),
                                        0));
                        }
                        else if(_ps.length==1) {
                            _i.setRGB(i,j,Colors.pack(
                                        extractColor(0,i,j),
                                        0,
                                        0));
                        }
                    }
                //}
            }
        }
        return _i;
    }

    private int extractColor(int p, int i, int j) {
        final double c = p(p).getCell(i,j);
        final int mc = p(p).creator().archetype().colors();
        final double r = (double)c/(double)mc;
        return (int) (r*255f);
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
        return pr().toJfxImage();
    }

    @Override public javafx.scene.image.Image toJfxImage(WritableImage jfxImage) {
        return pr().toJfxImage(jfxImage);
    }

    @Override public CA creator() {
        return pr().creator();
    }

    @Override public Plane copy() {
        Plane[] psn = new Plane[_ps.length];
        for(int i=0;i<psn.length;i++) {
            psn[i] = _ps[i].copy();
        }
        return new CompositeFloatPlane(psn);
    }

    @Override public Plane withDepth(int d) {
        throw new UnsupportedOperationException();
    }

    @Override public Plane scale(float scale) {
        return new BufferedImagePlane((BufferedImage)toImage()).scale(scale);
    }

    @Override public Plane scale(float scale, boolean antialias) {
        return new BufferedImagePlane((BufferedImage)toImage()).scale(scale, antialias);
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

    @Override public int computeRgbColor(double idx) {
        throw new UnsupportedOperationException();
    }

    //public Palette getPalette() {
        //return pr().getPalette();
    //}

    @Override public void init() {
    }

    private final FloatPlane pr() {
        return (FloatPlane) _ps[_readDepthIdx];
    }

    private final FloatPlane pw() {
        return (FloatPlane) _ps[_writeDepthIdx];
    }

    private final FloatPlane p(int idx) {
        return (FloatPlane) _ps[idx];
    }
}
