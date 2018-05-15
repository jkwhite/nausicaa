package org.excelsi.nausicaa.ca;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.io.File;
import java.util.Arrays;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelFormat;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;


public class WritableImagePlane extends AbstractPlane {
    private final CA _creator;
    private final WritableImage _i;
    private final PixelReader _r;
    private final PixelWriter _w;
    private final Palette _p;
    private final PixelFormat<ByteBuffer> _pf;
    private final WritablePixelFormat<IntBuffer> _pfi = WritablePixelFormat.getIntArgbInstance();
    private final int[] _colors;
    private final int _width;
    private final int _height;
    private int _lockOwner = -1;


    public WritableImagePlane(CA creator, int w, int h, Palette p) {
        _creator = creator;
        _i = new WritableImage(w, h);
        _r = _i.getPixelReader();
        _w = _i.getPixelWriter();
        _width = w;
        _height = h;
        _p = p;
        _colors = p.getColors();
        _pf = PixelFormat.createByteIndexedInstance(p.getColors());
    }

    public CA creator() {
        return _creator;
    }

    public Plane copy() {
        return new WritableImagePlane(_creator, _width, _height, _p);
    }

    @Override public Plane withDepth(int d) {
        IntBlockPlane p = new IntBlockPlane(_creator, _width, _height, d, _p);
        for(int i=0;i<_width;i++) {
            for(int j=0;j<_height;j++) {
                p.setCell(i, j, 0, getCell(i, j));
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

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    @Override public int getDepth() {
        return 1;
    }

    public void init() {
    }

    public Image toJfxImage() {
        return _i;
    }

    @Override public javafx.scene.image.Image toJfxImage(javafx.scene.image.WritableImage img) {
        return _i;
    }

    public java.awt.Image toImage() {
        throw new UnsupportedOperationException();
    }

    public java.awt.Image toImage(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override public BufferedImage toBufferedImage() {
        throw new UnsupportedOperationException();
    }

    public java.awt.Image toImage(Rendering rend) {
        throw new UnsupportedOperationException();
    }

    public java.awt.Image toImage(Rendering rend, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override public BufferedImage toBufferedImage(Rendering rend) {
        throw new UnsupportedOperationException();
    }

    @Override public void setCell(int x, int y, int z, int v) {
        setCell(x, y, v);
    }

    private byte[] _cellBufW = new byte[1];
    public void setCell(int x, int y, int v) {
        //System.err.println(System.identityHashCode(this)+"setting "+x+", "+y+" to "+v);
        //_w.setArgb(x, y, v);
        _cellBufW[0] = (byte) v;
        _w.setPixels(x, y, 1, 1, _pf, _cellBufW, 0, 0);
        int c = _r.getArgb(x, y);
        //System.err.println(System.identityHashCode(this)+"set to "+c);
    }

    @Override public int getCell(int x, int y, int z) {
        return getCell(x, y);
    }

    private byte[] _cellBufR = new byte[1];
    public int getCell(int x, int y) {
        //System.err.println(System.identityHashCode(this)+"getCell "+x+","+y);
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
        int c = _r.getArgb(x, y);
        return reverse(c);
        //_r.getPixels(x, y, 1, 1, _pf, _cellBufR, 0, 0);
        //return _cellBufR[0];
    }

    private final int reverse(int argb) {
        for(int i=0;i<_colors.length;i++) {
            //System.err.println(System.identityHashCode(this)+"compare "+argb+" vs "+_colors[i]);
            if(_colors[i]==argb) {
                return i;
            }
        }
        //throw new IllegalStateException("no such value "+argb+" in "+Arrays.toString(_colors));
        //return -1;
        return argb;
    }

    private final int[] reverse(int[] argb) {
        //System.err.println(System.identityHashCode(this)+"array: "+Arrays.toString(argb));
        for(int i=0;i<argb.length;i++) {
            argb[i] = reverse(argb[i]);
        }
        return argb;
    }

    @Override public void setRGBCell(int x, int y, int rgb) {
        throw new UnsupportedOperationException();
        //_w.setArgb(x, y, rgb);
    }

    //public int[] getRow(int[] into, int y) {
        //return _i.getRGB(0, y, into.length, 1, into, 0, 0);
    //}

    public int[] getRow(int[] into, int y, int offset) {
        //return _i.getRGB(0, y, into.length-2*offset, 1, into, offset, 0);
        //System.err.println(System.identityHashCode(this)+"getRow "+y);
        _r.getPixels(0, y, into.length-2*offset, 1,
            _pfi, into, offset, getWidth());
        return reverse(into);
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
        into[offset++] = getCell(x+1,y,0);
        into[offset++] = getCell(x,y-1,0);
        into[offset++] = getCell(x,y,0);
        into[offset++] = getCell(x,y+1,0);
        into[offset++] = getCell(x-1,y,0);
        return into;
    }

    public int[] getBlock(int[] into, int x, int y, int w, int h, int offset) {
        //System.err.println(System.identityHashCode(this)+"getBlock "+x+","+y);
        //return getBlock(into, x, y, d, 0);
        //throw new UnsupportedOperationException();
        if(false && x>=0&&y>=0&&x+w<_i.getWidth()&&y+h<_i.getHeight()) {
            //_r.getPixels(x, y, w, h, WritablePixelFormat.getIntArgbInstance(), into, offset, 0);
            //_r.getPixels(x, y, w, h, _pf, into, offset, 0);
            _r.getPixels(x, y, w, h, _pfi, into, offset, 0);
            reverse(into);
        }
        else {
            int n = 0;
            for(int j=y;j<y+h;j++) {
                for(int i=x;i<x+w;i++) {
                    into[n++] = getCell(i,j);
                }
            }
        }
        return into;
    }
//
    //public int[] getBlock(int[] into, int x, int y, int d, int offset) {
        //return _i.getRGB(x, y, d, d, into, offset, 0);
    //}
//
    //public int[] getBlock(int[] into, int x, int y, int dx, int dy, int offset) {
        //return _i.getRGB(x, y, dx, dy, into, offset, 0);
    //}

    private byte[] _lastRow;
    public synchronized void setRow(final int[] row, final int y) {
        if(_lastRow==null||_lastRow.length!=row.length) {
            _lastRow = new byte[row.length];
        }
        for(int i=0;i<row.length;i++) {
            _lastRow[i] = (byte) row[i];
        }
        //_i.setRGB(0, y, row.length, 1, row, 0, 0);
        //_w.setPixels(0, y, row.length, 1, PixelFormat.getIntArgbInstance(), row, 0, getWidth());
        _w.setPixels(0, y, row.length, 1, _pf, _lastRow, 0, getWidth());
    }

    @Override public void save(String filename) throws IOException {
        ImageIO.write(SwingFXUtils.fromFXImage(_i, null), "png", new File(filename));
    }

    @Override public Plane subplane(int x1, int y1, int x2, int y2) {
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
