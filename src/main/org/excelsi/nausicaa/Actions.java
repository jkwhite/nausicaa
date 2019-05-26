package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;

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
import java.util.EnumSet;
import java.util.Random;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class Actions {
    public void newCA(NViewer v) {
        final JDialog d = new JDialog(v, "New automata");
        final Config config = v.getConfig();

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(8,2));

        top.add(new JLabel("Dimensions"));
        final JTextField alpha = new JTextField();
        alpha.setText(config.getVariable("default_dimensions", "2"));
        alpha.setColumns(3);
        top.add(alpha);

        top.add(new JLabel("Size"));
        final JTextField siz = new JTextField();
        siz.setText(config.getVariable("default_size", "1"));
        siz.setColumns(3);
        top.add(siz);

        // Compute
        final String[] comphack = new String[1];
        top.add(new JLabel("Compute"));
        ButtonGroup comp = new ButtonGroup();

        AbstractAction comparr = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0] = "array";
            }
        };
        JRadioButton rcomparr = new JRadioButton(comparr);
        rcomparr.setText("Full Target");
        comp.add(rcomparr);

        AbstractAction compspars = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0] = "sparse";
            }
        };
        JRadioButton rspars = new JRadioButton(compspars);
        rspars.setText("Sparse Target");
        comp.add(rspars);

        AbstractAction compelf = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0] = "machineelf";
            }
        };
        JRadioButton relf = new JRadioButton(compelf);
        relf.setText("Machine Elf");
        comp.add(relf);

        JPanel comps = new JPanel();
        comps.add(rcomparr);
        comps.add(rspars);
        comps.add(relf);
        relf.setSelected(true);
        comphack[0] = "machineelf";
        top.add(comps);

        top.add(new JLabel("Language"));
        final JComboBox lang = new JComboBox(Languages.catalog());
        lang.setSelectedItem(config.getVariable("default_language", "Universal"));
        top.add(lang);

        // Neighborhood
        final Archetype.Neighborhood[] neihack = new Archetype.Neighborhood[1];
        top.add(new JLabel("Neighborhood"));
        ButtonGroup nei = new ButtonGroup();

        /*
        AbstractAction von = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                neihack[0] = Archetype.Neighborhood.vonneumann;
            }
        };
        JRadioButton rvon = new JRadioButton(von);
        rvon.setText("von Neumann");
        nei.add(rvon);

        AbstractAction moo = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                neihack[0] = Archetype.Neighborhood.moore;
            }
        };
        JRadioButton rmoo = new JRadioButton(moo);
        rmoo.setText("Moore");
        nei.add(rmoo);

        JPanel neis = new JPanel();
        neis.add(rvon);
        neis.add(rmoo);
        rmoo.setSelected(true);
        neihack[0] = Archetype.Neighborhood.moore;
        */
        JPanel neis = new JPanel();
        String defNei = config.getVariable("default_neighborhood", Archetype.Neighborhood.moore.name());
        for(Archetype.Neighborhood neighbor:EnumSet.allOf(Archetype.Neighborhood.class)) {
            AbstractAction von = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    neihack[0] = neighbor;
                }
            };
            JRadioButton rvon = new JRadioButton(von);
            rvon.setText(neighbor.getName());
            nei.add(rvon);
            if(neighbor.name().equals(defNei)) {
                rvon.setSelected(true);
                neihack[0] = neighbor;
            }
            neis.add(rvon);
        }

        top.add(neis);

        // Color kind
        final String[] colhack = new String[1];
        final JComponent[] idxhack = new JComponent[4];
        top.add(new JLabel("Kind"));
        ButtonGroup kind = new ButtonGroup();

        AbstractAction real = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                colhack[0] = "real";
                idxhack[0].setEnabled(true);
                idxhack[1].setEnabled(true);
                idxhack[2].setEnabled(true);
                idxhack[3].setEnabled(true);
            }
        };
        JRadioButton rreal = new JRadioButton(real);
        rreal.setText("Real");
        kind.add(rreal);

        AbstractAction rgb = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                colhack[0] = "rgb";
                idxhack[0].setEnabled(false);
                idxhack[1].setEnabled(false);
                idxhack[2].setEnabled(false);
                idxhack[3].setEnabled(false);
            }
        };
        JRadioButton rrgb = new JRadioButton(rgb);
        rrgb.setText("RGB");
        kind.add(rrgb);

        AbstractAction rgba = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                colhack[0] = "rgba";
                idxhack[0].setEnabled(false);
                idxhack[1].setEnabled(false);
                idxhack[2].setEnabled(false);
                idxhack[3].setEnabled(false);
            }
        };
        JRadioButton rrgba = new JRadioButton(rgb);
        rrgba.setText("RGBA");
        kind.add(rrgba);

        AbstractAction indexed = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                colhack[0] = "indexed";
                idxhack[0].setEnabled(true);
                idxhack[1].setEnabled(true);
                idxhack[2].setEnabled(false);
                idxhack[3].setEnabled(false);
            }
        };
        JRadioButton rindexed = new JRadioButton(indexed);
        rindexed.setText("Indexed");
        rindexed.setSelected(true);
        kind.add(rindexed);

        colhack[0] = config.getVariable("default_kind", "indexed");
        switch(colhack[0]) {
            case "indexed":
                rindexed.setSelected(true);
                break;
            case "real":
                rreal.setSelected(true);
                break;
            case "rgb":
                rrgb.setSelected(true);
                break;
            case "rgba":
                rrgba.setSelected(true);
                break;
        }

        JPanel kinds = new JPanel();
        kinds.add(rindexed);
        kinds.add(rrgb);
        kinds.add(rrgba);
        kinds.add(rreal);
        top.add(kinds);

        idxhack[0] = new JLabel("Value Colors");
        top.add(idxhack[0]);
        final JTextField mc = new JTextField();
        idxhack[1] = mc;
        mc.setText(config.getVariable("default_colors", "2"));
        mc.setColumns(3);
        top.add(mc);

        idxhack[2] = new JLabel("Palette Colors");
        top.add(idxhack[2]);
        final JTextField pmc = new JTextField();
        idxhack[3] = pmc;
        pmc.setText(config.getVariable("default_palettecolors", "2"));
        pmc.setColumns(3);
        top.add(pmc);

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
                Integer pcolors = Integer.parseInt(pmc.getText());
                Integer size = Integer.parseInt(siz.getText());
                String lng = lang.getSelectedItem().toString();
                config.setVariable("default_dimensions", alpha.getText());
                config.setVariable("default_colors", mc.getText());
                config.setVariable("default_palettecolors", pmc.getText());
                config.setVariable("default_size", siz.getText());
                config.setVariable("default_kind", colhack[0]);
                config.setVariable("default_language", lng);
                config.setVariable("default_neighborhood", neihack[0].name());
                Random rand = new Random();
                Palette pal;
                boolean usePalColors;
                switch(colhack[0]) {
                    case "rgb":
                        pal = new RGBPalette();
                        usePalColors = true;
                        break;
                    case "rgba":
                        pal = new RGBAPalette();
                        usePalColors = true;
                        break;
                    case "real":
                        pal = Palette.random(pcolors, rand, true);
                        usePalColors = false;
                        break;
                    case "indexed":
                    default:
                        pal = Palette.random(colors, rand, true);
                        usePalColors = true;
                        break;
                }
                Archetype a = new Archetype(dims, size, usePalColors?pal.getColorCount():colors, neihack[0], "real".equals(colhack[0])?Values.continuous:Values.discrete);
                Ruleset rs;
                switch(comphack[0]) {
                    case "machineelf":
                    default:
                        rs = new ComputedRuleset(a, Languages.named(lng));
                        break;
                    case "sparse":
                    case "array":
                        rs = dims==1?new IndexedRuleset1d(a):new IndexedRuleset2d(a);
                        break;
                }
                Rule rule = rs.random(rand).next();
                CA ca = new CA(rule, pal, v.getActiveCA().getInitializer(), rand, 0,
                        v.getConfig().getWidth(),
                        v.getConfig().getHeight(),
                        v.getConfig().getDepth(),
                        v.getConfig().getPrelude(),
                        v.getConfig().getWeight(),
                        0,
                        ComputeMode.combined,
                        MetaMode.depth,
                        new UpdateMode.SimpleSynchronous(),
                        EdgeMode.defaultMode(),
                        ExternalForce.nop(),
                        new Varmap(),
                        null);
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

    public void newCAImageRGB(NViewer v, Config config) {
        newCAImage(v, config, "rgb", config.getVariable("default_language", "Universal"));
    }

    public void newCAImageRGBA(NViewer v, Config config) {
        newCAImage(v, config, "rgba", config.getVariable("default_language", "Universal"));
    }

    public void newCAImageIndexed(NViewer v, Config config) {
        newCAImage(v, config, "indexed", config.getVariable("default_language", "Universal"));
    }

    public void newCAImageCont(NViewer v, Config config) {
        newCAImage(v, config, "continuous", config.getVariable("default_language", "Universal"));
    }

    public void newCAImageContChan(NViewer v, Config config) {
        newCAImage(v, config, "continuous-channels", config.getVariable("default_language", "Universal"));
    }

    private void newCAImage(NViewer v, Config config, String paletteMode, String lang) {
        JFileChooser f = new JFileChooser(config.getImgDir());
        f.setDialogTitle("New CA from image");
        f.setDialogType(f.OPEN_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showOpenDialog(v.getRoot());
        if(ret==f.APPROVE_OPTION) {
            try {
                config.setImgDir(f.getSelectedFile().getParent());
                final CA ca = CA.fromImage(f.getSelectedFile().toString(), paletteMode, lang);
                //config.setSize(ca.getWidth(), ca.getHeight(), ca.getDepth());
                v.setActiveCA(ca);
                config.setSize(ca.getWidth(), ca.getHeight(), ca.getDepth(), ca.getPrelude());
            }
            catch(IOException e) {
                showError(v, "Failed to load "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void load(NViewer v, Config config) {
        JFileChooser f = new JFileChooser(config.getSaveDir());
        f.setDialogTitle("Open automata");
        f.setDialogType(f.OPEN_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showOpenDialog(v.getRoot());
        if(ret==f.APPROVE_OPTION) {
            try {
                config.setSaveDir(f.getSelectedFile().getParent());
                final CA ca = CA.fromFile(f.getSelectedFile().toString(), "text");
                config.setSize(ca.getWidth(), ca.getHeight(), ca.getDepth());
                config.setWeight(ca.getWeight());
                v.setActiveCA(ca);
            }
            catch(IOException e) {
                showError(v, "Failed to load "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void save(NViewer v, Config config) {
        JFileChooser f = new JFileChooser(config.getSaveDir());
        f.setDialogTitle("Save automata");
        f.setDialogType(f.SAVE_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showSaveDialog(v.getRoot());
        CA ca = v.getActiveCA();
        if(ret==f.APPROVE_OPTION) {
            try {
                config.setSaveDir(f.getSelectedFile().getParent());
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
        JFileChooser f = new JFileChooser(config.getImgDir());
        f.setDialogTitle("Export image");
        f.setDialogType(f.SAVE_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showSaveDialog(v.getRoot());
        CA ca = v.getActiveCA();
        Plane plane = v.getPlaneDisplayProvider().getActivePlane();
        if(ret==f.APPROVE_OPTION) {
            try {
                config.setImgDir(f.getSelectedFile().getParent());
                plane.save(f.getSelectedFile().toString(), v.getPlaneDisplayProvider().getActivePlaneDisplay().getRendering());
            }
            catch(IOException e) {
                showError(v, "Failed to save "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    public void exportGenerated(NViewer v, Config config) {
        JFileChooser f = new JFileChooser(config.getSaveDir());
        f.setDialogTitle("Export Generated Data");
        f.setDialogType(f.SAVE_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showSaveDialog(v.getRoot());
        Plane plane = v.getPlaneDisplayProvider().getActivePlane();
        if(ret==f.APPROVE_OPTION) {
            try(PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(f.getSelectedFile().toString())))) {
                config.setSaveDir(f.getSelectedFile().getParent());
                plane.export(w);
            }
            catch(IOException e) {
                showError(v, "Failed to save "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }

    /*
    public void exportRule(NViewer v, Config config) { JFileChooser f = new JFileChooser(config.getDir());
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
    */

    public void debug(NViewer v) {
        final Plane p = v.getPlaneDisplayProvider().getActivePlane();
        final java.util.List<Blobs.Blob> blobs = new Blobs().blobs((IntPlane)p, Blobs.Mode.finite);
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

    private final String CONSOLE_PROGRAM = System.getProperty("app.root")+"/bin/nausicaa";
    public void openConsole(NViewer v, Config c) {
        final CA ca = v.getActiveCA();
        File f = null;
        try {
            f = File.createTempFile("ca_", ".ca");
            ca.save(f.toString(), "text");
            final ProcessBuilder b = new ProcessBuilder(CONSOLE_PROGRAM, "-cli" /*, f.toString()*/);
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

        Rule r = ca.getRule();
        final JFrame i = new JFrame("Info");
        InfoPanel p = new InfoPanel();
        if(r instanceof IndexedRule) {
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
            //if(b64.length()<100000) {
                //p.addPair("Incantation", createRuleText(ca.toIncantation()));
            //}
            if(ca.archetype().dims()>1) {
                final Plane plane = v.getPlaneDisplayProvider().getActivePlane();
                final Plane nextPlane = ca.getRule().frameIterator(plane, Pools.adhoc(), new GOptions(false, 1, 1, 1f)).next();
                final Stats stats = Stats.forPlane(plane);
                final Stats nextStats = Stats.forPlane(nextPlane);
                final Multistats ms = stats.compareWith(nextStats);
                p.addPair("Stats", createRuleText(ms.humanize(), false));
            }
        }
        else if(r instanceof ComputedRule2d) {
            ComputedRule2d cr = (ComputedRule2d) r;
            p.addPair("Dimensions", r.archetype().dims());
            p.addPair("Colors", r.archetype().colors());
            p.addPair("Neighborhood", r.archetype().neighborhood());
            p.addPair("Size", r.archetype().size());
            p.addPair("Values", r.archetype().values());
            p.addPair("Initializer", ca.getInitializer().humanize());
            p.addPair("Update", ca.getUpdateMode().humanize());
            p.addPair("Edge", ca.getEdgeMode().humanize());
            p.addPair("External Force", ca.getExternalForce().humanize());
            p.addPair("Language", ((AbstractComputedRuleset)cr.origin()).language().name());
            p.addPair("Genome", cr.prettyGenome());
            p.addPair("Codons",
                createText(new GenomeParser(r.archetype(), ((ComputedRuleset)r.origin()).language()).info(cr.archetype(), cr.genome()).toString(), 10, true));
        }
        p.addPair("Colors", createColorPanel(ca.getPalette()));
        p.addPair("Caption", createText(createCaption(ca), 10, true));
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
        if(_a!=null&&_a.isAlive()) {
            _a.stopAnimation();
            _a = null;
        }
        else {
            _a = new Animation(v.getConfig(), v.getPlanescapeProvider(), timeline, frames);
            _a.start();
        }
    }

    public void chooseAnimFrames(final NViewer v, final Timeline timeline) {
        final JDialog d = new JDialog(v, "Animation configuration");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(1,2));

        top.add(new JLabel("Number of frames"));
        final JTextField fr = new JTextField();
        fr.setText(v.getConfig().getVariable("animation_nframes", "100"));
        fr.setColumns(5);
        top.add(fr);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                int frames = Integer.parseInt(fr.getText());
                v.getConfig().setVariable("animation_nframes", fr.getText());
                animate(v, timeline, frames);
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
        JPanel top = new JPanel(new GridLayout(7,2));

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

        top.add(new JLabel("Recurse to meta"));
        final JCheckBox meta = new JCheckBox();
        meta.setSelected("true".equals(config.getVariable("mutator_meta", "false")));
        top.add(meta);

        top.add(new JLabel("Never repeat"));
        final JCheckBox rp = new JCheckBox();
        rp.setSelected("true".equals(config.getVariable("mutator_neverrepeat", "false")));
        top.add(rp);

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
                config.setVariable("mutator_meta", ""+meta.isSelected());
                config.setVariable("mutator_neverrepeat", ""+rp.isSelected());
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

    public void chooseRandomExternalForce(final NViewer v) {
        final JDialog d = new JDialog(v, "Random External Force");
        final Config config = v.getConfig();
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(1,2));

        top.add(new JLabel("Amount"));
        final JTextField value = new JTextField();
        value.setText(config.getVariable("externalforce_random_amount", "0.01"));
        value.setColumns(10);
        top.add(value);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("externalforce_random_amount", value.getText());
                v.setActiveCA(v.getActiveCA().externalForce(new ExternalForce.RandomExternalForce(Float.parseFloat(value.getText()))));
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

    public void chooseConstantEdgeMode(final NViewer v) {
        final JDialog d = new JDialog(v, "Constant Edge Mode");
        final Config config = v.getConfig();
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(1,2));

        top.add(new JLabel("Color"));
        final JTextField value = new JTextField();
        value.setText(config.getVariable("edgemode_constant", "0"));
        value.setColumns(10);
        top.add(value);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("edgemode_constant", value.getText());
                v.setActiveCA(v.getActiveCA().edgeMode(new EdgeMode(EdgeMode.Type.constant, Integer.parseInt(value.getText()))));
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

    public void chooseAsynchronousEnergyUpdate(final NViewer v) {
        final JDialog d = new JDialog(v, "Asynchronous Energy Update");
        final Config config = v.getConfig();
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(2,2));

        top.add(new JLabel("Chance Coefficient"));
        final JTextField chance = new JTextField();
        chance.setText(config.getVariable("energyasynchronous_chance", "1.0"));
        chance.setColumns(10);
        top.add(chance);

        top.add(new JLabel("Size"));
        final JTextField size = new JTextField();
        size.setText(config.getVariable("energyasynchronous_size", "1"));
        size.setColumns(10);
        top.add(size);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("energyasynchronous_chance", chance.getText());
                config.setVariable("energyasynchronous_size", size.getText());
                v.setActiveCA(v.getActiveCA().updateMode(
                    UpdateMode.create("energy",
                        Float.parseFloat(chance.getText()),
                        Integer.parseInt(size.getText())
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

    public void chooseAsynchronousUpdate(final NViewer v) {
        final JDialog d = new JDialog(v, "Asynchronous Update");
        final Config config = v.getConfig();
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(1,2));

        top.add(new JLabel("Chance"));
        final JTextField chance = new JTextField();
        chance.setText(config.getVariable("asynchronous_chance", "0.5"));
        chance.setColumns(10);
        top.add(chance);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("asynchronous_chance", chance.getText());
                v.setActiveCA(v.getActiveCA().updateMode(UpdateMode.create("async", Float.parseFloat(chance.getText()), 0)));
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

    public void chooseVariableUpdate(final NViewer v) {
        final JDialog d = new JDialog(v, "Variable Update");
        final Config config = v.getConfig();
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(1,2));

        top.add(new JLabel("Chance"));
        final JTextField chance = new JTextField();
        chance.setText(config.getVariable("variable_chance", "0.5"));
        chance.setColumns(10);
        top.add(chance);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("variable_chance", chance.getText());
                v.setActiveCA(v.getActiveCA().updateMode(UpdateMode.create("variable", Float.parseFloat(chance.getText()), 0)));
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

    public void chooseAsynchronousLocalUpdate(final NViewer v) {
        final JDialog d = new JDialog(v, "Local Asynchronous Update");
        final Config config = v.getConfig();
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(1,2));

        top.add(new JLabel("Chance Coefficient"));
        final JTextField chance = new JTextField();
        chance.setText(config.getVariable("localasynchronous_chance", "0.5"));
        chance.setColumns(10);
        top.add(chance);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("localasynchronous_chance", chance.getText());
                v.setActiveCA(v.getActiveCA().updateMode(UpdateMode.create("localasync", Float.parseFloat(chance.getText()), 0)));
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

    public void chooseSingle(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Fixed initializer");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(5,2));

        top.add(new JLabel("Color"));
        final JTextField color = new JTextField();
        color.setText(config.getVariable("single_color", "-1"));
        color.setColumns(10);
        top.add(color);

        top.add(new JLabel("X"));
        final JTextField cx = new JTextField();
        cx.setText(config.getVariable("single_x", "-1"));
        cx.setColumns(10);
        top.add(cx);

        top.add(new JLabel("Y"));
        final JTextField cy = new JTextField();
        cy.setText(config.getVariable("single_y", "-1"));
        cy.setColumns(10);
        top.add(cy);

        top.add(new JLabel("Z"));
        final JTextField cz = new JTextField();
        cz.setText(config.getVariable("single_z", "-1"));
        cz.setColumns(10);
        top.add(cz);

        top.add(new JLabel("Size"));
        final JTextField sz = new JTextField();
        sz.setText(config.getVariable("single_size", "1"));
        sz.setColumns(10);
        top.add(sz);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("single_color", color.getText());
                config.setVariable("single_x", cx.getText());
                config.setVariable("single_y", cy.getText());
                config.setVariable("single_z", cz.getText());
                config.setVariable("single_size", sz.getText());
                v.setInitializer(new SingleInitializer(
                    Float.parseFloat(color.getText()),
                    Integer.parseInt(cx.getText()),
                    Integer.parseInt(cy.getText()),
                    Integer.parseInt(cz.getText()),
                    Integer.parseInt(sz.getText())
                ));
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

    public void chooseCustomInitializer(final NViewer v, Config config) {
        final JDialog d = new JDialog(v, "Custom initializer");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //JPanel top = new JPanel(new GridLayout(2,2));
        JPanel top = new JPanel();

        //top.add(new JLabel("Variables"));
        top.add(new JLabel("a: archetype; i: image; r: random"));
        //top.add(new JLabel("Rule"));
        final JTextArea rule = new JTextArea(10, 40);
        String ruleText = config.getVariable("custom_rule", "");
        final Initializer in = v.getActiveCA().getInitializer();
        if(in instanceof CustomInitializer) {
            ruleText = ((CustomInitializer)in).getText();
        }
        rule.setText(ruleText);
        top.add(new JScrollPane(rule));

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                config.setVariable("custom_rule", rule.getText());
                v.setInitializer(new CustomInitializer(rule.getText()));
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
                final JFileChooser f = new JFileChooser(config.getImgDir());
                f.setDialogTitle("Initial state image");
                f.setDialogType(f.OPEN_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showOpenDialog(v);
                if(ret==f.APPROVE_OPTION) {
                    File img = f.getSelectedFile();
                    config.setImgDir(img.getParent());
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
                final JFileChooser f = new JFileChooser(config.getSaveDir());
                f.setDialogTitle("Automata");
                f.setDialogType(f.OPEN_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showOpenDialog(v);
                if(ret==f.APPROVE_OPTION) {
                    File ca = f.getSelectedFile();
                    config.setSaveDir(ca.getParent());
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
        final JFileChooser f = new JFileChooser(config.getImgDir());
        f.setDialogTitle("Spectrum source image");
        f.setDialogType(f.OPEN_DIALOG);
        f.setMultiSelectionEnabled(false);
        int ret = f.showOpenDialog(v);
        if(ret==f.APPROVE_OPTION) {
            File img = f.getSelectedFile();
            config.setImgDir(img.getParent());
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
                v.setActiveCA(v.getActiveCA().palette(Palette.randomCutRainbow(v.getRandom(), v.getActiveCA().getPalette().getColorCount(), de, blz, clrs, inv)));
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

    private Thread _automutate;
    public void automutate(NViewer v) {
        if(_automutate==null) {
            _automutate = new Thread() {
                @Override public void run() {
                    while(!Thread.currentThread().isInterrupted()) {
                        v.pickRandom();
                        try {
                            Thread.sleep(30000);
                        }
                        catch(InterruptedException e) {
                            break;
                        }
                    }
                }
            };
            _automutate.start();
        }
        else {
            _automutate.interrupt();
            _automutate = null;
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

    public void invokeFunction(final NViewer v, final Functions.CAFunction fn) {
        final CA ca = v.getActiveCA();
        final String[] args = fn.buildArgs(ca);
        final Varmap vm = new Varmap(args);
        final Varmap prevars = v.getConfig().getFunctionArgs(fn.getName());
        if(prevars!=null) {
            for(String arg:args) {
                vm.put(arg, prevars.get(arg));
            }
        }

        final JDialog d = new JDialog(v, "Run "+fn.getName());
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel();

        final VarPanel vp = new VarPanel(vm);
        top.add(vp);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Run");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                Varmap vn = vp.commit();
                v.getConfig().setFunctionArgs(fn.getName(), vn);
                final MutationFactor mf = createMutationFactor(ca, v.getConfig(), v.getRandom());
                int ccores = v.getConfig().getIntVariable("animation_computeCores", 2);
                final ExecutorService pool = Pools.named("compute", ccores);
                final GOptions opt = new GOptions(true, ccores, 0, 1f)
                        .computeMode(ComputeMode.from(v.getConfig().<String>getVariable("rgb_computemode","combined")))
                        .metaMode(ca.getMetaMode())
                        ;
                final Rendering rend = v.getPlaneDisplayProvider().getActivePlaneDisplay().getRendering();
                new FunctionRunner(v, ca, mf, pool, opt, rend, vn, fn);
            }
        });
        de.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
            }
        });
        bot.add(ne);
        bot.add(de);
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
                config.setSize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()), Integer.parseInt(depth.getText()), Integer.parseInt(prelude.getText()), Float.parseFloat(updateWeight.getText()));
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

    public void configureVariables(NViewer v) {
        final Config config = v.getConfig();
        final JDialog d = new JDialog(v, "Variables");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel();

        final VarPanel vp = new VarPanel(v.getActiveCA().getVars());
        top.add(vp);

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Cancel");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                Varmap vn = vp.commit();
                CA ca = v.getActiveCA().vars(vn);
                Rule r = ca.getRule();
                if(r instanceof Genomic) {
                    ca = ca.mutate(r.origin().create(((Genomic)r).genome(),
                        createMutationFactor(ca, config, v.getRandom())), ca.getRandom());
                }
                v.setActiveCA(ca);
            }
        });
        de.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
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

    private GenomeMutator _lastMutator;
    public void repeatLastMutation(NViewer v, Config config, Random rand) {
        mutate(v, config, rand, _lastMutator);
    }

    public void addRuleStage(final NViewer v, Config config, Random rand) {
        final CA ca = v.getActiveCA();
        final Rule r = ca.getRule();
        if(r instanceof Mutatable) {
            v.setActiveCA(ca.mutate((Rule)((Mutatable)r).mutate(createMutationFactor(ca, config, rand).withMode("add")), rand));
            int nstage = 1+Integer.parseInt(config.getVariable("mutator_stage", "0"));
            config.setVariable("mutator_stage", ""+nstage);
        }
    }

    public void addDataStage(final NViewer v, Config config, Random rand) {
        final CA ca = v.getActiveCA();
        final Rule r = ca.getRule();
        if(r instanceof Mutatable) {
            v.setActiveCA(ca.mutate((Rule)((Mutatable)r).mutate(createMutationFactor(ca, config, rand).withMode("add_data")), rand));
            int nstage = 1+Integer.parseInt(config.getVariable("mutator_stage", "0"));
            config.setVariable("mutator_stage", ""+nstage);
        }
    }

    public void removeRuleStage(final NViewer v, Config config, Random rand) {
        final CA ca = v.getActiveCA();
        final Rule r = ca.getRule();
        if(r instanceof Mutatable) {
            v.setActiveCA(ca.mutate((Rule)((Mutatable)r).mutate(createMutationFactor(ca, config, rand).withMode("remove")), rand));
            int stage = Integer.parseInt(config.getVariable("mutator_stage", "0"));
            config.setVariable("mutator_stage", ""+(stage-1));
        }
    }

    public void pushMeta(final NViewer v, Config config) {
        CA ca = v.getActiveCA();
        Rule nr = ca.getRule().origin().random(v.getRandom()).next();
        CA nca = ca.mutate(nr, new Random()).meta(ca);
        v.setActiveCA(nca);
    }

    public void popMeta(final NViewer v, Config config) {
        CA ca = v.getActiveCA();
        if(ca.getMeta()!=null) {
            v.setActiveCA(ca.getMeta());
        }
        else {
            System.err.println("**** already at root ****");
        }
    }

    public void rotateMeta(final NViewer v, Config config) {
        CA ca = v.getActiveCA();
        CA root = ca.getMeta();
        if(root==null) {
            System.err.println("**** there is only one ****");
        }
        else {
            CA nca = root;
            while(root.getMeta()!=null) {
                root = root.getMeta();
            }
            nca = nca.meta(ca.meta(null));
            v.setActiveCA(nca);
        }
    }

    public void randomMutation(NViewer v) {
    }

    public void mutate(NViewer v, Config config, Random rand, GenomeMutator m) {
        final CA ca = v.getActiveCA();
        v.setActiveCA(new RuleTransform(rand,
            null,
            createMutationFactor(ca, config, rand)
                .withGenomeMutator(m)
            ).transform(ca));
    }

    public static void translateToUniversal(NViewer v) {
        CA ca = v.getActiveCA();
        Rule r = ca.getRule();
        Rule uni = GenomeParser.forRule(r).toUniversal(r);
        ca = ca.mutate(uni, null);
        v.setActiveCA(ca);
    }

    public static MutationFactor createMutationFactor(CA ca, Config config, Random r) {
        return createMutationFactor(ca, config, r, false);
    }

    public static MutationFactor createMutationFactor(CA ca, Config config, Random r, boolean trace) {
        int mc = Integer.parseInt(config.getVariable("mutator_maxcolors", "9"));
        final String stage = config.getVariable("mutator_stage", "0");
        MutationFactor mf = MutationFactor.defaultFactor()
            .withAlpha(Integer.parseInt(config.getVariable("mutator_alpha", "20")))
            .withStage("*".equals(stage)?-1:Integer.parseInt(stage))
            .withRandom(r)
            .withTransition(Float.parseFloat(config.getVariable("mutator_transition", "0.5")))
            .withSymmetry("true".equals(config.getVariable("mutator_symmetry", "false")))
            .withMeta("true".equals(config.getVariable("mutator_meta", "false")))
            .withUpdateWeight(config.getWeightVariations())
            .withRule(config.getRuleVariations())
            .withInitializers(config.getInitializerVariations())
            .withLanguage(Languages.universal())
            .withVars(ca.getVars())
            .withArchetype(ca.getRule().archetype())
            .withTrace(trace)
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

    private static JComponent createText(String str, int rows, boolean scroll) {
        JTextArea a = new JTextArea(str, rows, 80);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(false);
        Font f = a.getFont();
        //a.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
        return scroll ? new JScrollPane(a) : a;
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

    private static JComponent createTextArea(String str, int rows, int cols, boolean scroll) {
        JTextArea a = new JTextArea(str, rows, cols);
        a.setEditable(true);
        a.setLineWrap(false);
        a.setWrapStyleWord(false);
        return scroll ? new JScrollPane(a) : a;
    }

    private static String dimension2Text(int d) {
        switch(d) {
            case 1:
                return "One dimension";
            case 2:
                return "Two dimensions";
            case 3:
                return "Three dimensions";
            default:
                return "Hyperdimensional";
        }
    }

    private static String createCaption(CA ca) {
        Rule r = ca.getRule();
        String fmt = "%s, %d colors, %s neighborhood of %s %d, %s values, %s updates, %s edge, initialized with %s";
        String capt = String.format(fmt,
            dimension2Text(r.archetype().dims()),
            r.archetype().colors(),
            r.archetype().neighborhood().getName(),
            r.archetype().neighborhood()==Archetype.Neighborhood.circular?"radius":"size",
            r.archetype().size(),
            r.archetype().values(),
            ca.getUpdateMode().humanize().toLowerCase(),
            ca.getEdgeMode().humanize().toLowerCase(),
            ca.getInitializer().humanize().toLowerCase()
        );
        if(! (ca.getExternalForce() instanceof ExternalForce.NopExternalForce) ) {
            capt += ", external force of "+ca.getExternalForce().humanize();
        }
        if(r instanceof ComputedRule2d) {
            ComputedRule2d cr = (ComputedRule2d) r;
            capt += "\n\nRule Genome:\n"+cr.prettyGenome();
        }
        else {
            capt += "\n\nRule:\n\n"+r.humanize();
        }
        if(ca.getMeta()!=null) {
            capt = capt+"\n\nWith meta "+createCaption(ca.getMeta());
        }
        return capt;
    }

    private static JComponent createColorPanel(Palette palette) {
        JPanel colors = new JPanel();
        colors.setAlignmentY(0);
        colors.add(new JLabel(""+palette.getColorCount()+": "));
        if(palette.getColorCount()<=100) {
            for(int col:palette.getColors()) {
                colors.add(new CAEditor.Cell(col));
                colors.add(new JLabel(Colors.toColorString(col)));
            }
        }
        return colors;
    }
}
