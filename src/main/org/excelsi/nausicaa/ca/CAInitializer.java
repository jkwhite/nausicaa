package org.excelsi.nausicaa.ca;


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import org.imgscalr.Scalr;


public class CAInitializer implements Initializer {
    private String _url;
    private int _iterations = 10;
    private Plane _cached;


    public CAInitializer() {
    }

    public CAInitializer(String url, int iterations) {
        _url = url;
        _iterations = iterations;
    }

    @Override public void init(Plane plane, Rule rule, Random random) {
        Plane res = null;
        /*
        if(_cached!=null) {
            switch(rule.archetype().dims()) {
                case 1:
                case 2:
                    if(_cached.getWidth()==plane.getWidth()
                        && _cached.getHeight()==plane.getHeight()) {
                        res = _cached;
                    }
                    break;
                case 3:
                    if(plane instanceof IntBlockPlane && _cached instanceof IntBlockPlane) {
                        IntBlockPlane bp = (IntBlockPlane) plane;
                        IntBlockPlane cbp = (IntBlockPlane) _cached;
                        if(cbp.getWidth()==bp.getWidth()
                            && cbp.getHeight()==bp.getHeight()
                            && cbp.getDepth()==bp.getDepth()) {
                            res = _cached;
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("unsupported dimensionality "+rule.archetype().dims());
            }
        }
        */
        if(res==null) {
            CA ca = null;
            try {
                ca = CA.fromFile(_url, "text")
                    .prelude(_iterations);
                switch(rule.archetype().dims()) {
                    case 1:
                    case 2:
                        ca = ca.size(plane.getWidth(), plane.getHeight());
                        break;
                    case 3:
                        IntBlockPlane bp = (IntBlockPlane) plane;
                        ca = ca.size(plane.getWidth(), plane.getHeight(), bp.getDepth());
                        break;
                    default:
                        throw new IllegalArgumentException("unsupported dimensionality "+rule.archetype().dims());
                }
                res = ca.createPlane();
            }
            catch(IOException e) {
                throw new IllegalStateException("no such file '"+_url+"'");
            }
        }
        final Plane p = res;
        switch(rule.archetype().dims()) {
            case 1:
            case 2:
                for(int i=0;i<plane.getWidth();i++) {
                    for(int j=0;j<plane.getHeight();j++) {
                        plane.setCell(i,j,p.getCell(i,j));
                    }
                }
                break;
            case 3:
                IntBlockPlane s = (IntBlockPlane) p;
                IntBlockPlane bp = (IntBlockPlane) plane;
                for(int i=0;i<bp.getWidth();i++) {
                    for(int j=0;j<bp.getHeight();j++) {
                        for(int k=0;k<bp.getDepth();k++) {
                            bp.setCell(i,j,s.getCell(i,j,k));
                        }
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("unsupported dimensionality "+rule.archetype().dims());
        }
        _cached = p;
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.ca.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.ca.name());
        w.println(_url!=null?_url.toString():"-");
        w.println(_iterations);
    }

    public static CAInitializer read(BufferedReader r, int version) throws IOException {
        return new CAInitializer(
            r.readLine(),
            Integer.parseInt(r.readLine())
        );
    }

    /*
    private static class CKey {
        public Plane pl
        public int w;
        public int h;
        public int d;
        public int r;
    }
    */
}
