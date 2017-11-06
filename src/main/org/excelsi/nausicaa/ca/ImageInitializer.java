package org.excelsi.nausicaa.ca;


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Random;
import java.util.Map;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import org.imgscalr.Scalr;


public class ImageInitializer implements Initializer {
    private final BufferedImage _image;
    private final Params _params;
    private BufferedImage _lastImage;
    private File _url;


    public ImageInitializer() {
        _image = null;
        _params = new Params();
    }

    public ImageInitializer(File url) throws IOException {
        this(url, new Params());
    }

    public ImageInitializer(File url, Params params) throws IOException {
        this(ImageIO.read(url), params);
        _url = url;
    }

    public ImageInitializer(BufferedImage image) {
        this(image, new Params());
    }

    public ImageInitializer(BufferedImage image, Params params) {
        _image = image;
        _params = params;
    }

    @Override public void init(Plane plane, Rule rule, Random random) {
        Palette p = plane.creator().getPalette();
        if(rule.archetype().dims()==1) {
            final int w = _image.getWidth();
            final int h = _image.getHeight();
            for(int i=0;i<plane.getWidth();i++) {
                int v = _image.getRGB(i % w, 0);
                plane.setRGBCell(i, 0, v);
            }
        }
        else {
            //final BufferedImage image = _image.getScaledInstance(plane.getWidth(), plane.getHeight(), BufferedImage.SCALE_SMOOTH);
            if(_lastImage==null || _lastImage.getWidth()!=plane.getWidth() || _lastImage.getHeight()!=plane.getHeight()) {
                //_lastImage = Scalr.resize(_image, Scalr.Method.ULTRA_QUALITY, plane.getWidth(), plane.getHeight(), Scalr.OP_ANTIALIAS /*, Scalr.OP_BRIGHTER*/);
                if(_params.scale) {
                    _lastImage = Scalr.resize(_image, Scalr.Method.SPEED, plane.getWidth(), plane.getHeight() /*, Scalr.OP_ANTIALIAS , Scalr.OP_BRIGHTER*/);
                }
                else {
                    _lastImage = _image;
                }
            }
            final int w = _lastImage.getWidth();
            final int h = _lastImage.getHeight();
            if(false && plane instanceof BufferedImagePlane) {
                for(int j=0;j<plane.getHeight();j++) {
                    for(int i=0;i<plane.getWidth();i++) {
                        int v = _lastImage.getRGB(i % w, j % h);
                        plane.setRGBCell(i, j, v);
                    }
                }
            }
            else {
                Map<Integer,Integer> colormap = p.buildColormap();
                if(_params.tile) {
                    for(int j=0;j<plane.getHeight();j++) {
                        for(int i=0;i<plane.getWidth();i++) {
                            int v = _lastImage.getRGB(i % w, j % h);
                            Integer iv = colormap.get(v);
                            if(iv==null) {
                                throw new IllegalStateException("colormap missing color "+v);
                            }
                            plane.setCell(i, j, iv);
                        }
                    }
                }
                else {
                    int xoff = _params.center ? plane.getWidth()/2 - _lastImage.getWidth()/2 : 0;
                    int yoff = _params.center ? plane.getHeight()/2 - _lastImage.getHeight()/2 : 0;
                    for(int j=0;j<_lastImage.getHeight();j++) {
                        for(int i=0;i<_lastImage.getWidth();i++) {
                            int v = _lastImage.getRGB(i, j);
                            Integer iv = colormap.get(v);
                            if(iv==null) {
                                throw new IllegalStateException("colormap missing color "+v);
                            }
                            if(i+xoff>=0 && j+yoff>=0 && i+xoff<plane.getWidth() && j+yoff<plane.getHeight()) {
                                plane.setCell(i+xoff, j+yoff, iv);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.image.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.image.name());
        w.println(_url!=null?_url.toString():"-");
        w.println(""+_params.center);
        w.println(""+_params.tile);
        w.println(""+_params.scale);
    }

    public static ImageInitializer read(BufferedReader r, int version) throws IOException {
        return new ImageInitializer(
            new File(r.readLine()),
            new Params(
                Boolean.valueOf(r.readLine()),
                Boolean.valueOf(r.readLine()),
                Boolean.valueOf(r.readLine())
            )
        );
    }

    public static class Params {
        public final boolean center;
        public final boolean tile;
        public final boolean scale;

        public Params() {
            this(false, false, true);
        }

        public Params(boolean center, boolean tile, boolean scale) {
            this.center = center;
            this.tile = tile;
            this.scale = scale;
        }
    }
}
