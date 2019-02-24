package org.excelsi.nausicaa.ca;



import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.excelsi.nausicaa.AnimatedGifEncoder;


public class CAGenerator implements Runnable {
    private int _cores = 4;
    private boolean _animate;
    private boolean _gif;
    private int _numFrames = 1000;
    private int _frameRate = 15;
    private float _scale = 1f;
    private CA _ca;
    private String _outputFile;
    private int _width;
    private int _height;
    private float _weight = 1f;
    private int _skipFrames = 0;
    private Progress _prog = new Noprogress();
    private Rendering _rend = new Rendering()
            .composition(Rendering.Composition.from("front"));

    
    public CAGenerator progress(Progress p) {
        _prog = p;
        return this;
    }

    public CAGenerator cores(int c) {
        _cores = c;
        return this;
    }

    public CAGenerator animate(boolean a) {
        _animate = a;
        return this;
    }

    public CAGenerator gif(boolean g) {
        _gif = g;
        return this;
    }

    public CAGenerator frames(int f) {
        _numFrames = f;
        return this;
    }

    public CAGenerator frameRate(int r) {
        _frameRate = r;
        return this;
    }

    public CAGenerator scale(float s) {
        _scale = s;
        return this;
    }

    public CAGenerator ca(CA c) {
        _ca = c;
        return this;
    }

    public CAGenerator output(String f) {
        _outputFile = f;
        return this;
    }

    public CAGenerator width(int w) {
        _width = w;
        return this;
    }

    public CAGenerator height(int h) {
        _height = h;
        return this;
    }

    public CAGenerator weight(float w) {
        _weight = w;
        return this;
    }

    public CAGenerator skipFrames(int s) {
        _skipFrames = s;
        return this;
    }

