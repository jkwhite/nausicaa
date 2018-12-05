package org.excelsi.nausicaa;


import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import org.excelsi.nausicaa.ca.*;


public class JCAGenerator extends JDialog {
    public JCAGenerator(final JFrame root, final CA ca, final Config config) {
        super(root, "Generate automata to disk");
        final Rule rule = ca.getRule();
        final JDialog d = this;
        final String _lastWidth = config.<String>getVariable("generatorWidth", "1920");
        final String _lastHeight = config.<String>getVariable("generatorHeight", "1080");
        final String _lastFrames = config.<String>getVariable("generatorFrames", "1000");
        final String _cores = config.<String>getVariable("generatorCores", "4");
        final String _skipframes = config.<String>getVariable("generatorSkipframes", "0");
        final String initFramerate = config.<String>getVariable("generateFramerate", "15");
        final float frameWeight = config.getFloatVariable("weight", 1f);
        final Rendering rend = new Rendering()
            .composition(Rendering.Composition.from(config.getVariable("composite_mode","front")));
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridLayout gl = null;
        if(rule.dimensions()==1) {
            gl = new GridLayout(2,2);
        }
        else {
            gl = new GridLayout(13,2);
        }
        JPanel top = new JPanel(gl);
        top.add(new JLabel("Width"));
        final JTextField width = new JTextField();
        width.setText(""+_lastWidth);
        width.setColumns(6);
        JPanel wp = new JPanel();
        wp.add(width);
        wp.add(new JLabel("px"));
        top.add(wp);
        JPanel hp = new JPanel();
        top.add(new JLabel("Height"));
        final JTextField height = new JTextField();
        height.setText(""+_lastHeight);
        height.setColumns(6);
        hp.add(height);
        hp.add(new JLabel("px"));
        top.add(hp);
        JCheckBox animat = null;
        JCheckBox mp4 = null;
        JCheckBox webm = null;
        JCheckBox gif = null;
        JCheckBox bigbounc = null;
        JCheckBox revers = null;
        JTextField frame = null;
        JTextField framerate = null;
        JTextField scalingf = null;
        JTextField coresf = null;
        JTextField skipf = null;
        if(rule.dimensions()>1) {
            JPanel an = new JPanel();
            animat = new JCheckBox("Animate");
            animat.setSelected(true);
            JPanel fr = new JPanel();
            frame = new JTextField();
            frame.setText(""+_lastFrames);
            frame.setColumns(6);
            JPanel frate = new JPanel();
            framerate = new JTextField();
            framerate.setText(initFramerate);
            framerate.setColumns(4);
            JPanel scalingp = new JPanel();
            scalingf = new JTextField();
            scalingf.setText("1.0");
            scalingf.setColumns(4);
            JPanel coresp = new JPanel();
            coresf = new JTextField();
            coresf.setText(_cores);
            coresf.setColumns(4);

            JPanel skipfp = new JPanel();
            skipf = new JTextField();
            skipf.setText(_skipframes);
            skipf.setColumns(4);

            an.add(animat);
            top.add(an);
            top.add(new JLabel(""));

            top.add(new JLabel("Frames"));
            fr.add(frame);
            top.add(fr);

            top.add(new JLabel("Framerate"));
            frate.add(framerate);
            top.add(frate);

            top.add(new JLabel("Scaling"));
            scalingp.add(scalingf);
            top.add(scalingp);

            top.add(new JLabel("Cores"));
            coresp.add(coresf);
            top.add(coresp);

            top.add(new JLabel("Skip frames"));
            skipfp.add(skipf);
            top.add(skipfp);

            mp4 = new JCheckBox("Create MP4");
            JPanel p4 = new JPanel();
            p4.add(mp4);
            top.add(p4);
            top.add(new JLabel(""));

            webm = new JCheckBox("Create WebM");
            JPanel ebm = new JPanel();
            ebm.add(webm);
            top.add(ebm);
            top.add(new JLabel(""));

            gif = new JCheckBox("Create GIF");
            JPanel jg = new JPanel();
            jg.add(gif);
            top.add(jg);
            top.add(new JLabel(""));

            JPanel rev = new JPanel();
            revers = new JCheckBox("Reverse");
            rev.add(revers);
            top.add(rev);
            top.add(new JLabel(""));

            JPanel bigb = new JPanel();
            bigbounc = new JCheckBox("Big Bounce");
            bigb.add(bigbounc);
            top.add(bigb);
            top.add(new JLabel(""));
        }
        final JCheckBox animate = animat;
        final JCheckBox genMp4 = mp4;
        final JCheckBox genWebm = webm;
        final JCheckBox genGif = gif;
        final JCheckBox bigbounce = bigbounc;
        final JTextField frames = frame;
        final JTextField frates = framerate;
        final JTextField scaling = scalingf;
        final JTextField cores = coresf;
        final JTextField skipframes = skipf;
        final JCheckBox reverse = revers;

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Generate ...");
        d.getRootPane().setDefaultButton(ne);

        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                final JFileChooser f = new JFileChooser(config.getGenDir());
                f.setDialogTitle("Save generated automata");
                f.setDialogType(f.SAVE_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showSaveDialog(root);
                if(ret==f.APPROVE_OPTION) {
                    String _dir = f.getSelectedFile().getParentFile().toString();
                    config.setGenDir(_dir);
                    final JDialog gene = new JDialog(root, "Generating");
                    JPanel main = new JPanel(new BorderLayout());
                    final JLabel task = new JLabel("Building automata");
                    Font font = task.getFont();
                    task.setFont(font.deriveFont(font.getSize()-2f));
                    final int w = Integer.parseInt(width.getText());
                    final int h = Integer.parseInt(height.getText());
                    final int ccores = Integer.parseInt(cores.getText());
                    config.setVariable("generatorWidth", Integer.toString(w));
                    config.setVariable("generatorHeight", Integer.toString(h));
                    final int skipFrames = Integer.parseInt(skipframes.getText());
                    config.setVariable("generatorFramerate", skipframes.getText());
                    config.setVariable("generatorSkipframes", skipframes.getText());
                    //_lastWidth = w;
                    //_lastHeight = h;
                    final JProgressBar prog = new JProgressBar(1, h);
                    final String selfile = f.getSelectedFile().toString();
                    prog.setValue(0);
                    main.add(prog, BorderLayout.NORTH);
                    main.add(task, BorderLayout.WEST);
                    main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    gene.add(main, BorderLayout.CENTER);
                    final JButton[] hack = new JButton[1];

                    final Thread builder = new Thread() {
                        public void run() {
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
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                task.setText("Writing "+selfile);
                                                prog.setIndeterminate(true);
                                            }
                                        });
                                        hack[0].setEnabled(false);
                                        plane.save(selfile, rend);
                                    }
                                }
                                else {
                                    //ffmpeg -f image2 -i life-%d.jpg -ab 128kb -vcodec mpeg4 -b 1200kb -mbd 2 -flags +4mv -trellis 2 -cmp 2 -subcmp 2 life.mp4
                                    int numFrames = Integer.parseInt(frames.getText());
                                    config.setVariable("generatorFrames", Integer.toString(numFrames));
                                    int frameRate = Integer.parseInt(frates.getText());
                                    config.setVariable("generatorFramerate", Integer.toString(frameRate));
                                    float scale = Float.parseFloat(scaling.getText());
                                    prog.setMaximum(numFrames);
                                    boolean intermediate = animate.isSelected();
                                    boolean createMp4 = genMp4.isSelected();
                                    boolean createWebm = genWebm.isSelected();
                                    boolean createGif = genGif.isSelected();
                                    boolean rever = reverse.isSelected();
                                    boolean bb = bigbounce.isSelected();
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
                                    }
                                    if(createMp4) {
                                        // -c:v libx264 -pix_fmt yuv420p -preset medium -crf 24 -an
                                        Runner pb1 = new Runner(
                                            "ffmpeg", "-y",
                                            "-r", frameRate+"",
                                            "-f", "image2",
                                            "-i", selfile+"-%d"+ext,
                                            "-c:v", "libx264", "-pix_fmt", "yuv420p", "-preset", "medium", "-crf", "24", "-an",
                                            selfile+".mp4");
                                        int exit1 = pb1.go();
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
                                        if(exit1==0) {
                                            for(int i=0;i<numFrames;i++) {
                                                new File(selfile+"-"+i+ext).delete();
                                            }
                                        }
                                        else {
                                            System.err.println("exited with value "+exit1);
                                        }
                                    }
                                }
                            }
                            catch(IOException e) {
                                e.printStackTrace();
                            }
                            finally {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        gene.dispose();
                                        //_label.requestFocus();
                                    }
                                });
                            }
                        }
                    };
                    final JButton cancel = new JButton("Cancel");
                    cancel.addActionListener(new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            task.setText("Canceling");
                            cancel.setEnabled(false);
                            builder.interrupt();
                        }
                    });
                    hack[0] = cancel;
                    JPanel south = new JPanel(new BorderLayout());
                    south.add(cancel, BorderLayout.EAST);
                    main.add(south, BorderLayout.SOUTH);
                    Dimension di = main.getPreferredSize();
                    gene.setSize(100+di.width, 50+di.height);
                    Things.centerWindow(gene);
                    gene.setVisible(true);
                    builder.start();
                }
            }
        });

        bot.add(ne);
        p.add(bot, BorderLayout.SOUTH);
        d.getContentPane().add(p);
        Dimension dim = p.getPreferredSize();
        dim.height += 40;
        d.setSize(dim);
        Things.centerWindow(d);
        d.setVisible(true);
    }
}
