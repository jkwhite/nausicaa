package org.excelsi.nausicaa.ca;


import java.awt.image.*;
import java.awt.Image;
import java.io.*;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
    

public class CompositeIntPlane extends AbstractIntPlane implements Sliceable {
    private final IntPlane[] _ps;
    private BufferedImage _i;
    private WritableRaster _r;
    private int _readDepthIdx;
    private int _writeDepthIdx;


    public CompositeIntPlane(IntPlane[] ps) {
        _ps = ps;
        //_i = new BufferedImage(ps[0].getWidth(), ps[0].getHeight(), BufferedImage.TYPE_INT_ARGB);
        //_r = _i.getRaster();
    }

    public IntPlane[] planes() {
        return _ps;
    }

    @Override public void setReadDepth(int idx) {
        _readDepthIdx = idx;
    }

    @Override public void setWriteDepth(int idx) {
        _writeDepthIdx = idx;
    }

    @Override public void setCell(int x, int y, int v) {
        pw().setCell(x, y, v);
    }

    @Override public void setCell(int x, int y, int z, int v) {
        pw().setCell(x, y, z, v);
    }

    @Override public void setRGBCell(int x, int y, int rgb) {
        pw().setRGBCell(x, y, rgb);
    }

    @Override public int getCell(int x, int y) {
        return pr().getCell(x, y);
    }

    @Override public int getCell(int x, int y, int z) {
        return pr().getCell(x, y, z);
    }

    @Override public int[] getRow(int[] into, int y, int offset) {
        return pr().getRow(into, y, offset);
    }

    @Override public int[] getBlock(int[] into, int x, int y, int dx, int dy, int offset) {
        return pr().getBlock(into, x, y, dx, dy, offset);
    }

    @Override public int[] getCardinal(int[] into, int x, int y, int dx, int dy, int offset) {
        return pr().getCardinal(into, x, y, dx, dy, offset);
    }

    @Override public int[] getCardinal(int[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        return pr().getCardinal(into, x, y, z, dx, dy, dz, offset);
    }

    @Override public void setRow(int[] row, int y) {
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

    private final int[] _rgb = new int[3];
    private final int[] _unpack = new int[4];
    @Override public java.awt.Image toImage(Rendering rend) {
        if(_i==null) {
            _i = new BufferedImage(_ps[0].getWidth(), _ps[0].getHeight(), BufferedImage.TYPE_INT_ARGB);
            _r = _i.getRaster();
        }
        final Rendering.Composition comp = rend.composition();
        final int[] rgb = _rgb;
        for(int i=0;i<getWidth();i++) {
            for(int j=0;j<getHeight();j++) {
                rgb[0]=0;
                rgb[1]=0;
                rgb[2]=0;
                int mx = 0;
                /*
                if(_d==1) {
                    int idx = getCell(i,j,0);
                    _i.setRGB(i,j,_p.color(idx));
                }
                else { */
                    if(comp==Rendering.Composition.front||comp==Rendering.Composition.truefront||comp==Rendering.Composition.wavg) {
                        for(int k=0;k<_ps.length;k++) {
                            int idx = _ps[k].getCell(i,j);
                            final int[] u = _ps[k].creator().getPalette().unpack(idx, _unpack);
                            float mult = ((_ps.length-k)/(float)_ps.length);
                            rgb[0] += (int)(mult*u[0]);
                            rgb[1] += (int)(mult*u[1]);
                            rgb[2] += (int)(mult*u[2]);
                            //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                            mx += (_ps.length-k);
                            if(comp==Rendering.Composition.truefront||comp==Rendering.Composition.front&&(u[0]>0||u[1]>0||u[2]>0)) break;
                        }
                        mx = 1;
                        _i.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
                    }
                    else if(comp==Rendering.Composition.back||comp==Rendering.Composition.revwavg) {
                        for(int k=_ps.length-1;k>=0;k--) {
                            int idx = _ps[k].getCell(i,j);
                            final int[] u = _ps[k].creator().getPalette().unpack(idx, _unpack);
                            float mult = ((1+k)/(float)_ps.length);
                            rgb[0] += (int)(mult*u[0]);
                            rgb[1] += (int)(mult*u[1]);
                            rgb[2] += (int)(mult*u[2]);
                            //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                            mx += (_ps.length-k);
                            if(rend.composition()==Rendering.Composition.back&&(u[0]>0||u[1]>0||u[2]>0)) break;
                        }
                        mx = 1;
                        _i.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
                    }
                    else if(comp==Rendering.Composition.multiply||comp==Rendering.Composition.avg||comp==Rendering.Composition.difference) {
                        for(int k=0;k<_ps.length;k++) {
                            int idx = _ps[k].getCell(i,j);
                            final int[] u = _ps[k].creator().getPalette().unpack(idx, _unpack);
                            //float mult = ((1+k)/(float)_d);
                            switch(comp) {
                                case avg:
                                default:
                                    rgb[0] += u[0];
                                    rgb[1] += u[1];
                                    rgb[2] += u[2];
                                    break;
                                case multiply:
                                    if(k!=0) {
                                        rgb[0] *= u[0];
                                        rgb[1] *= u[1];
                                        rgb[2] *= u[2];
                                    }
                                    else {
                                        rgb[0] = u[0];
                                        rgb[1] = u[1];
                                        rgb[2] = u[2];
                                    }
                                    break;
                                case difference:
                                    if(k!=0) {
                                        rgb[0] += (int)Math.abs(rgb[0] - u[0]);
                                        rgb[1] += (int)Math.abs(rgb[1] - u[1]);
                                        rgb[2] += (int)Math.abs(rgb[2] - u[2]);
                                    }
                                    else {
                                        rgb[0] = u[0];
                                        rgb[1] = u[1];
                                        rgb[2] = u[2];
                                    }
                                    break;
                            }
                            //if(u[0]>0) System.err.println("k: "+k+", u: "+u[0]+","+u[1]+","+u[2]+", mult: "+mult+", rgb: "+rgb[0]+","+rgb[1]+","+rgb[2]);
                            mx += (_ps.length-k);
                            //if(rend.composition()==Rendering.Composition.back&&(u[0]>0||u[1]>0||u[2]>0)) break;
                        }
                        mx = _ps.length;
                        if(comp==Rendering.Composition.multiply) {
                            _i.setRGB(i,j,Colors.pack(rgb[2]/(255*(mx-1)), rgb[1]/(255*(mx-1)), rgb[0]/(255*(mx-1))));
                        }
                        else if(comp==Rendering.Composition.difference) {
                            _i.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
                        }
                        else {
                            _i.setRGB(i,j,Colors.pack(rgb[2]/mx, rgb[1]/mx, rgb[0]/mx));
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
        final int c = _ps[p].getCell(i,j);
        final int mc = _ps[p].creator().archetype().colors();
        final float r = (float)c/(float)mc;
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
        IntPlane[] psn = new IntPlane[_ps.length];
        for(int i=0;i<psn.length;i++) {
            psn[i] = (IntPlane) _ps[i].copy();
        }
        return new CompositeIntPlane(psn);
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

    //public Palette getPalette() {
        //return pr().getPalette();
    //}

    @Override public void init() {
    }

    private final IntPlane pr() {
        return _ps[_readDepthIdx];
    }

    private final IntPlane pw() {
        return _ps[_writeDepthIdx];
    }
}
