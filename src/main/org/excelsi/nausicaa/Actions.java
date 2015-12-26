package org.excelsi.nausicaa;


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
import org.excelsi.nausicaa.ca.WordInitializer;
import org.excelsi.nausicaa.ca.ImageInitializer;

import java.math.BigInteger;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Random;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


public class Actions {
    public void newCA(NViewer v) {
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
                v.setActiveCA(CA.fromFile(f.getSelectedFile().toString()));
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
                ca.save(f.getSelectedFile().toString());
            }
            catch(IOException e) {
                showError(v, "Failed to save "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void close(NViewer v) {
    }

    public void export(NViewer v, Config config) {
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

    public void info(NViewer v) {
        final CA ca = v.getActiveCA();

        Rule r = ca.getRule();
        final JFrame i = new JFrame("Info");
        InfoPanel p = new InfoPanel();
        //p.addPair("Universe", chop(r.humanize(),80));
        final String b64 = ca.toBase64();
        p.addPair("Universe", chop(b64,80));
        p.addPair("Text", createRuleText(b64));
        String id = r.id();
        if(id.length()<1000000) {
            BigInteger rval = new BigInteger(r.id(), r.colorCount());
            final String frval = rval.toString(10);
            p.addPair("Rval ("+frval.length()+" digits)", createRuleText(frval));
        }
        else {
            p.addPair("Rval", id.length()+" digits elided");
        }
        if(b64.length()<100000) {
            p.addPair("Incantation", createRuleText(ca.toIncantation()));
        }
        p.addPair("Colors", createColorPanel(ca.getPalette()));
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

    public void generateToFile(NViewer v) {
        new JCAGenerator(v, v.getActiveCA(), v.getConfig());
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
        JPanel top = new JPanel(new GridLayout(5,2));

        top.add(new JLabel("Iterations"));
        final JTextField iterations = new JTextField();
        iterations.setText("100");
        iterations.setColumns(6);
        top.add(iterations);

        top.add(new JLabel("Population"));
        final JTextField pop = new JTextField();
        pop.setText("10");
        pop.setColumns(6);
        top.add(pop);

        top.add(new JLabel("Birth rate"));
        final JTextField births = new JTextField();
        births.setText("0.1");
        births.setColumns(6);
        top.add(births);

        top.add(new JLabel("Death rate"));
        final JTextField deaths = new JTextField();
        deaths.setText("0.1");
        deaths.setColumns(6);
        top.add(deaths);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                final Evolver evolver = new EvolverBuilder()
                    .withRandom(random)
                    .withInitializer(new RandomInitializer())
                    .withEncoder(null)
                    //.withFitness(FitnessCriteria.repeatGreatest())
                    .withFitness(FitnessCriteria.findTarget(
                        WordInitializer.encodeWord(WordInitializer.ALPAHBET, WordInitializer.TARGET)))
                    .withMutationStrategy(new RandomMutationStrategy(MutatorFactory.defaultMutators()))
                    .withPopulation(Integer.parseInt(pop.getText()))
                    .withBirthRate(Float.parseFloat(births.getText()))
                    .withDeathRate(Float.parseFloat(deaths.getText()))
                    .build();
                int it = Integer.parseInt(iterations.getText());
                final CA evolved = evolver.run(v.getActiveCA(), it);
                v.setActiveCA(evolved);
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
        JPanel top = new JPanel(new GridLayout(3,2));

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

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Reset");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                //di.setCASize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
                config.setSize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()), Integer.parseInt(depth.getText()));
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
    public void repeatLastMutation(NViewer v, Random rand) {
        mutate(v, rand, _lastMutator);
    }

    public void randomMutation(NViewer v) {
    }

    public void mutate(NViewer v, Random rand, Mutator m) {
        _lastMutator = m;
        if(v.getConfig().getForceSymmetry()) {
            m = Mutator.chain(m, new Symmetry());
        }
        final CA ca = v.getActiveCA();
        v.setActiveCA(new RuleTransform(rand, m).transform(ca));
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
        JTextArea a = new JTextArea(str, Math.max(1,Math.min(7,str.length()/80)), 80);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(false);
        Font f = a.getFont();
        a.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
        return new JScrollPane(a);
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
