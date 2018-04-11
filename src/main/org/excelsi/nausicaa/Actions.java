package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.Archetype;
import org.excelsi.nausicaa.ca.Blobs;
import org.excelsi.nausicaa.ca.Mutator;
import org.excelsi.nausicaa.ca.Symmetry;
import org.excelsi.nausicaa.ca.SymmetryForcer;
import org.excelsi.nausicaa.ca.Colors;
import org.excelsi.nausicaa.ca.CA;
import org.excelsi.nausicaa.ca.MutatorFactory;
import org.excelsi.nausicaa.ca.Plane;
import org.excelsi.nausicaa.ca.Palette;
import org.excelsi.nausicaa.ca.Rule;
import org.excelsi.nausicaa.ca.RuleTransform;
import org.excelsi.nausicaa.ca.Evolver;
import org.excelsi.nausicaa.ca.EvolverBuilder;
import org.excelsi.nausicaa.ca.Fitness;
import org.excelsi.nausicaa.ca.FitnessCriteria;
import org.excelsi.nausicaa.ca.Encoder;
import org.excelsi.nausicaa.ca.RandomMutationStrategy;
import org.excelsi.nausicaa.ca.RandomInitializer;
import org.excelsi.nausicaa.ca.GaussianInitializer;
import org.excelsi.nausicaa.ca.CAInitializer;
import org.excelsi.nausicaa.ca.ClusteredGaussianInitializer;
import org.excelsi.nausicaa.ca.WordEncoder;
import org.excelsi.nausicaa.ca.ByteInitializer;
import org.excelsi.nausicaa.ca.WordInitializer;
import org.excelsi.nausicaa.ca.ImageInitializer;
import org.excelsi.nausicaa.ca.SingleInitializer;
import org.excelsi.nausicaa.ca.Initializer;
import org.excelsi.nausicaa.ca.Training;
import org.excelsi.nausicaa.ca.RetryingMutationStrategy;
import org.excelsi.nausicaa.ca.MutationStrategies;
import org.excelsi.nausicaa.ca.Mutatable;
import org.excelsi.nausicaa.ca.MutationFactor;
import org.excelsi.nausicaa.ca.Stats;
import org.excelsi.nausicaa.ca.Multistats;
import org.excelsi.nausicaa.ca.Pools;
import org.excelsi.nausicaa.ca.Ruleset;
import org.excelsi.nausicaa.ca.ComputedRuleset;
import org.excelsi.nausicaa.ca.Initializers;
import org.excelsi.nausicaa.ca.GOptions;

import java.math.BigInteger;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Random;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.util.concurrent.Executors;


