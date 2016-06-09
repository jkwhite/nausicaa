package org.excelsi.nausicaa.ca;


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Random;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import org.imgscalr.Scalr;


public class ImageInitializer implements Initializer {
    private final BufferedImage _image;
    private BufferedImage _lastImage;


    public ImageInitializer() {
        _image = null;
    }

    public ImageInitializer(File url) throws IOException {
        this(ImageIO.read(url));
    }

    public ImageInitializer(BufferedImage image) {
        _image = image;
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
                _lastImage = Scalr.resize(_image, Scalr.Method.ULTRA_QUALITY, plane.getWidth(), plane.getHeight(), Scalr.OP_ANTIALIAS /*, Scalr.OP_BRIGHTER*/);
            }
            final int w = _lastImage.getWidth();
            final int h = _lastImage.getHeight();
            for(int j=0;j<plane.getHeight();j++) {
                for(int i=0;i<plane.getWidth();i++) {
                    int v = _lastImage.getRGB(i % w, j % h);
                    plane.setRGBCell(i, j, v);
                }
            }
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.image.getId());
    }
}
