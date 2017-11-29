package org.excelsi.nausicaa.ca;


import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.*;
import javax.imageio.*;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.util.Iterator;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javafx.embed.swing.SwingFXUtils;
import org.imgscalr.Scalr;


public class BufferedImagePlane extends AbstractPlane implements java.io.Serializable {
    private final CA _creator;
    private final Palette _palette;
    private transient BufferedImage _i;
    private transient WritableRaster _r;
    private transient JFrame _d;
    private float _scale = 1f;
    private int _lockOwner = -1;


    public BufferedImagePlane(CA creator, int width, int time, Palette p) {
        _creator = creator;
        _i = new BufferedImage(width, time, BufferedImage.TYPE_BYTE_INDEXED, p.toColorModel());
        _r = _i.getRaster();
        _palette = p;
    }

    public BufferedImagePlane(CA creator, int width, int time) {
        _creator = creator;
        _i = new BufferedImage(width, time, BufferedImage.TYPE_BYTE_INDEXED,
            new IndexColorModel(8, 2,
                new byte[]{0, 127},
                new byte[]{0, 127},
                new byte[]{0, 127}
            ));
        _r = _i.getRaster();
        _palette = null;
    }

    public BufferedImagePlane(BufferedImage i) {
        _creator = null;
        _i = i;
        _palette = null;
    }

    @Override public CA creator() {
        return _creator;
    }

    @Override public Plane copy() {
        BufferedImagePlane p;
        if(_palette!=null) {
            p = new BufferedImagePlane(_creator, getWidth(), getHeight(), _palette);
        }
        else {
            p = new BufferedImagePlane(_creator, getWidth(), getHeight());
        }
        p._i.setData(_i.getData());
        return p;
    }

    @Override public void init() {
    }

    @Override public Image toImage() {
        return _i;
    }

    @Override public Image toImage(int width, int height) {
        return _i.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override public Plane scale(float scale) {
        return scale(scale, true);
    }

    @Override public Plane scale(float scale, boolean antialias) {
        if(antialias) {
            return new BufferedImagePlane(Scalr.resize(_i, Scalr.Method.ULTRA_QUALITY, (int)(getWidth()*scale), (int)(getWidth()*scale), Scalr.OP_ANTIALIAS /*, Scalr.OP_BRIGHTER*/));
        }
        else {
            return new BufferedImagePlane(Scalr.resize(_i, Scalr.Method.SPEED, (int)(getWidth()*scale), (int)(getWidth()*scale)));
        }
    }

    @Override public BufferedImage toBufferedImage() {
        return _i;
    }

    @Override public javafx.scene.image.Image toJfxImage() {
        return SwingFXUtils.toFXImage(_i, null);
    }

    @Override public javafx.scene.image.Image toJfxImage(javafx.scene.image.WritableImage jfxImage) {
        return SwingFXUtils.toFXImage(_i, jfxImage);
    }

    public float getScale() {
        return _scale;
    }

    public BufferedImagePlane withScale(float scale) {
        _scale = scale;
        return this;
    }

    @Override public int getWidth() {
        return _i.getWidth();
    }

    @Override public int getHeight() {
        return _i.getHeight();
    }

    @Override public int getDepth() {
        return 1;
    }

    public Raster getRaster() {
        //return _i.getRaster();
        return _r;
    }

    @Override public void setCell(int x, int y, int z, int v) {
        setCell(x, y, v);
    }

    @Override public void setCell(int x, int y, int v) {
        //_i.getRaster().setSample(x, y, 0, v);
        //if(v>=_palette.getColorCount()) {
            //throw new IllegalArgumentException("color out of range: "+v);
        //}
        _r.setSample(x, y, 0, v);
    }

    @Override public void setRGBCell(int x, int y, int rgb) {
        _i.setRGB(x, y, rgb);
    }

    @Override public void setRow(int[] row, int y) {
        _r.setSamples(0, y, getWidth(), 1, 0, row);
    }

    @Override public int[] getRow(int[] into, int y, int offset) {
        for(int i=0;i<getWidth();i++) {
            into[i+offset] = getCell(i, y);
        }
        return into;
    }

    @Override public int[] getCardinal(int[] into, int x, int y, int z, int offset) {
        into[0] = getCell(x-1,y,0);
        into[1] = getCell(x+1,y,0);
        into[2] = getCell(x,y-1,0);
        into[3] = getCell(x,y+1,0);
        return into;
    }

    @Override public int[] getBlock(int[] into, int x, int y, int w, int h, int offset) {
        //Raster r = _i.getData();
        //System.err.println(String.format("getBlock %d,%d,%d,%d", x, y, w, h));
        Raster r = _r;
        if(x>=0&&y>=0&&x+w<_i.getWidth()&&y+h<_i.getHeight()) {
            //return r.getPixels(x, y, w, h, into);
            //System.err.println("a");
            return r.getSamples(x, y, w, h, 0, into);
        }
        else {
            int n = 0;
            for(int j=y;j<y+h;j++) {
                for(int i=x;i<x+w;i++) {
                    into[n++] = getCell(i,j);
                }
            }
            return into;
        }
    }

    @Override public int getCell(int x, int y, int z) {
        return getCell(x, y);
    }

    @Override public int getCell(int x, int y) {
        //System.err.println(String.format("init %d,%d", x, y));
        if(x<0) {
            x += getWidth();
        }
        else if(x>=getWidth()) {
            x -= getWidth();
        }
        if(y<0) {
            y += getHeight();
        }
        else if(y>=getHeight()) {
            y -= getHeight();
        }
        //return _i.getData().getSample(x, y, 0);
        //System.err.println(String.format("cell %d,%d", x, y));
        return _r.getSample(x, y, 0);
    }

    @Override public void save(String filename) throws IOException {
        Pipeline.write(_i, filename);
    }

    @Override public Plane subplane(final int x1, final int y1, final int x2, final int y2) {
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
    //@Override public synchronized boolean lock(int id) {
        //if(_lockOwner==id) {
            //return true;
        //}
        //while(_lockOwner!=-1);
        //_lockOwner = id;
        //return true;
    //}
//
    //@Override public void unlock() {
        //_lockOwner = -1;
    //}
}