public class Actions {
    public void newCA(NViewer v) {
        final JDialog d = new JDialog(v, "New automata");
        final Config config = v.getConfig();
        //v.pack();
        //Things.centerWindow(v);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(3,2));

        top.add(new JLabel("Dimensions"));
        final JTextField alpha = new JTextField();
        alpha.setText(config.getVariable("default_dimensions", "2"));
        alpha.setColumns(3);
        top.add(alpha);

        top.add(new JLabel("Colors"));
        final JTextField mc = new JTextField();
        mc.setText(config.getVariable("default_colors", "2"));
        mc.setColumns(3);
        top.add(mc);

        top.add(new JLabel("Size"));
        final JTextField siz = new JTextField();
        siz.setText(config.getVariable("default_size", "1"));
        siz.setColumns(3);
        top.add(siz);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                Integer dims = Integer.parseInt(alpha.getText());
                Integer colors = Integer.parseInt(mc.getText());
                Integer size = Integer.parseInt(siz.getText());
                config.setVariable("default_dimensions", alpha.getText());
                config.setVariable("default_colors", mc.getText());
                config.setVariable("default_size", siz.getText());
                Archetype a = new Archetype(dims, size, colors);
                Random rand = new Random();
                Ruleset rs = new ComputedRuleset(a);
                Rule rule = rs.random(rand).next();
                Palette pal = Palette.random(colors, rand, true);
                CA ca = new CA(rule, pal, v.getActiveCA().getInitializer(), rand, 0, v.getConfig().getWidth(), v.getConfig().getHeight(), v.getConfig().getDepth(), v.getConfig().getPrelude(), v.getConfig().getWeight());
                v.setActiveCA(ca);
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

    public void newCAImage(NViewer v, Config config) {
        JFileChooser f = new JFileChooser(config.getDir());
        f.setDialogTitle("New CA from image");
        f.setDialogType(f.OPEN_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showOpenDialog(v.getRoot());
        if(ret==f.APPROVE_OPTION) {
            try {
                config.setDir(f.getSelectedFile().getParent());
                final CA ca = CA.fromImage(f.getSelectedFile().toString());
                config.setSize(ca.getWidth(), ca.getHeight(), ca.getDepth(), ca.getPrelude());
                //config.setSize(ca.getWidth(), ca.getHeight(), ca.getDepth());
                v.setActiveCA(ca);
            }
            catch(IOException e) {
                showError(v, "Failed to load "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void load(NViewer v, Config config) {
        JFileChooser f = new JFileChooser(config.getDir());
        f.setDialogTitle("Open automata");
        f.setDialogType(f.OPEN_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showOpenDialog(v.getRoot());
        if(ret==f.APPROVE_OPTION) {
            try {
                config.setDir(f.getSelectedFile().getParent());
                final CA ca = CA.fromFile(f.getSelectedFile().toString(), "text");
                config.setSize(ca.getWidth(), ca.getHeight(), ca.getDepth());
                v.setActiveCA(ca);
            }
            catch(IOException e) {
                showError(v, "Failed to load "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void save(NViewer v, Config config) {
        JFileChooser f = new JFileChooser(config.getDir());
        f.setDialogTitle("Save automata");
        f.setDialogType(f.SAVE_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showSaveDialog(v.getRoot());
        CA ca = v.getActiveCA();
        if(ret==f.APPROVE_OPTION) {
            try {
                config.setDir(f.getSelectedFile().getParent());
                ca.save(f.getSelectedFile().toString(), "text");
            }
            catch(IOException e) {
                showError(v, "Failed to save "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void close(NViewer v) {
    }

    public void exportImage(NViewer v, Config config) {
        JFileChooser f = new JFileChooser(config.getDir());
        f.setDialogTitle("Export image");
        f.setDialogType(f.SAVE_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showSaveDialog(v.getRoot());
        CA ca = v.getActiveCA();
        Plane plane = v.getPlaneDisplayProvider().getActivePlane();
        if(ret==f.APPROVE_OPTION) {
            try {
                config.setDir(f.getSelectedFile().getParent());
                plane.save(f.getSelectedFile().toString());
            }
            catch(IOException e) {
                showError(v, "Failed to save "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void exportRule(NViewer v, Config config) {
        JFileChooser f = new JFileChooser(config.getDir());
        f.setDialogTitle("Export rule");
        f.setDialogType(f.SAVE_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showSaveDialog(v.getRoot());
        final Rule rule = v.getActiveCA().getRule();
        if(ret==f.APPROVE_OPTION) {
            try(PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(f.getSelectedFile().toString())))) {
                config.setDir(f.getSelectedFile().getParent());
                rule.write(w);
            }
            catch(IOException e) {
                showError(v, "Failed to save "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void debug(NViewer v) {
        final Plane p = v.getPlaneDisplayProvider().getActivePlane();
        final java.util.List<Blobs.Blob> blobs = new Blobs().blobs(p, Blobs.Mode.finite);
        for(Blobs.Blob b:blobs) {
            System.err.println(b.toString());
        }
    }

    private final String VIEW3D_PROGRAM = System.getProperty("app.root")+"/bin/nausicaa";
    public void external3dView(NViewer v, Config c) {
        final CA ca = v.getActiveCA();
        File f = null;
        try {
            f = File.createTempFile("ca_", ".ca");
            ca.save(f.toString(), "text");
            final ProcessBuilder b = new ProcessBuilder(VIEW3D_PROGRAM, "-jfx", f.toString());
            b.redirectError(ProcessBuilder.Redirect.INHERIT);
            b.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            final Process p = b.start();
            final File doomed = f;
            final Thread t = new Thread("Watch-"+f) {
                public void run() {
                    try {
                        p.waitFor();
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    doomed.delete();
                }
            };
            t.start();
        }
        catch(IOException e) {
            showError(v, "Failed to save: "+e.getClass().getName()+": "+e.getMessage(), e);
        }
    }

    public void info(NViewer v) {
        final CA ca = v.getActiveCA();
        final Plane plane = v.getPlaneDisplayProvider().getActivePlane();
        final Plane nextPlane = ca.getRule().frameIterator(plane, Pools.adhoc(), new GOptions(false, 1, 1, 1f)).next();
        final Stats stats = Stats.forPlane(plane);
        final Stats nextStats = Stats.forPlane(nextPlane);
        final Multistats ms = stats.compareWith(nextStats);

        Rule r = ca.getRule();
        final JFrame i = new JFrame("Info");
        InfoPanel p = new InfoPanel();
        //p.addPair("Universe", chop(r.humanize(),80));
        final String b64 = ca.toBase64();
        p.addPair("Universe", chop(b64,80));
        p.addPair("Text", createRuleText(b64));
        String id = r.id();
        if(id.length()<1000000) {
            String frval;
            try {
                BigInteger rval = new BigInteger(r.id(), r.colorCount());
                frval = rval.toString(10);
            }
            catch(Exception e) {
                e.printStackTrace();
                frval = "Error: "+r.id();
            }
            p.addPair("Rval ("+frval.length()+" digits)", createRuleText(frval));
        }
        else {
            p.addPair("Rval", id.length()+" digits elided");
        }
        if(b64.length()<100000) {
            p.addPair("Incantation", createRuleText(ca.toIncantation()));
        }
        p.addPair("Colors", createColorPanel(ca.getPalette()));
        p.addPair("Stats", createRuleText(ms.humanize(), false));
        /*
        if(r instanceof Multirule) {
            Rule[] chs = ((Multirule)r).rules();
            if(chs.length>1) {
                for(Rule ch:((Multirule)r).rules()) {
                    p.addPair(" ", " ");
                    p.addPair("  Rule", chop(ch.toString(),66));
                    p.addPair("  Verse", createRuleText(ch.toIncantation()));
                    p.addPair("  Colors", createColorPanel(ch.colors()));
                }
            }
        }
        */
        p.done();
        i.getContentPane().add(p);
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        AbstractAction close = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                i.setVisible(false);
            }
        };
        JMenuItem cl = file.add(close);
        cl.setText("Close");
        cl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, shortcut));
        bar.add(file);
        i.setJMenuBar(bar);
        i.pack();
        Things.centerWindow(i);
        i.setVisible(true);
    }

    public void cancel(NViewer v) {
    }

    public void generate(NViewer v) {
    }

    public void reroll(NViewer v) {
        final Config config = v.getConfig();
        config.setSeed(config.getSeed()+1);
    }

    private Animation _a;
    public void animate(NViewer v, Timeline timeline, int frames) {
        if(_a!=null) {
            _a.stopAnimation();
            _a = null;
        }
        else {
            _a = new Animation(v.getConfig(), v.getPlanescapeProvider(), timeline, frames);
            _a.start();
        }
    }

    public void animateSpeedup(NViewer v) {
        v.getConfig().setAnimationDelay(Math.max(1, (int)(v.getConfig().getAnimationDelay()/1.5)));
    }

    public void animateSlowdown(NViewer v) {
        v.getConfig().setAnimationDelay(Math.min(10000, Math.max(3, (int)(v.getConfig().getAnimationDelay()*1.5))));
    }

    public void coreConfig(NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Animation configuration");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(2,2));

        top.add(new JLabel("Compute cores"));
        final JTextField compute = new JTextField();
        compute.setText(config.getVariable("animation_computeCores", "2"));
        compute.setColumns(3);
        top.add(compute);

        top.add(new JLabel("Render cores"));
        final JTextField render = new JTextField();
        render.setText(config.getVariable("animation_renderCores", "2"));
        render.setColumns(3);
        top.add(render);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("animation_computeCores", compute.getText());
                config.setVariable("animation_renderCores", render.getText());
                config.notify("animation");
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

    public void paletteOptions(NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Palette options");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(3,2));

        top.add(new JLabel("Black Zero"));
        final JCheckBox bz = new JCheckBox();
        bz.setSelected("true".equals(config.getVariable("palette_blackzero", "true")));
        top.add(bz);

        top.add(new JLabel("Density"));
        final JTextField dense = new JTextField();
        dense.setText(config.getVariable("palette_cut", "0"));
        dense.setColumns(3);
        top.add(dense);

        top.add(new JLabel("Invisible %"));
        final JTextField invis = new JTextField();
        invis.setText(config.getVariable("palette_invisible", "0"));
        invis.setColumns(3);
        top.add(invis);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("palette_blackzero", ""+bz.isSelected());
                config.setVariable("palette_cut", dense.getText());
                config.setVariable("palette_invisible", invis.getText());
                config.notify("palette");
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


    public void generateToFile(NViewer v) {
        new JCAGenerator(v, v.getActiveCA(), v.getConfig());
    }

    public void mutationParams(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Mutation parameters");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(5,2));

        top.add(new JLabel("Alpha"));
        final JTextField alpha = new JTextField();
        alpha.setText(config.getVariable("mutator_alpha", "20"));
        alpha.setColumns(3);
        top.add(alpha);

        top.add(new JLabel("Max colors"));
        final JTextField mc = new JTextField();
        mc.setText(config.getVariable("mutator_maxcolors", "9"));
        mc.setColumns(3);
        top.add(mc);

        top.add(new JLabel("Mutation stage"));
        final JTextField ms = new JTextField();
        ms.setText(config.getVariable("mutator_stage", "0"));
        ms.setColumns(3);
        top.add(ms);

        top.add(new JLabel("Transition factor"));
        final JTextField tf = new JTextField();
        tf.setText(config.getVariable("mutator_transition", "0.5"));
        tf.setColumns(3);
        top.add(tf);

        top.add(new JLabel("Attempt symmetry"));
        final JCheckBox as = new JCheckBox();
        as.setSelected("true".equals(config.getVariable("mutator_symmetry", "false")));
        top.add(as);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("mutator_alpha", alpha.getText());
                config.setVariable("mutator_maxcolors", mc.getText());
                config.setVariable("mutator_stage", ms.getText());
                config.setVariable("mutator_transition", tf.getText());
                config.setVariable("mutator_symmetry", ""+as.isSelected());
                config.notify("mutator");
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

    public void chooseRandom(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Random initializer");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(1,2));

        top.add(new JLabel("Zero weight"));
        final JTextField zeroWeight = new JTextField();
        zeroWeight.setText(config.getVariable("random_zeroweight", "0"));
        zeroWeight.setColumns(10);
        top.add(zeroWeight);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("random_zeroweight", zeroWeight.getText());
                v.setInitializer(new RandomInitializer(null, 0, new RandomInitializer.Params(Float.parseFloat(zeroWeight.getText()))));
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

    public void chooseGaussian(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Gaussian initializer");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(/*rows*/4,2));

        top.add(new JLabel("Zero weight"));
        final JTextField zeroWeight = new JTextField();
        zeroWeight.setText(config.getVariable("gaussian_zeroweight", "0"));
        zeroWeight.setColumns(10);
        top.add(zeroWeight);

        top.add(new JLabel("Max points"));
        final JTextField maxpoints = new JTextField();
        maxpoints.setText(config.getVariable("gaussian_maxpoints", "5"));
        maxpoints.setColumns(10);
        top.add(maxpoints);

        top.add(new JLabel("Max radius"));
        final JTextField maxrad = new JTextField();
        maxrad.setText(config.getVariable("gaussian_maxradius", "0.1"));
        maxrad.setColumns(10);
        top.add(maxrad);

        top.add(new JLabel("Density"));
        final JTextField density = new JTextField();
        density.setText(config.getVariable("gaussian_density", "0.2"));
        density.setColumns(10);
        top.add(density);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("gaussian_zeroweight", zeroWeight.getText());
                config.setVariable("gaussian_maxpoints", maxpoints.getText());
                config.setVariable("gaussian_maxradius", maxrad.getText());
                config.setVariable("gaussian_density", density.getText());
                v.setInitializer(new GaussianInitializer(null, 0,
                    new GaussianInitializer.Params(
                        Float.parseFloat(zeroWeight.getText()),
                        Integer.parseInt(maxpoints.getText()),
                        Float.parseFloat(maxrad.getText()),
                        Float.parseFloat(density.getText())
                    )));
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

    public void chooseClusteredGaussian(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Clustered gaussian initializer");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(/*rows*/5,2));

        top.add(new JLabel("Zero weight"));
        final JTextField zeroWeight = new JTextField();
        zeroWeight.setText(config.getVariable("clusteredgaussian_zeroweight", "0"));
        zeroWeight.setColumns(10);
        top.add(zeroWeight);

        top.add(new JLabel("Max points"));
        final JTextField maxpoints = new JTextField();
        maxpoints.setText(config.getVariable("clusteredgaussian_maxpoints", "5"));
        maxpoints.setColumns(10);
        top.add(maxpoints);

        top.add(new JLabel("Max radius"));
        final JTextField maxrad = new JTextField();
        maxrad.setText(config.getVariable("clusteredgaussian_maxradius", "0.1"));
        maxrad.setColumns(10);
        top.add(maxrad);

        top.add(new JLabel("Density"));
        final JTextField density = new JTextField();
        density.setText(config.getVariable("clusteredgaussian_density", "0.2"));
        density.setColumns(10);
        top.add(density);

        top.add(new JLabel("Skew"));
        final JTextField skew = new JTextField();
        skew.setText(config.getVariable("clusteredgaussian_skew", "0.2"));
        skew.setColumns(10);
        top.add(skew);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("clusteredgaussian_zeroweight", zeroWeight.getText());
                config.setVariable("clusteredgaussian_maxpoints", maxpoints.getText());
                config.setVariable("clusteredgaussian_maxradius", maxrad.getText());
                config.setVariable("clusteredgaussian_density", density.getText());
                config.setVariable("clusteredgaussian_skew", skew.getText());
                v.setInitializer(new ClusteredGaussianInitializer(null, 0,
                    new ClusteredGaussianInitializer.Params(
                        Float.parseFloat(zeroWeight.getText()),
                        Integer.parseInt(maxpoints.getText()),
                        Float.parseFloat(maxrad.getText()),
                        Float.parseFloat(density.getText()),
                        Float.parseFloat(skew.getText())
                    )));
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

    public void chooseWord(final NViewer v) {
        final JDialog d = new JDialog(v, "Choose word initializer");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(4,2));

        top.add(new JLabel("Alphabet"));
        final JTextField alphabet = new JTextField();
        alphabet.setText(WordInitializer.ALPAHBET);
        alphabet.setColumns(10);
        top.add(alphabet);

        top.add(new JLabel("Word"));
        final JTextField word = new JTextField();
        word.setText(WordInitializer.WORD);
        word.setColumns(20);
        top.add(word);

        top.add(new JLabel("Target"));
        final JTextField target = new JTextField();
        target.setText(WordInitializer.TARGET);
        target.setColumns(20);
        top.add(target);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                WordInitializer.WORD = word.getText();
                WordInitializer.ALPAHBET = alphabet.getText();
                WordInitializer.TARGET = target.getText();
                v.setInitializer(new WordInitializer(alphabet.getText(), word.getText()));
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

    public void chooseImage(final NViewer v, Config config) {
        final String[] filehack = new String[1];
        final String[] methack = new String[1];

        final JDialog d = new JDialog(v, "Choose image initializer");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(3,2));

        AbstractAction choosefile = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser f = new JFileChooser(config.getDir());
                f.setDialogTitle("Initial state image");
                f.setDialogType(f.OPEN_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showOpenDialog(v);
                if(ret==f.APPROVE_OPTION) {
                    File img = f.getSelectedFile();
                    config.setDir(img.getParent());
                    filehack[0] = img.toString();
                    //BufferedImage initImage = ImageIO.read(img);
                    System.err.println("read image "+img);
                    //v.setInitializer(new ImageInitializer(initImage));
                }
            }
        };

        top.add(new JLabel("Image"));
        final JButton file = new JButton(choosefile);
        file.setText("Choose file");
        top.add(file);

        top.add(new JLabel("Method"));
        ButtonGroup met = new ButtonGroup();

        AbstractAction center = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                methack[0] = "center";
            }
        };
        JRadioButton rcenter = new JRadioButton(center);
        rcenter.setText("Center");
        met.add(rcenter);
        AbstractAction tile = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                methack[0] = "tile";
            }
        };
        JRadioButton rtile = new JRadioButton(center);
        rtile.setText("Tile");
        met.add(rtile);
        AbstractAction none = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                methack[0] = "none";
            }
        };
        JRadioButton rnone = new JRadioButton(none);
        rnone.setText("None");
        met.add(rnone);
        JPanel methods = new JPanel();
        methods.add(rcenter);
        methods.add(rtile);
        methods.add(rnone);
        top.add(methods);

        final boolean[] scalehack = new boolean[1];
        top.add(new JLabel("Scale"));
        JCheckBox scl = new JCheckBox(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scalehack[0] = !scalehack[0];
            }
        });
        top.add(scl);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                try {
                    //BufferedImage initImage = ImageIO.read(new File(filehack[0]));
                    boolean cent = "center".equals(methack[0]);
                    boolean til = "tile".equals(methack[0]);
                    boolean sc = scl.isSelected();
                    //v.setInitializer(new ImageInitializer(initImage, new ImageInitializer.Params(cent, til, sc)));
                    v.setInitializer(new ImageInitializer(new File(filehack[0]), new ImageInitializer.Params(cent, til, sc)));
                }
                catch(IOException ex) {
                    ex.printStackTrace();
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

    public void chooseCAInitializer(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "CA initializer");
        final String[] filehack = new String[1];
        filehack[0] = config.getVariable("cainitializer_file", "");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(2,2));

        AbstractAction choosefile = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser f = new JFileChooser(config.getDir());
                f.setDialogTitle("Automata");
                f.setDialogType(f.OPEN_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showOpenDialog(v);
                if(ret==f.APPROVE_OPTION) {
                    File ca = f.getSelectedFile();
                    config.setDir(ca.getParent());
                    filehack[0] = ca.toString();
                }
            }
        };

        top.add(new JLabel("Automata"));
        final JButton file = new JButton(choosefile);
        file.setText("Choose file");
        top.add(file);

        top.add(new JLabel("Iterations"));
        final JTextField iter = new JTextField();
        iter.setColumns(5);
        iter.setText(config.getVariable("cainitializer_iter", "100"));
        top.add(iter);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                String url = filehack[0];
                int it = Integer.parseInt(iter.getText());
                config.setVariable("cainitializer_file", url);
                config.setVariable("cainitializer_iter", ""+it);
                try {
                    v.setInitializer(new CAInitializer(url, it));
                }
                catch(IOException ex) {
                    ex.printStackTrace();
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

    public void imageSpectrum(final NViewer v, Config config) {
        final JFileChooser f = new JFileChooser(config.getDir());
        f.setDialogTitle("Spectrum source image");
        f.setDialogType(f.OPEN_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showOpenDialog(v);
        if(ret==f.APPROVE_OPTION) {
            File img = f.getSelectedFile();
            config.setDir(img.getParent());
            try {
                BufferedImage bi = ImageIO.read(img);
                System.err.println("read image "+bi);
                Palette p = Palette.fromImage(bi);
                if(v.getActiveCA().archetype().colors()!=p.getColorCount()) {
                    throw new IllegalArgumentException("archetype colors "+v.getActiveCA().archetype().colors()
                        +" does not match image colors "+p.getColorCount());
                }
                else {
                    v.setActiveCA(v.getActiveCA().palette(p));
                }
                //v.setInitializer(new ImageInitializer(initImage));
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void customSpectrum(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Custom Spectrum");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(4,2));

        top.add(new JLabel("Number of colors"));
        final JTextField cols = new JTextField();
        cols.setColumns(10);
        cols.setText(config.getVariable("customspectrum_colors", "10"));
        top.add(cols);

        top.add(new JLabel("Density"));
        final JTextField dens = new JTextField();
        dens.setColumns(10);
        dens.setText(config.getVariable("customspectrum_density", "0"));
        top.add(dens);

        top.add(new JLabel("Black zero"));
        final JCheckBox bz = new JCheckBox();
        bz.setSelected("true".equals(config.getVariable("customspectrum_blackzero", "true")));
        top.add(bz);

        top.add(new JLabel("Invisible %"));
        final JTextField invis = new JTextField();
        invis.setColumns(10);
        invis.setText(config.getVariable("customspectrum_invisible", "0"));
        top.add(invis);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                String cc = cols.getText();
                int clrs = Integer.parseInt(cols.getText());
                int de = Integer.parseInt(dens.getText());
                boolean blz = bz.isSelected();
                int inv = Integer.parseInt(invis.getText());
                config.setVariable("customspectrum_colors", cc);
                config.setVariable("customspectrum_density", ""+de);
                config.setVariable("customspectrum_blackzero", ""+blz);
                config.setVariable("customspectrum_invisible", ""+inv);
                v.setActiveCA(v.getActiveCA().palette(Palette.randomCutRainbow(v.getRandom(), v.getActiveCA().archetype().colors(), de, blz, clrs, inv)));
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

    public void evolver(NViewer v, Random random) {
        final Config config = v.getConfig();
        final JDialog d = new JDialog(v, "Evolver");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(9,2));

        top.add(new JLabel("Epicycles"));
        final JTextField epicycles = new JTextField();
        epicycles.setText(config.<String>getVariable("evo_epicycles", "1"));
        epicycles.setColumns(6);
        top.add(epicycles);

        top.add(new JLabel("Iterations"));
        final JTextField iterations = new JTextField();
        iterations.setText(config.<String>getVariable("evo_iterations", "100"));
        iterations.setColumns(6);
        top.add(iterations);

        top.add(new JLabel("Subiterations"));
        final JTextField subiterations = new JTextField();
        subiterations.setText(config.<String>getVariable("evo_subiterations", "1"));
        subiterations.setColumns(6);
        top.add(subiterations);

        top.add(new JLabel("Population"));
        final JTextField pop = new JTextField();
        pop.setText(config.<String>getVariable("evo_population", "10"));
        pop.setColumns(6);
        top.add(pop);

        top.add(new JLabel("Birth rate"));
        final JTextField births = new JTextField();
        births.setText(config.<String>getVariable("evo_birthrate", "0.1"));
        births.setColumns(6);
        top.add(births);

        top.add(new JLabel("Death rate"));
        final JTextField deaths = new JTextField();
        deaths.setText(config.<String>getVariable("evo_deathrate", "0.1"));
        deaths.setColumns(6);
        top.add(deaths);

        top.add(new JLabel("Color limit"));
        final JTextField colorlimit = new JTextField();
        colorlimit.setText(config.<String>getVariable("evo_colorlimit", "9"));
        colorlimit.setColumns(6);
        top.add(colorlimit);

        top.add(new JLabel("Training set"));
        final JTextField training = new JTextField();
        training.setText(config.<String>getVariable("evo_trainingset", ""));
        training.setColumns(32);
        top.add(training);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                final Encoder enc = new WordEncoder("abc");
                try {
                    config.setVariable("evo_epicycles", epicycles.getText());
                    config.setVariable("evo_iterations", iterations.getText());
                    config.setVariable("evo_subiterations", subiterations.getText());
                    config.setVariable("evo_population", pop.getText());
                    config.setVariable("evo_birthrate", births.getText());
                    config.setVariable("evo_deathrate", deaths.getText());
                    config.setVariable("evo_colorlimit", colorlimit.getText());
                    config.setVariable("evo_trainingset", training.getText());
                    //final Initializer target = new ImageInitializer(new File(training.getText()));
                    //final Plane tplane = v.getActiveCA().size(config.getWidth(), config.getHeight()).initializer(target).createPlane();
                    final Evolver evolver = new EvolverBuilder()
                        .withEncoder(null)
                        //.withTraining(Training.file(training.getText(),
                            //(line)->{return new ByteInitializer(enc.encode(line));}
                        //))
                        //.withTraining(Training.of(new SingleInitializer()))
                        .withTraining(Training.of(new RandomInitializer(1) /*, new RandomInitializer(2), new RandomInitializer(3), new RandomInitializer(4)*/))
                        //.withFitness(FitnessCriteria.repeatGreatest())
                        //.withFitness(FitnessCriteria.findTarget(
                            //WordInitializer.encodeWord(WordInitializer.ALPAHBET, WordInitializer.TARGET)))
                        //.withFitness(FitnessCriteria.reverse(4, 4))
                        //.withFitness(FitnessCriteria.nothingLostNothingGained())
                        //.withFitness(FitnessCriteria.findTarget(tplane, new double[]{1}))
                        .withFitness(FitnessCriteria.interesting2())
                        .withMutationStrategy(new RetryingMutationStrategy(new RandomMutationStrategy(MutatorFactory.defaultMutators(), true), MutationStrategies.noise(), 4))
                        .withPopulation(Integer.parseInt(pop.getText()))
                        .withBirthRate(Float.parseFloat(births.getText()))
                        .withDeathRate(Float.parseFloat(deaths.getText()))
                        .build();
                    final int epi = Integer.parseInt(epicycles.getText());
                    final int it = Integer.parseInt(iterations.getText());
                    final int subit = Integer.parseInt(subiterations.getText());
                    final int colorLimit = Integer.parseInt(colorlimit.getText());
                    final JDialog gene = new JDialog(v, "Evolving");
                    final JLabel task = new JLabel("Building automata");
                    Font font = task.getFont();
                    task.setFont(font.deriveFont(font.getSize()-2f));
                    JPanel main = new JPanel(new BorderLayout());
                    main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    gene.add(main, BorderLayout.CENTER);
                    final JButton[] hack = new JButton[1];

                    Thread builder = new Thread() {
                        public void run() {
                            final CA evolved = evolver.run(v.getActiveCA().size(config.getWidth(), config.getHeight()),
                                    random,
                                    epi,
                                    it,
                                    subit,
                                    colorLimit,
                                    Executors.newFixedThreadPool(4));
                            SwingUtilities.invokeLater(()->{ v.setActiveCA(evolved); });
                            SwingUtilities.invokeLater(()->{ gene.dispose(); });
                        }
                    };
                    final JButton cancel = new JButton("Cancel");
                    cancel.addActionListener(new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            task.setText("Canceling");
                            cancel.setEnabled(false);
                            evolver.requestCancel();
                            //builder.interrupt();
                        }
                    });
                    hack[0] = cancel;
                    JPanel south = new JPanel(new BorderLayout());
                    south.add(cancel, BorderLayout.EAST);
                    main.add(south, BorderLayout.SOUTH);
                    main.add(task, BorderLayout.WEST);
                    Dimension di = main.getPreferredSize();
                    gene.setSize(100+di.width, 50+di.height);
                    Things.centerWindow(gene);
                    gene.setVisible(true);
                    builder.start();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
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

    public void resizeCA(NViewer v) {
        final Config config = v.getConfig();
        final JDialog d = new JDialog(v, "Size");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(5,2));

        top.add(new JLabel("Width"));
        final JTextField width = new JTextField();
        width.setText(""+config.getWidth());
        width.setColumns(6);
        JPanel wp = new JPanel();
        wp.add(width);
        wp.add(new JLabel("px"));
        top.add(wp);

        JPanel hp = new JPanel();
        top.add(new JLabel("Height"));
        final JTextField height = new JTextField();
        height.setText(""+config.getHeight());
        height.setColumns(6);
        hp.add(height);
        hp.add(new JLabel("px"));
        top.add(hp);

        top.add(new JLabel("Depth"));
        JPanel dp = new JPanel();
        final JTextField depth = new JTextField();
        depth.setText(""+config.getDepth());
        depth.setColumns(6);
        dp.add(depth);
        dp.add(new JLabel("px"));
        top.add(dp);

        top.add(new JLabel("Prelude"));
        JPanel pp = new JPanel();
        final JTextField prelude = new JTextField();
        prelude.setText(""+config.getPrelude());
        prelude.setColumns(6);
        pp.add(prelude);
        pp.add(new JLabel("steps"));
        top.add(pp);

        top.add(new JLabel("Update weight"));
        JPanel uw = new JPanel();
        final JTextField updateWeight = new JTextField();
        //updateWeight.setText(""+config.getFloatVariable("weight", 1f));
        updateWeight.setText(""+v.getActiveCA().getWeight());
        updateWeight.setColumns(6);
        uw.add(updateWeight);
        top.add(uw);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Reset");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                //di.setCASize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
                //config.setVariable("weight", Float.parseFloat(updateWeight.getText()));
                config.setSize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()), Integer.parseInt(depth.getText()), Integer.parseInt(prelude.getText()), Float.parseFloat(updateWeight.getText()));
                //generate(di);
            }
        });
        bot.add(ne);
        /*
        de.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                width.setText(""+(_width-32));
                height.setText(""+(_height-96));
                //d.dispose();
                //di.setCASize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
                //generate(di);
            }
        });
        bot.add(de);
        */
        p.add(bot, BorderLayout.SOUTH);
        d.getContentPane().add(p);
        Dimension dim = p.getPreferredSize();
        dim.height += 40;
        d.setSize(dim);
        Things.centerWindow(d);
        d.setVisible(true);
    }

    private Mutator _lastMutator;
    public void repeatLastMutation(NViewer v, Config config, Random rand) {
        mutate(v, config, rand, _lastMutator);
    }

    public void addRuleStage(final NViewer v, Config config, Random rand) {
        final CA ca = v.getActiveCA();
        final Rule r = ca.getRule();
        if(r instanceof Mutatable) {
            v.setActiveCA(ca.mutate((Rule)((Mutatable)r).mutate(createMutationFactor(config, rand).withMode("add")), rand));
            int nstage = 1+Integer.parseInt(config.getVariable("mutator_stage", "0"));
            config.setVariable("mutator_stage", ""+nstage);
        }
    }

    public void removeRuleStage(final NViewer v, Config config, Random rand) {
        final CA ca = v.getActiveCA();
        final Rule r = ca.getRule();
        if(r instanceof Mutatable) {
            v.setActiveCA(ca.mutate((Rule)((Mutatable)r).mutate(createMutationFactor(config, rand).withMode("remove")), rand));
            int stage = Integer.parseInt(config.getVariable("mutator_stage", "0"));
            config.setVariable("mutator_stage", ""+(stage-1));
        }
    }

    public void randomMutation(NViewer v) {
    }

    public void mutate(NViewer v, Config config, Random rand, Mutator m) {
        _lastMutator = m;
        if(v.getConfig().getForceSymmetry()) {
            m = Mutator.chain(m, new Symmetry());
        }
        final CA ca = v.getActiveCA();
        v.setActiveCA(new RuleTransform(rand, m, createMutationFactor(config, rand)).transform(ca));
    }

    public static MutationFactor createMutationFactor(Config config, Random r) {
        int mc = Integer.parseInt(config.getVariable("mutator_maxcolors", "9"));
        final String stage = config.getVariable("mutator_stage", "0");
        MutationFactor mf = MutationFactor.defaultFactor()
            .withAlpha(Integer.parseInt(config.getVariable("mutator_alpha", "20")))
            .withStage("*".equals(stage)?-1:Integer.parseInt(stage))
            .withRandom(r)
            .withTransition(Float.parseFloat(config.getVariable("mutator_transition", "0.5")))
            .withSymmetry("true".equals(config.getVariable("mutator_symmetry", "false")))
            .withValidator((a)->{
                return a.colors()<mc;
            });
        //System.err.println("####### stage: "+mf.stage());
        return mf;
    }

    public void zoomIn(NViewer v) {
        v.getConfig().setScale(v.getConfig().getScale()*1.5f);
    }

    public void zoomOut(NViewer v) {
        v.getConfig().setScale(v.getConfig().getScale()/1.5f);
    }

    public void zoomOne(NViewer v) {
        v.getConfig().setScale(1f);
    }

    public void view3d(NViewer v) {
        JfxWindow.setCA(v.getActiveCA());
        JfxWindow.launch(JfxWindow.class, new String[0]);
    }

    private void showError(NViewer v, String msg, Throwable e) {
        JDialog er = new JDialog(v.getRoot(), "Fuck.");
        er.getContentPane().add(new JLabel(msg));
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        JTextArea text = new JTextArea(sw.toString());
        er.getContentPane().add(text);
        er.setSize(300, 300);
        Things.centerWindow(er);
        er.setVisible(true);
    }

    private static int pair(int count, java.awt.Container c, String field, Object value) {
        JLabel l1 = new JLabel(field);
        JLabel l2 = new JLabel(value.toString());
        c.add(l1);
        c.add(l2);
        return ++count;
    }

    private static String chop(String s, int max) {
        return (s.length()>max?s.substring(0,max)+"...":s)+" ("+s.length()+" characters)";
    }

    private static JComponent createRuleText(String str) {
        return createRuleText(str, true);
    }

    private static JComponent createRuleText(String str, boolean scroll) {
        JTextArea a = new JTextArea(str, Math.max(1,Math.min(7,str.length()/80)), 80);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(false);
        Font f = a.getFont();
        a.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
        return scroll ? new JScrollPane(a) : a;
    }

    private static JComponent createColorPanel(Palette palette) {
        JPanel colors = new JPanel();
        colors.setAlignmentY(0);
        colors.add(new JLabel(""+palette.getColorCount()+": "));
        for(int col:palette.getColors()) {
            colors.add(new CAEditor.Cell(col));
            colors.add(new JLabel(Colors.toColorString(col)));
        }
        return colors;
    }
}
