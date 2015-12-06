package org.excelsi.nausicaa.ca;


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.imgscalr.Scalr;


public class Pipeline {
    public static Supplier<Long> identifier() {
        return new Supplier() {
            private long id = 0;

            @Override public Long get() {
                long n = id++;
                return n;
            }
        };
    }

    public static <T> Function<T,PipelineContext> context(String name, String id, Supplier<Long> identifier) {
        return (t)->new PipelineContext().with(name, t).with(id, identifier.get());
    }

    public static Function<Plane,Plane> scalePlane(float scale) {
        return (p1)->p1.scale(scale);
    }

    public static Function<PipelineContext,PipelineContext> scale(String s, String d, float scale) {
        return (p)->{
            BufferedImage p1 = p.<BufferedImage>get(s);
            return p.with(d, scale==1.0f?p1:Scalr.resize(p1, Scalr.Method.ULTRA_QUALITY, (int)(p1.getWidth()*scale), (int)(p1.getWidth()*scale), Scalr.OP_ANTIALIAS /*, Scalr.OP_BRIGHTER*/));
        };
    }

    public static Function<PipelineContext,PipelineContext> indexed2Rgb(String s, String d) {
        return (p)->{
            BufferedImage p1 = p.<BufferedImage>get(s);
            BufferedImage rgb = new BufferedImage(p1.getWidth(), p1.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgb.createGraphics().drawImage(p1, 0, 0, null);
            //p1.copyData(rgb.getRaster());
            return p.with(d, rgb);
        };
    }

    public static Function<PipelineContext,PipelineContext> toBufferedImage(String s, String d) {
        return (p)->p.with(d, p.<Plane>get(s).toBufferedImage());
    }

    public static Consumer<PipelineContext> write(String s, String i, String filename) {
        return (p)->{
            try {
                write(p.<BufferedImage>get(s), filename+"-"+p.get(i));
            }
            catch(IOException e) {
                System.err.println(e.toString());
            }
        };
    }

    public static void write(BufferedImage i, String filename) throws IOException {
        File file = new File(filename);
        if(file.getName().endsWith(".gif")) {
            ImageIO.write(i, "png", file);
        }
        else if(file.getName().endsWith(".jpg")) {
            ImageIO.write(i, "jpg", file);
        }
        else {
            if(!file.getName().endsWith(".png")) {
                file = new File(file.toString()+".png");
            }
            ImageIO.write(i, "png", file);
        }
    }
}
