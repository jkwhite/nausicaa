package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.Archetype;
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
import org.excelsi.nausicaa.ca.WordEncoder;
import org.excelsi.nausicaa.ca.ByteInitializer;
import org.excelsi.nausicaa.ca.WordInitializer;
import org.excelsi.nausicaa.ca.ImageInitializer;
import org.excelsi.nausicaa.ca.SingleInitializer;
import org.excelsi.nausicaa.ca.Initializer;
import org.excelsi.nausicaa.ca.Training;
import org.excelsi.nausicaa.ca.RetryingMutationStrategy;
import org.excelsi.nausicaa.ca.MutationStrategies;
import org.excelsi.nausicaa.ca.MutationFactor;
import org.excelsi.nausicaa.ca.Stats;
import org.excelsi.nausicaa.ca.Multistats;
import org.excelsi.nausicaa.ca.Pools;
import org.excelsi.nausicaa.ca.Ruleset;
import org.excelsi.nausicaa.ca.ComputedRuleset;
import org.excelsi.nausicaa.ca.Initializers;

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
                CA ca = new CA(rule, pal, v.getActiveCA().getInitializer(), rand, 0, v.getConfig().getWidth(), v.getConfig().getHeight(), v.getConfig().getDepth(), v.getConfig().getPrelude());
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

    public void info(NViewer v) {
        final CA ca = v.getActiveCA();
        final Plane plane = v.getPlaneDisplayProvider().getActivePlane();
        final Plane nextPlane = ca.getRule().frameIterator(plane, Pools.bgr(), false, 1).next();
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
            _a = new Animation(v.getConfig(), v.getPlaneDisplayProvider(), timeline, frames);
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

    public void generateToFile(NViewer v) {
        new JCAGenerator(v, v.getActiveCA(), v.getConfig());
    }

    public void mutationParams(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Mutation parameters");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(2,2));

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
        final JFileChooser f = new JFileChooser(config.getDir());
        f.setDialogTitle("Initial state image");
        f.setDialogType(f.OPEN_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showOpenDialog(v);
        if(ret==f.APPROVE_OPTION) {
            File img = f.getSelectedFile();
            config.setDir(img.getParent());
            try {
                BufferedImage initImage = ImageIO.read(img);
                System.err.println("read image "+img);
                v.setInitializer(new ImageInitializer(initImage));
                //SwingUtilities.invokeLater(new Runnable() {
                    //public void run() {
                        //generate();
                    //}
                //});
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
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
        JPanel top = new JPanel(new GridLayout(4,2));

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

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Reset");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                //di.setCASize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
                config.setSize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()), Integer.parseInt(depth.getText()), Integer.parseInt(prelude.getText()));
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

    public void randomMutation(NViewer v) {
    }

    public void mutate(NViewer v, Config config, Random rand, Mutator m) {
        _lastMutator = m;
        if(v.getConfig().getForceSymmetry()) {
            m = Mutator.chain(m, new Symmetry());
        }
        final CA ca = v.getActiveCA();
        v.setActiveCA(new RuleTransform(rand, m, createMutationFactor(config)).transform(ca));
    }

    public static MutationFactor createMutationFactor(Config config) {
        int mc = Integer.parseInt(config.getVariable("mutator_maxcolors", "9"));
        return MutationFactor.defaultFactor()
            .withAlpha(Integer.parseInt(config.getVariable("mutator_alpha", "20")))
            .withValidator((a)->{
                return a.colors()<mc;
            });
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
