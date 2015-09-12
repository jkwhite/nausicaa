package org.excelsi.ca;


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


public class CA implements java.io.Serializable {
    public static final int COLOR_MASK = 0x00ffffff;
    public static final int ALPHA_MASK = 0xff000000;
    private transient BufferedImage _i;
    private transient JFrame _d;
    private transient Rule _rule;
    private float _goodness;
    private float _scale = 1f;


    public CA(int width, int time) {
        _i = new BufferedImage(width, time, BufferedImage.TYPE_INT_ARGB);
        //_i = new BufferedImage(width, time, BufferedImage.TYPE_USHORT_555_RGB);
    }

    public CA(BufferedImage i) {
        _i = i;
    }

    public int getWidth() {
        return _i.getWidth();
    }

    public int getHeight() {
        return _i.getHeight();
    }

    public Rule getRule() {
        return _rule;
    }

    public void setRule(Rule r) {
        _rule = r;
    }

    public BufferedImage getImageBuffer() {
        return _i;
    }

    public void setGoodness(float goodness) {
        _goodness = goodness;
    }

    public float getGoodness() {
        return _goodness;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeFloat(_goodness);
        out.writeInt(getWidth());
        out.writeInt(getHeight());
        int[] row = new int[getWidth()];
        for(int y=0;y<getHeight();y++) {
            _i.getRGB(0, y, row.length, 1, row, 0, 0);
            out.writeObject(row);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        _goodness = in.readFloat();
        int w = in.readInt();
        int h = in.readInt();
        _i = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<h;y++) {
            int[] row = (int[]) in.readObject();
            _i.setRGB(0, y, w, 1, row, 0, 0);
        }
    }
    

    /*
    public float goodness() {
        HashMap<Integer,Integer> h = new HashMap<Integer,Integer>();
        for(int i=0;i<getWidth();i++) {
            for(int j=0;j<getHeight();j++) {
                int p = get(i,j);
                Integer c = h.get(p);
                if(c==null) {
                    c = new Integer(1);
                    h.put(p, c);
                }
                else {
                    h.put(p, new Integer(c.intValue()+1));
                }
            }
        }
    }
    */

    public void initPlain(int v) {
        for(int i=0;i<getWidth();i++) {
            set(i, 0, v);
        }
    }

    public void initRandom(int[] values) {
        Random r = new Random();
        for(int i=0;i<getWidth();i++) {
            set(i, 0, values[r.nextInt(values.length)]);
        }
    }

    public void initStaggered(int[] values, int stagger) {
        for(int i=0;i<getWidth();i++) {
            int idx = 0;
            if(i%stagger==0) {
                idx = 1;
            }
            set(i, 0, values[idx]);
        }
    }

    public void setData(Raster r) {
        _i.setData(r);
    }

    public Raster getData() {
        return _i.getData();
    }

    public int boundx(int x) {
        int w = getWidth();
        while(x<0) x += w;
        while(x>=w) x -= w;
        return x;
    }

    public int boundy(int y) {
        int h = getHeight();
        while(y<0) y += h;
        while(y>=h) y -= h;
        return y;
    }

    public void set(int x, int y, int v) {
        x = boundx(x);
        y = boundy(y);
        _i.setRGB(x, y, v);
    }

    public int get(int x, int y) {
        x = boundx(x);
        y = boundy(y);
        return _i.getRGB(x, y);
    }

    public int[] getRow(int[] into, int y) {
        return _i.getRGB(0, y, into.length, 1, into, 0, 0);
    }

    public int[] getRow(int[] into, int y, int offset) {
        return _i.getRGB(0, y, into.length-2*offset, 1, into, offset, 0);
    }

    public int[] getBlock(int[] into, int x, int y, int d) {
        return getBlock(into, x, y, d, 0);
    }

    public int[] getBlock(int[] into, int x, int y, int d, int offset) {
        return _i.getRGB(x, y, d, d, into, offset, 0);
    }

    public int[] getBlock(int[] into, int x, int y, int dx, int dy, int offset) {
        return _i.getRGB(x, y, dx, dy, into, offset, 0);
    }

    public void setRow(int[] row, int y) {
        _i.setRGB(0, y, row.length, 1, row, 0, 0);
    }

    public Image toImage() {
        return _i;
    }

    public CA scale(float scale) {
        _scale = scale;
        return this;
    }

