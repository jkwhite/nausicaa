package org.excelsi.nausicaa;


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


public class CA2 implements java.io.Serializable {
    private transient BufferedImage _i;
    private transient WritableRaster _r;
    private transient JFrame _d;
    private float _scale = 1f;


    public CA2(int width, int time) {
        //_i = new BufferedImage(width, time, BufferedImage.TYPE_BYTE_INDEXED);
        _i = new BufferedImage(width, time, BufferedImage.TYPE_BYTE_INDEXED,
            new IndexColorModel(8, 5,
                new byte[]{0, 127, 127, 0, 0},
                new byte[]{0, 127, 0, 127, 0},
                new byte[]{0, 127, 0, 0, 127}
            ));
        _r = _i.getRaster();
        //_i = new BufferedImage(width, time, BufferedImage.TYPE_USHORT_555_RGB);
    }

    public CA2(BufferedImage i) {
        _i = i;
    }

    public Image toImage() {
        return _i;
    }

    public Image toImage(int width, int height) {
        return _i.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    public float getScale() {
        return _scale;
    }

    public CA2 scale(float scale) {
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
        _r.setSample(x, y, 0, v);
    }

    public int[] getBlock(int[] into, int x, int y, int w, int h) {
        //Raster r = _i.getData();
        Raster r = _r;
        if(x>=0&&y>=0&&x+w<_i.getWidth()&&y+h<_i.getHeight()) {
            //return r.getPixels(x, y, w, h, into);
            return r.getSamples(x, y, w, h, 0, into);
        }
        else {
            int n = 0;
            for(int i=x;i<x+w;i++) {
                for(int j=y;j<y+h;j++) {
                    into[n++] = getCell(i,j);
                }
            }
            return into;
        }
    }

    public int getCell(int x, int y) {
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
        return _i.getData().getSample(x, y, 0);
    }

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
}
