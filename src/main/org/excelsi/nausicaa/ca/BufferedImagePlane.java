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
import org.imgscalr.Scalr;


public class BufferedImagePlane implements Plane, java.io.Serializable {
    private final CA _creator;
    private final Palette _palette;
    private transient BufferedImage _i;
    private transient WritableRaster _r;
    private transient JFrame _d;
    private float _scale = 1f;


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

    public CA creator() {
        return _creator;
    }

    public Plane copy() {
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

    public void init() {
    }

    public Image toImage() {
        return _i;
    }

    public Image toImage(int width, int height) {
        return _i.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override public Plane scale(float scale) {
        return new BufferedImagePlane(Scalr.resize(_i, Scalr.Method.ULTRA_QUALITY, (int)(getWidth()*scale), (int)(getWidth()*scale), Scalr.OP_ANTIALIAS /*, Scalr.OP_BRIGHTER*/));
    }

    @Override public BufferedImage toBufferedImage() {
        return _i;
    }

    public javafx.scene.image.Image toJfxImage() {
        throw new UnsupportedOperationException();
    }

    public float getScale() {
        return _scale;
    }

    public BufferedImagePlane withScale(float scale) {
        _scale = scale;
        return this;
    }

    public int getWidth() {
        return _i.getWidth();
    }

    public int getHeight() {
        return _i.getHeight();
    }

    public Raster getRaster() {
        //return _i.getRaster();
        return _r;
    }

    public void setCell(int x, int y, int v) {
        //_i.getRaster().setSample(x, y, 0, v);
        //if(v>=_palette.getColorCount()) {
            //throw new IllegalArgumentException("color out of range: "+v);
        //}
        _r.setSample(x, y, 0, v);
    }

    public void setRow(int[] row, int y) {
        _r.setSamples(0, y, getWidth(), 1, 0, row);
    }

    public int[] getRow(int[] into, int y, int offset) {
        for(int i=0;i<getWidth();i++) {
            into[i+offset] = getCell(i, y);
        }
        return into;
    }

    public int[] getBlock(int[] into, int x, int y, int w, int h, int offset) {
        //Raster r = _i.getData();
        //System.err.println(String.format("getBlock %d,%d,%d,%d", x, y, w, h));
        Raster r = _r;
        if(x>=0&&y>=0&&x+w<_i.getWidth()&&y+h<_i.getHeight()) {
            //return r.getPixels(x, y, w, h, into);
            //System.err.println("a");
            return r.getSamples(x, y, w, h, 0, into);
        }
        else {
            //System.err.println("m");
            int n = 0;
            /*
            for(int i=x;i<x+w;i++) {
                for(int j=y;j<y+h;j++) {
                    into[n++] = getCell(i,j);
                }
            }
            */
            for(int j=y;j<y+h;j++) {
                for(int i=x;i<x+w;i++) {
                    into[n++] = getCell(i,j);
                }
            }
            return into;
        }
    }

    public int getCell(int x, int y) {
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

    /*
    public void show() {
        if(_d!=null) {
            hide();
        }
        Display2 dis = new Display2(this);
        //_d = dis;
        _d = new JFrame();
        _d.setSize(35+(int)(_scale*getWidth()), 35+(int) (_scale*getHeight()));
        _d.getContentPane().add(dis);
        _d.setVisible(true);
    }

    public void hide() {
        if(_d!=null) {
            _d.setVisible(false);
            _d = null;
        }
    }
    */
}