    public CA toScaledCA(float scale) {
        int w = _i.getWidth();
        int h = _i.getHeight();
        /*
        BufferedImage scaled = new BufferedImage((int)(w*scale), (int)(h*scale), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        AffineTransformOp scaleOp = new AffineTransformOp(at, hints);
        scaled = scaleOp.filter(_i, scaled);
        return new CA(scaled);
        */
        return new CA(Scalr.resize(_i, Scalr.Method.ULTRA_QUALITY, (int)(w*scale), (int)(h*scale), Scalr.OP_ANTIALIAS, Scalr.OP_BRIGHTER));
    }

    public Image toScaledImage() {
        return _i.getScaledInstance((int) (getWidth()*_scale), (int) (getHeight()*_scale), Image.SCALE_SMOOTH);
    }

    public Image toImage(int width, int height) {
        return _i.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        //return _i.getScaledInstance(width, height, Image.SCALE_FAST);
    }

    public void show() {
        if(_d!=null) {
            hide();
        }
        Display dis = new Display(this);
        //_d = dis;
        _d = new JFrame();
        _d.setSize(getWidth(), getHeight());
        _d.getContentPane().add(dis);
        _d.setVisible(true);
    }

    public void hide() {
        if(_d!=null) {
            _d.setVisible(false);
            _d = null;
        }
    }

    public void save(String file) throws IOException {
        save(new java.io.File(file));
    }

    public void save(File file) throws IOException {
        if(file.getName().endsWith(".gif")) {
            ImageIO.write(_i, "png", file);
        }
        else if(file.getName().endsWith(".jpg")) {
            ImageIO.write(_i, "jpg", file);
        }
        else {
            if(!file.getName().endsWith(".png")) {
                file = new File(file.toString()+".png");
            }
            ImageIO.write(_i, "png", file);
        }
    }

    public JLabel label(String desc) {
        if(desc==null) {
            desc = ""+getGoodness();
        }
        JLabel l = new JLabel(desc, new ImageIcon(toImage()), SwingConstants.CENTER);
        l.setVerticalTextPosition(SwingConstants.BOTTOM);
        l.setHorizontalTextPosition(SwingConstants.CENTER);
        return l;
    }

    public static CA load(String file) throws IOException {
        BufferedImage i = ImageIO.read(new java.io.File(file));
        CA c = new CA(i);
        return c;
    }

    public static int randomColor() {
        return randomColor(Rand.om);
    }

    public static int randomColor(Random om) {
        return pack(om.nextInt(256), om.nextInt(256),
                om.nextInt(256),
                //255);
                //253 + Rand.om.nextInt(3));
                //200 + Rand.om.nextInt(53));
                randAlpha(om));
    }

    public static int randAlpha() {
        return randAlpha(Rand.om);
    }

    public static int randAlpha(Random om) {
        switch(om.nextInt(5)) {
            case 0:
                return 253;
            case 1:
                return 254;
            default:
                return 255;
        }
    }

    public static int pack(int r, int g, int b, int a) {
        //return a+(r<<8)+(g<<16)+(b<<24);
        return b|(g<<8)|(r<<16)|(a<<24);
    }

    public static int[] unpack(int c) {
        return unpack(c, new int[4]);
    }

    public static int[] unpack(int c, int[] u) {
        u[0] = c & 0xff;
        u[1] = (c>>8)&0xff;
        u[2] = (c>>16)&0xff;
        u[3] = (c>>24)&0xff;
        return u;
    }

    public static int alpha(int c) {
        return c>>24&0xff;
    }

    public static int setAlpha(int c, int a) {
        return setAlpha(c, a, 0);
    }

    public static int setAlpha(int c, int a, int offset) {
        a += offset;
        c = (c&COLOR_MASK) | ((a&0xff)<<24);
        return c;
    }

    public static String toColorString(int c) {
        int[] u = unpack(c);
        return "[R: "+u[0]+" G: "+u[1]+" B: "+u[2]+" A: "+u[3]+"]";
    }

    /*
    public static int combineAlpha(int a, int b) {
        int aa = CA.alpha(a);
        int ab = CA.alpha(b);
        //System.err.println("srcalphas: "+aa+", "+ab);
        int an = aa+ab; //-255;
        //an -= 255;
        //if(a+b!=-19192912) an-=255;
        if(an>255) an-=254;
        if(an>255) an=255;
        //if(an<0) an=0;
        if(an<128) an=128;
        //if(a+b!=-1191191) an=255;
        //System.out.print(an);
        //System.out.print(" ");
        a = (b&CA.COLOR_MASK)|(an<<24);
        //System.err.println("aa="+aa+", ab="+ab+", an="+an+", an<<24="+(an<<24)+", a="+a);
        return a;
    }
    */
}