    @Override public void run() {
        if(_ca==null) {
            throw new IllegalStateException("no ca specified");
        }
        if(_outputFile==null) {
            throw new IllegalStateException("no output file specified");
        }
        final CA ca = _ca;
        final Rule rule = _ca.getRule();
        final int numFrames = _numFrames;
        final int frameRate = _frameRate;
        final float scale = _scale;
        final boolean intermediate = _animate;
        final boolean createMp4 = false;
        final boolean createWebm = false;
        final boolean createGif = _gif;
        final boolean rever = false;
        final boolean bb = false;
        final int w = _width==0?ca.getWidth():_width;
        final int h = _height==0?ca.getHeight():_height;
        final float frameWeight = _weight;
        final int ccores = _cores;
        final String selfile = _outputFile;
        final int skipFrames = _skipFrames;
        final Progress prog = _prog;
        System.err.println("frames: "+numFrames+", w="+w+", h="+h);
        try {
            if(rule.dimensions()==1) {
                //CA c = new CA(w, h);
                //initCA(c, _r);
                CA c = ca.size(w, h);
                Plane plane = c.createPlane();
                /*
                rule.generate(c, 1, h-1, false, false, new Rule.Updater() {
                    public void update(Rule r, int start, int current, int end) {
                        prog.setValue(current);
                    }

                    public long interval() {
                        return -1;
                    }
                });
                */
                if(!Thread.currentThread().isInterrupted()) {
                    //SwingUtilities.invokeLater(new Runnable() {
                        //public void run() {
                            //prog.setIndeterminate(true);
                        //}
                    //});
                    //hack[0].setEnabled(false);
                    plane.save(selfile, _rend);
                }
            }
            else {
                //ffmpeg -f image2 -i life-%d.jpg -ab 128kb -vcodec mpeg4 -b 1200kb -mbd 2 -flags +4mv -trellis 2 -cmp 2 -subcmp 2 life.mp4
                //int numFrames = Integer.parseInt(frames.getText());
                //config.setVariable("generatorFrames", Integer.toString(numFrames));
                //int frameRate = Integer.parseInt(frates.getText());
                //config.setVariable("generatorFramerate", Integer.toString(frameRate));
                //float scale = Float.parseFloat(scaling.getText());
                prog.setMaximum(numFrames);
                //boolean intermediate = animate.isSelected();
                //boolean createMp4 = genMp4.isSelected();
                //boolean createWebm = genWebm.isSelected();
                //boolean createGif = genGif.isSelected();
                //boolean rever = reverse.isSelected();
                //boolean bb = bigbounce.isSelected();
                String ext = (createMp4||createWebm)?".jpg":".png";
                ext = ".png";
                //CA c = new CA(w, h);
                //initCA(c, _r);
                CA c = ca.size(w, h);
                //Iterator<CA> cas = ((Multirule2D)_r).frames(c);
                if(createGif) {
                    Plane plane = c.createPlane();
                    ExecutorService pool = Executors.newFixedThreadPool(1);
                    Iterator<Plane> cas = c.getRule().frameIterator(plane, pool, new GOptions(true, 1, 1, frameWeight));
                    AnimatedGifEncoder age = new AnimatedGifEncoder();
                    age.start(selfile.endsWith(".gif")?selfile:(selfile+".gif"));
                    age.setRepeat(0);
                    if(!bb) {
                        age.setDelay(frameRate);
                    }
                    LinkedList<BufferedImage> frames = new LinkedList<BufferedImage>();
                    for(int i=0;i<numFrames;i++) {
                        if(Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        if(rever||bb) {
                            BufferedImage orig = plane.toBufferedImage();
                            BufferedImage copy = new BufferedImage(orig.getColorModel(), orig.copyData(null), orig.isAlphaPremultiplied(), null);
                            frames.add(copy);
                        }
                        else {
                            age.addFrame(plane.toBufferedImage());
                        }
                        prog.setValue(i);
                        for(int j=0;j<=skipFrames;j++) {
                            plane = cas.next();
                        }
                    }
                    prog.setIndeterminate(true);
                    if(bb) {
                        int delay = 50;
                        age.setDelay(delay);
                        for(int i=0;i<frames.size();i++) {
                            age.setDelay(delay);
                            age.addFrame(frames.get(frames.size()-i-1));
                            delay = (int) (200f*Math.pow((double)i/frames.size(), 2)+50);
                        }
                        for(int i=0;i<frames.size();i++) {
                            age.setDelay(delay);
                            age.addFrame(frames.get(i));
                            delay = (int) (200f*Math.pow((frames.size()-i)/(double)frames.size(), 2)+50);
                        }
                    }
                    else if(rever) {
                        for(int i=frames.size()-1;i>=0;i--) {
                            age.addFrame(frames.get(i));
                        }
                    }
                    age.finish();
                    pool.shutdown();
                }
                else {
                    //Plane p0 = c.createPlane();
                    System.err.println("using "+ccores+" cores");
                    ExecutorService pool = Executors.newFixedThreadPool(ccores);
                    if(scale==1f) {
                        System.err.println("skip scaling");
                        c.getRule()
                            .stream(c.createPlane(), pool, new GOptions(true, ccores, 1, frameWeight))
                            .limit(numFrames)
                            .map(Pipeline.context("p", "i", Pipeline.identifier()))
                            .map(Pipeline.toBufferedImage("p", "b"))
                            .forEach(Pipeline
                                .write("b", "i", selfile)
                                .andThen((p)->prog.setValue(p.<Long>get("i").intValue()))
                            );
                    }
                    else {
                        System.err.println("scaling");
                        c.getRule()
                            .stream(c.createPlane(), pool, new GOptions(true, ccores, 1, frameWeight))
                            .limit(numFrames)
                            .map(Pipeline.context("p", "i", Pipeline.identifier()))
                            .map(Pipeline.toBufferedImage("p", "b"))
                            .map(Pipeline.indexed2Rgb("b", "b"))
                            .map(Pipeline.scale("b", "b", scale))
                            .forEach(Pipeline
                                .write("b", "i", selfile)
                                .andThen((p)->prog.setValue(p.<Long>get("i").intValue()))
                            );
                    }
                    pool.shutdown();

                    /*
                    for(int i=0;i<numFrames;i++) {
                        if(Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        if(intermediate&&(!createGif||createMp4||createWebm)) {
                            if(scale!=1f) {
                                plane = plane.scale(scale);
                            }
                            plane.save(selfile+"-"+(rever?numFrames-i-1:i)+ext);
                        }
                        if(Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        prog.setValue(i);
                        plane = cas.next();
                    }
                    */
                }
                if(createWebm) {
                    // ffmpeg -y -r 30 -f image2 -threads 4 -i frame-%d.png -c:v libvpx -crf 4 -b:v 12M -qmin 0 -qmax 50 -deadline best output.webm
                    /*
                    Runner pb1 = new Runner(
                        "ffmpeg", "-y",
                        "-loglevel", "fatal",
                        "-r", frameRate+"",
                        "-f", "image2",
                        "-threads", "4",
                        "-i", selfile+"-%d"+ext,
                        "-c:v", "libvpx", "-crf", "4",
                        "-b:v", "12M", "-qmin", "0", "-qmax", "50",
                        selfile+".webm");
                    int exit1 = pb1.go();
                    if(exit1==0) {
                        if(!createMp4) {
                            for(int i=0;i<numFrames;i++) {
                                new File(selfile+"-"+i+ext).delete();
                            }
                        }
                    }
                    else {
                        System.err.println("exited with value "+exit1);
                    }
                    */
                }
                if(createMp4) {
                    // -c:v libx264 -pix_fmt yuv420p -preset medium -crf 24 -an
                    /*
                    Runner pb1 = new Runner(
                        "ffmpeg", "-y",
                        "-r", frameRate+"",
                        "-f", "image2",
                        "-i", selfile+"-%d"+ext,
                        "-c:v", "libx264", "-pix_fmt", "yuv420p", "-preset", "medium", "-crf", "24", "-an",
                        selfile+".mp4");
                    int exit1 = pb1.go();
                    */
                    /*
                    int exit2 = -1;
                    if(exit1==0) {
                        Runner pb2 = new Runner(
                            "/usr/local/bin/ffmpeg",
                            "-i", selfile+".p1.mp4",
                            "-acodec", "libfaac",
                            "-ab", "128kb",
                            "-pass", "2",
                            "-vcodec", "libx264",
                            "-vpre", "hq",
                            "-b", "2048kb",
                            "-bt", "2048kb",
                            "-threads", "0",
                            selfile+".mp4");
                        exit2 = pb2.go();
                    }
                    else {
                        System.err.println("exited with value "+exit1);
                    }
                    */
                    /*
                    if(exit1==0) {
                        for(int i=0;i<numFrames;i++) {
                            new File(selfile+"-"+i+ext).delete();
                        }
                    }
                    else {
                        System.err.println("exited with value "+exit1);
                    }
                    */
                }
            }
        }
        catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public interface Progress {
        void setIndeterminate(boolean i);
        void setMaximum(long m);
        void setValue(long v);
    }

    public static class Noprogress implements Progress {
        @Override public void setIndeterminate(boolean i) {
        }

        @Override public void setMaximum(long m) {
        }

        @Override public void setValue(long v) {
        }
    }

    public static class StderrProgress implements Progress {
        private long _m;
        private boolean _ind;
        private long _v;
        private String _last;


        @Override public void setIndeterminate(boolean i) {
            _ind = i;
            if(_ind) {
                System.out.println("¯\\_(ツ)_/¯");
            }
            else {
                System.out.println("(._.)");
            }
        }

        @Override public void setMaximum(long m) {
            _m = m;
        }

        @Override public void setValue(long v) {
            _v = v;
            update();
        }

        private void update() {
            String msg = Math.round((double)_v/(double)_m*100d)+"%";
            if(!msg.equals(_last)) {
                _last = msg;
                System.err.println(msg);
            }
        }
    }
}
