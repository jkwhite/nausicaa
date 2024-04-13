package org.excelsi.nausicaa;


import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.excelsi.nausicaa.ca.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class NViewer extends JFrame implements UIActions, Sizer {
    private static final Logger LOG = LoggerFactory.getLogger(NViewer.class);

    private static int _width;
    private static int _height;
    private static JFrame _root;
    private JMenuItem _repeat;
    private JTabbedPane _tabs;
    private Actions _a = new Actions();
    private static NViewer _instance;
    private Initializers _init = Initializers.random;
    private Initializer _initializer;
    private Futures _futures;
    private Config _config;
    private Timeline _timeline;
    private JFrame _peditor;
    private JFrame _reditor;
    private JFrame _leditor;
    private PaletteEditor _paletteEditor;
    private RuleEditor _ruleEditor;
    private LangEditor _langEditor;
    private JMenuItem _pehack;
    private JMenuItem _rehack;
    private JMenuItem _lehack;
    private Random _random;


    static {
        _width = Toolkit.getDefaultToolkit().getScreenSize().width;
        _height = Toolkit.getDefaultToolkit().getScreenSize().height;
    }

    public static UIActions getUIActions() {
        return instance();
    }

    public static NViewer instance() {
        return _instance;
    }

    public static void main(String[] args) {
        NViewer v = new NViewer();
        _instance = v;
        _root = v;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                v.init();
                v.invalidate();
                loadWindow(v.getConfig(), v, "main");
                v.setVisible(true);
            }
        });
    }

    public NViewer() {
        super("Multiverse");
    }

    @Override
    public Config getConfig() {
        return _config;
    }

    public Random getRandom() {
        return _random;
    }

    @Override
    public CA getActiveCA() {
        return futures().getCA();
    }

    @Override public void setActiveCA(CA ca) {
        futures().setCA(ca);
    }

    @Override public void branch(CA c) {
        String name = ""+(1+_tabs.getTabCount());
        Futures f = new Futures(this, this, _config, _timeline, c, new Random(), name);
        JPanel main = new JPanel(new BorderLayout());
        main.add(f, BorderLayout.CENTER);
        _tabs.addTab(name, main);
    }

    public void pickRandom() {
        futures().pickRandom();
    }

    public void setInitializer(Initializer initializer) {
        _initializer = initializer;
        setActiveCA(getActiveCA().initializer(_initializer));
    }

    @Override
    public PlaneDisplayProvider getPlaneDisplayProvider() {
        return futures();
    }

    @Override public PlanescapeProvider getPlanescapeProvider() {
        return futures();
    }

    @Override public Frame getRoot() {
        return this;
    }

    @Override public Dimension getAppSize() {
        JRootPane p = getRootPane();
        return p.getSize();
    }

    public void init() {
        //final int w = 600, h = 600, d = 1;
        //final int w = 300, h = 300, d = 1, pre = 0;
        final float weight = 1f;
        //final int w = 100, h = 100, d = 100, pre=0;
        final int w = 300, h = 300, d = 1, pre=0;
        //_config = new Config(w, h, d, weight);
        _config = Config.load();
        _timeline = new Timeline();
        if(root()!=null) {
            createMenu();
        }
        setSize(_width, _height);
        int dims = 2;
        int size = 1;
        Random rand = new Random();
        _random = rand;
        CA ca = initCA();

        JPanel main = new JPanel(new BorderLayout());

        Futures f = new Futures(this, this, _config, _timeline, ca, new Random(), "1");
        _futures = f;
        main.add(f, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1", main);
        getContentPane().add(tabs);
        _tabs = tabs;

        //getContentPane().add(main);
    }

    public Futures getFutures() {
        // return _futures;
        return futures();
    }

    public void saveUIState() {
        saveWindow(_config, this, "main");
        if(_console!=null) {
            saveWindow(_config, _console, "console");
        }
    }

    private static void saveWindow(Config c, Window w, String name) {
        StringBuilder b = new StringBuilder();
        b.append(w.getLocation().x).append(",").append(w.getLocation().y)
            .append(",").append(w.getWidth()).append(",").append(w.getHeight());
        c.setVariable("window_"+name, b.toString());
    }

    private static void loadWindow(Config c, Window w, String name) {
        String s = c.<String>getVariable("window_"+name, null);
        if(s!=null) {
            String[] dims = s.split(",");
            try {
                w.setLocation(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
                w.setSize(Integer.parseInt(dims[2]), Integer.parseInt(dims[3]));
            }
            catch(Exception e) {
                LOG.warn("ignoring malformed value for window_"+name+": '"+s+"'", e);
            }
        }
    }

    private Futures futures() {
        return (Futures) ((JPanel)_tabs.getSelectedComponent()).getComponent(0);
    }

    private CA initCA() {
        //final int w = 600, h = 600, d = 1;
        //final int w = 300, h = 300, d = 1, pre = 0;
        final float weight = 1f;
        //final int w = 100, h = 100, d = 100, pre=0;
        final int w = 300, h = 300, d = 1, pre=0;
        //_config = new Config(w, h, d, weight);
        int dims = 2;
        int size = 1;
        Random rand = new Random();
        Palette pal = Palette.random(2, rand, true);
        //Palette pal = Palette.grey(colors);
        //Palette pal = Palette.rainbow(colors,true);
        //Palette pal = new RGBAPalette();
        //int colors = 1001;
        //int colors = 1000;
        int colors = pal.getColorCount();
        //int colors = 2;
        org.excelsi.nausicaa.ca.Archetype a = new org.excelsi.nausicaa.ca.Archetype(dims, size, colors, Archetype.Neighborhood.moore, Values.discrete);
        org.excelsi.nausicaa.ca.Archetype a1 = new org.excelsi.nausicaa.ca.Archetype(1, size, colors);
        org.excelsi.nausicaa.ca.Archetype a2 = new org.excelsi.nausicaa.ca.Archetype(2, size, colors);
        //Ruleset rs = new IndexedRuleset1d(a);
        //Ruleset rs = new IndexedRuleset1d(a1, new IndexedRuleset2d(a2));
        //Ruleset rs = new IndexedRuleset2d(a);
        //Language lang = Languages.universal();
        Language lang = Languages.universal();
        Ruleset rs = new ComputedRuleset(a, lang);
        //Ruleset rs = new IndexedRuleset1d(a1, new IndexedRuleset1d(a1));
        Rule rule = rs.random(rand, new Implicate(a, new Datamap(), lang)).next();
        ComputeMode cmode = ComputeMode.combined;
        //pal = new Palette(Colors.pack(0,0,0,255), Colors.pack(255,255,255,255));
        Varmap vm = new Varmap();
        CA ca = new org.excelsi.nausicaa.ca.CA(
                rule,
                pal,
                Initializers.random.create(),
                rand,
                0,
                w,
                h,
                d,
                pre,
                weight,
                0,
                cmode,
                MetaMode.depth,
                new UpdateMode.SimpleSynchronous(),
                EdgeMode.defaultMode(),
                ExternalForce.nop(),
                vm,
                null,
                "Nameless");
        return ca;
    }

    public static JFrame root() {
        return _root;
    }

    public void doWait(Runnable r, final long initDelay) {
        final JRootPane root = getRootPane();
        final long now = System.currentTimeMillis();
        Thread mon = null;
        try {
            mon = new Thread() {
                public void run() {
                    while(!isInterrupted()) {
                        if(now+initDelay<System.currentTimeMillis()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    Cursor w = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                                    root.getGlassPane().setCursor(w);
                                    root.getGlassPane().setVisible(true);
                                }
                            });
                            break;
                        }
                        try {
                            Thread.sleep(500);
                        }
                        catch(InterruptedException e) {
                            break;
                        }
                    }
                }
            };
            mon.start();
            r.run();
        }
        finally {
            if(mon!=null) {
                mon.interrupt();
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Cursor w = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                    root.getGlassPane().setCursor(w);
                    root.getGlassPane().setVisible(false);
                }
            });
        }
    }

    private void createMenu() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        JMenuBar bar = new JMenuBar();
        createFileMenu(shortcut, bar);
        createAutomataMenu(shortcut, bar);
        createAnimationMenu(shortcut, bar);
        createPaletteMenu(shortcut, bar);
        createMutateMenu(shortcut, bar);
        createRenderMenu(shortcut, bar);
        createFunctionsMenu(shortcut, bar);
        createViewMenu(shortcut, bar);
        createExperimentalMenu(shortcut, bar);
        createWindowMenu(shortcut, bar);
        root().setJMenuBar(bar);

        try {
            Class.forName("org.excelsi.nausicaa.MacCustomizer").getMethod("run", new Class[]{JMenuBar.class}).invoke(null, new Object[]{bar});
        }
        catch(Throwable t) {
            t.printStackTrace();
            createNonMacMenu(shortcut, bar);
        }
    }

    private void createNonMacMenu(int shortcut, JMenuBar bar) {
        JMenu help = new JMenu("Help");
        AbstractAction about = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, 
                    "<html>Copyright (C) 2007-2023 John K White, dhcmrlchtdj@gmail.com<br/>Licensed under the terms of the GNU General Public License Version 3</html>", 
                    "NausiCAä 1.0", 
                    JOptionPane.INFORMATION_MESSAGE,
                    new ImageIcon(MacCustomizer.class.getResource("/na1_164.png"))
                );
            }
        };
        JMenuItem abouti = help.add(about);
        abouti.setText("About");
        bar.add(help);
    }

    private void createFileMenu(int shortcut, JMenuBar bar) {
        JMenu file = new JMenu("File");
        AbstractAction newCA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.newCA(NViewer.this);
            }
        };
        AbstractAction newCAImageRGB = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.newCAImageRGB(NViewer.this, _config);
            }
        };
        AbstractAction newCAImageRGBA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.newCAImageRGBA(NViewer.this, _config);
            }
        };
        AbstractAction newCAImageIndexed = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.newCAImageIndexed(NViewer.this, _config);
            }
        };
        AbstractAction newCAImageCont = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.newCAImageCont(NViewer.this, _config);
            }
        };
        AbstractAction newCAImageContChan = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.newCAImageContChan(NViewer.this, _config);
            }
        };
        AbstractAction open = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.load(NViewer.this, _config);
            }
        };
        AbstractAction save = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.save(NViewer.this, _config);
            }
        };
        AbstractAction close = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //_a.close(NViewer.this);
                closeFutures();
            }
        };
        AbstractAction exportImg = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.exportImage(NViewer.this, _config);
            }
        };
        AbstractAction exportGenerated = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.exportGenerated(NViewer.this, _config);
            }
        };
        // AbstractAction snapScene = new AbstractAction() {
            // public void actionPerformed(ActionEvent e) {
                // _a.snapshotScene(NViewer.this, _config);
            // }
        // };
        JMenuItem ni = file.add(newCA);
        ni.setText("New ...");
        ni.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));



        //JMenuItem niimg = file.add(newCAImage);
        //niimg.setText("New from image...");

        JMenu newimg = new JMenu("New from image");
        file.add(newimg);

        JMenuItem niimgrgb = newimg.add(newCAImageRGB);
        niimgrgb.setText("RGB");
        JMenuItem niimgrgba = newimg.add(newCAImageRGBA);
        niimgrgba.setText("RGBA");
        JMenuItem niimgidx = newimg.add(newCAImageIndexed);
        niimgidx.setText("Indexed");
        JMenuItem niimgcont = newimg.add(newCAImageCont);
        niimgcont.setText("Continuous");
        JMenuItem niimgcontchan = newimg.add(newCAImageContChan);
        niimgcontchan.setText("Continuous Channels");


        JMenuItem oi = file.add(open);
        oi.setText("Open ...");
        oi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
        JMenuItem si = file.add(save);
        si.setText("Save ...");
        si.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
        JMenuItem cl = file.add(close);
        cl.setText("Close");
        cl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
        file.addSeparator();
        JMenuItem expImg = file.add(exportImg);
        expImg.setText("Export generated image ...");
        JMenuItem expGenerated = file.add(exportGenerated);
        expGenerated.setText("Export generated data ...");
        bar.add(file);
    }

    private Palette applyPaletteOptions(Palette p, Config c) {
        Palette ret = p;
        int cut = Integer.parseInt(c.getVariable("palette_cut", "0"));
        if(cut>0) {
            ret = ret.cut(cut, _random);
        }
        return ret;
    }

    private static boolean blackZero(Config c) {
        return "true".equals(c.getVariable("palette_blackzero", "true"));
    }

    private AbstractAction _lastPaletteAction = null;
    private void createPaletteMenu(int shortcut, JMenuBar bar) {
        JMenu pal = new JMenu("Palette");
        final boolean[] hack = new boolean[2];
        final NViewer v = this;
        hack[0] = true;

        AbstractAction repeat = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if(_lastPaletteAction!=null) {
                    _lastPaletteAction.actionPerformed(e);
                }
            }
        };
        JMenuItem rep = pal.add(repeat);
        rep.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, shortcut));
        rep.setText("Repeat palette");

        pal.addSeparator();

        AbstractAction greys = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.grey(getActiveCA().getPalette().getColorCount()), _config)));
            }
        };
        JMenuItem grey = pal.add(greys);
        grey.setText("Grayscale");

        AbstractAction rands = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.random(getActiveCA().getPalette().getColorCount(), v.getRandom()), _config)));
            }
        };
        JMenuItem rand = pal.add(rands);
        rand.setText("Random");

        AbstractAction rains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.rainbow(getActiveCA().getPalette().getColorCount(), blackZero(_config)), _config)));
            }
        };
        JMenuItem rain = pal.add(rains);
        rain.setText("Rainbow");

        /*
        AbstractAction brains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.rainbow(getActiveCA().getPalette().getColorCount(), blackZero(_config)), _config)));
            }
        };
        JMenuItem brain = pal.add(brains);
        brain.setText("Black Rainbow");
        */

        AbstractAction rrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()/2), _config)));
            }
        };
        JMenuItem rrain = pal.add(rrains);
        rrain.setText("Spectrum");

        AbstractAction rdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()), _config)));
            }
        };
        JMenuItem rdrain = pal.add(rdrains);
        rdrain.setText("Dense Spectrum");

        AbstractAction srdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomShinyRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()/2), _config)));
            }
        };
        JMenuItem srdrain = pal.add(srdrains);
        srdrain.setText("Sparkly Spectrum");

        AbstractAction bsrdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomShinyRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()), _config)));
            }
        };
        JMenuItem bsrdrain = pal.add(bsrdrains);
        bsrdrain.setText("Dense Sparkly Spectrum");

        AbstractAction ssrdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomShinyRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()/4), _config)));
            }
        };
        JMenuItem ssrdrain = pal.add(ssrdrains);
        ssrdrain.setText("Sparse Sparkly Spectrum");

        AbstractAction sssrdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomShinyRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()/10), _config)));
            }
        };
        JMenuItem sssrdrain = pal.add(sssrdrains);
        sssrdrain.setText("Super Sparse Sparkly Spectrum");

        AbstractAction wrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomWrappedRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()/2), _config)));
            }
        };
        JMenuItem wrain = pal.add(wrains);
        wrain.setText("Wrapped Spectrum");

        /*
        AbstractAction wbrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getActiveCA().getRandom().nextBoolean();
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomWrappedRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()/2), _config)));
            }
        };
        JMenuItem wbrain = pal.add(wbrains);
        wbrain.setText("Wrapped Black Spectrum");
        */

        AbstractAction wdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.randomWrappedRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), blackZero(_config), getActiveCA().getPalette().getColorCount()), _config)));
            }
        };
        JMenuItem wdrain = pal.add(wdrains);
        wdrain.setText("Dense Wrapped Spectrum");

        pal.addSeparator();

        AbstractAction reds = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.shades(getActiveCA().getPalette().getColorCount(), new int[]{255,0,0}), _config)));
            }
        };
        JMenuItem red = pal.add(reds);
        red.setText("Red shades");

        AbstractAction breds = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.shades(getActiveCA().getPalette().getColorCount(), new int[]{255,128,128}), _config)));
            }
        };
        JMenuItem bred = pal.add(breds);
        bred.setText("Bright red shades");

        AbstractAction blues = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.shades(getActiveCA().getPalette().getColorCount(), new int[]{0,0,255}), _config)));
            }
        };
        JMenuItem blue = pal.add(blues);
        blue.setText("Blue shades");

        AbstractAction bblues = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.rainbow(getActiveCA().getPalette().getColorCount(), blackZero(_config), new int[][]{ {0,0,0}, {0,0,128}, {128,128,255}, {255,255,255} }), _config)));
            }
        };
        JMenuItem bblue = pal.add(bblues);
        bblue.setText("Bright blue shades");

        AbstractAction greens = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.shades(getActiveCA().getPalette().getColorCount(), new int[]{0,255,0}), _config)));
            }
        };
        JMenuItem green = pal.add(greens);
        green.setText("Green shades");

        AbstractAction bgreens = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.shades(getActiveCA().getPalette().getColorCount(), new int[]{128,255,128}), _config)));
            }
        };
        JMenuItem bgreen = pal.add(bgreens);
        bgreen.setText("Bright green shades");

        AbstractAction sepia = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.rainbow(getActiveCA().getPalette().getColorCount(), blackZero(_config), new int[][]{ {34,27,1}, {84,49,13}, {112,66,20}, {165,139,36}, {196,145,35} }), _config)));
            }
        };
        JMenuItem isepia = pal.add(sepia);
        isepia.setText("Sepia");

        AbstractAction rwb = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(applyPaletteOptions(Palette.rainbow(getActiveCA().getPalette().getColorCount(), false /*blackZero(_config)*/, new int[][]{ {255,0,0}, {255,255,255}, {0,0,255}, {255,0,0} }), _config)));
            }
        };
        JMenuItem irwb = pal.add(rwb);
        irwb.setText("Red White and Blue");

        pal.addSeparator();

        AbstractAction acbgreens = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(Palette.allShades(getActiveCA().getPalette().getColorCount(), new int[]{255,255,255})));
            }
        };
        JMenuItem acbgreen = pal.add(acbgreens);
        acbgreen.setText("All Creation");

        AbstractAction cutsp = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(Palette.randomCutRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), 60, blackZero(_config), getActiveCA().getPalette().getColorCount()/4, 0)));
            }
        };
        JMenuItem cutsps = pal.add(cutsp);
        cutsps.setText("Shepherd Moons");

        AbstractAction neongs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                setActiveCA(getActiveCA().palette(Palette.randomCutRainbow(v.getRandom(), getActiveCA().getPalette().getColorCount(), 20, blackZero(_config), getActiveCA().getPalette().getColorCount()/4, 0)));
            }
        };
        JMenuItem neong = pal.add(neongs);
        neong.setText("Neon Garden");

        AbstractAction aesth = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                getActiveCA().getRandom().nextBoolean();
                setActiveCA(getActiveCA().palette(
                    new SpectrumBuilder(getActiveCA().getPalette().getColorCount())
                        .cut(true)
                        .key(0f,0,0,0)
                        .key(0.1f,0,12,31)
                        .key(0.01f,45,255,250)
                        .key(0.1f,0,15,45)
                        .key(0.01f,45,255,250)
                        .key(0.1f,70,32,115)
                        .key(0.01f,45,255,250)
                        .key(0.1f,70,32,115)
                        .key(0.01f,45,255,250)
                        .key(0.1f,5,32,109)
                        .key(0.01f,45,255,250)
                        .key(0.1f,201,17,251)
                        .key(0.01f,45,255,250)
                        .key(0.1f,99,21,141)
                        .key(0.01f,45,255,250)
                        .key(0.1f,255,0,254)
                        .build()
                ));
            }
        };
        JMenuItem aesths = pal.add(aesth);
        aesths.setText("Aesthetic");

        pal.addSeparator();

        AbstractAction custs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                //getActiveCA().getRandom().nextInt(1000);
                _a.customSpectrum(NViewer.this, _config);
            }
        };
        JMenuItem cust = pal.add(custs);
        cust.setText("Custom Spectrum ...");

        AbstractAction imgs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _lastPaletteAction = this;
                //getActiveCA().getRandom().nextInt(1000);
                _a.imageSpectrum(NViewer.this, _config);
            }
        };
        JMenuItem img = pal.add(imgs);
        img.setText("From image ...");

        pal.addSeparator();

        //final JCheckBoxMenuItem black = new JCheckBoxMenuItem(new AbstractAction() {
            //public void actionPerformed(ActionEvent e) {
                //hack[0] = !hack[0];
            //}
        //});
        //black.setState(hack[0]);
        //JMenuItem bl = pal.add(black);
        //bl.setText("Black Zero");

        final JMenuItem popt = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.paletteOptions(NViewer.this, _config);
            }
        });
        JMenuItem palopt = pal.add(popt);
        palopt.setText("Options ...");

        bar.add(pal);
    }

    private void createAnimationMenu(int shortcut, JMenuBar bar) {
        JMenu auto = new JMenu("Animation");

        AbstractAction an = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.animate(NViewer.this, _timeline, -1);
            }
        };
        JMenuItem animate = auto.add(an);
        animate.setText("Animate");
        animate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcut));

        AbstractAction ant = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.chooseAnimFrames(NViewer.this, _timeline);
            }
        };
        JMenuItem animatet = auto.add(ant);
        animatet.setText("Animate for N steps ...");

        AbstractAction stp = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.animate(NViewer.this, _timeline, 1);
            }
        };
        JMenuItem stepone = auto.add(stp);
        stepone.setText("Step");
        stepone.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, shortcut));

        AbstractAction animfst = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.animateSpeedup(NViewer.this);
            }
        };
        JMenuItem animfaster = auto.add(animfst);
        animfaster.setText("Speed up animation");
        animfaster.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut | InputEvent.SHIFT_DOWN_MASK));

        AbstractAction animslw = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.animateSlowdown(NViewer.this);
            }
        };
        JMenuItem animslower = auto.add(animslw);
        animslower.setText("Slow down animation");
        animslower.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, shortcut | InputEvent.SHIFT_DOWN_MASK));

        AbstractAction animcrs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.coreConfig(NViewer.this, _config);
            }
        };
        JMenuItem animcores = auto.add(animcrs);
        animcores.setText("Configure animation ...");

        auto.addSeparator();

        AbstractAction gen = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.generateToFile(NViewer.this);
            }
        };
        JMenuItem genl = auto.add(gen);
        genl.setText("Generate to disk ...");
        genl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, shortcut));

        bar.add(auto);
    }

    private void createViewMenu(int shortcut, JMenuBar bar) {
        JMenu auto = new JMenu("View");

        AbstractAction zi = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.zoomIn(NViewer.this);
            }
        };
        JMenuItem zoomin = auto.add(zi);
        zoomin.setText("Zoom in");
        zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut));

        AbstractAction zo = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.zoomOut(NViewer.this);
            }
        };
        JMenuItem zoomout = auto.add(zo);
        zoomout.setText("Zoom out");
        zoomout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, shortcut));

        AbstractAction z1 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.zoomOne(NViewer.this);
            }
        };
        JMenuItem zoomone = auto.add(z1);
        zoomone.setText("Actual size");
        zoomone.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, shortcut));

        auto.addSeparator();

        AbstractAction rot = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        futures().setAnimationsEnabled(!futures().getAnimationsEnabled());
                        //_futures.revalidate();
                    }
                });
            }
        };
        JMenuItem rotate = auto.add(rot);
        rotate.setText("Toggle animations");
        rotate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut | InputEvent.SHIFT_DOWN_MASK));

        bar.add(auto);
    }

    private void createAutomataMenu(int shortcut, JMenuBar bar) {
        final JCheckBoxMenuItem[] hack = new JCheckBoxMenuItem[8];
        JMenu auto = new JMenu("Automata");

        //AbstractAction opentab = new AbstractAction() {
            //public void actionPerformed(ActionEvent e) {
                //newTab();
            //}
        //};
        //JMenuItem openi = auto.add(opentab);
        //openi.setText("Open");
        //openi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
//
        AbstractAction inf = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.info(NViewer.this);
            }
        };
        JMenuItem info = auto.add(inf);
        info.setText("Info");
        info.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, shortcut));

        AbstractAction debug = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.debug(NViewer.this);
            }
        };
        JMenuItem deb = auto.add(debug);
        deb.setText("Debug");

        AbstractAction cancel = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.cancel(NViewer.this);
            }
        };
        JMenuItem canc = auto.add(cancel);
        canc.setText("Cancel current calculation");
        canc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcut));
        auto.addSeparator();

        JCheckBoxMenuItem ran = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.random;
                _a.chooseRandom(NViewer.this, _config);
                //_initializer = new RandomInitializer();
                //setActiveCA(getActiveCA().initializer(_initializer));
                hack[0].setState(true);
                hack[1].setState(false);
                hack[2].setState(false);
                hack[3].setState(false);
                hack[4].setState(false);
                hack[5].setState(false);
                hack[6].setState(false);
                hack[7].setState(false);
                //_a.generate(NViewer.this);
            }
        });
        auto.add(ran);
        hack[0] = ran;
        ran.setText("Random initial state ...");
        ran.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcut));
        ran.setState(_init==Initializers.random);

        /*
        AbstractAction fixa = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.single;
                _a.chooseSingle(NViewer.this, _config);
                //_initializer = new SingleInitializer();
                //setActiveCA(getActiveCA().initializer(_initializer));
                hack[0].setState(false);
                hack[1].setState(true);
                hack[2].setState(false);
                hack[3].setState(false);
                hack[4].setState(false);
                hack[5].setState(false);
                hack[6].setState(false);
                hack[7].setState(false);
                //_a.generate(NViewer.this);
            }
        };
        */
        JCheckBoxMenuItem fix = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.single;
                _a.chooseSingle(NViewer.this, _config);
                //_initializer = new SingleInitializer();
                //setActiveCA(getActiveCA().initializer(_initializer));
                hack[0].setState(false);
                hack[1].setState(true);
                hack[2].setState(false);
                hack[3].setState(false);
                hack[4].setState(false);
                hack[5].setState(false);
                hack[6].setState(false);
                hack[7].setState(false);
                //_a.generate(NViewer.this);
            }
        });
        fix.setText("Fixed initial state");
        fix.setSelected(_init==Initializers.single);
        fix.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcut));
        auto.add(fix);
        hack[1] = fix;

        JCheckBoxMenuItem wrd = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.word;
                _a.chooseWord(NViewer.this);
                //setActiveCA(getActiveCA().initializer(_initializer));
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(true);
                hack[3].setState(false);
                hack[4].setState(false);
                hack[5].setState(false);
                hack[6].setState(false);
                hack[7].setState(false);
                //hack[3].setState(false);
                //_a.generate(NViewer.this);
            }
        });
        auto.add(wrd);
        wrd.setText("Word initial state ...");
        wrd.setSelected(_init==Initializers.word);
        //wrd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcut));
        hack[2] = wrd;

        JCheckBoxMenuItem img = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.image;
                _a.chooseImage(NViewer.this, _config);
                //setActiveCA(getActiveCA().initializer(_initializer));
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(false);
                hack[3].setState(true);
                hack[4].setState(false);
                hack[5].setState(false);
                hack[6].setState(false);
                hack[7].setState(false);
                //_a.generate(NViewer.this);
            }
        });
        auto.add(img);
        img.setText("Image initial state ...");
        img.setSelected(_init==Initializers.image);
        //wrd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcut));
        hack[3] = img;
        /*
        JCheckBoxMenuItem ara = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Rule.Initialization.arabesque;
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(true);
                hack[3].setState(false);
                _a.generate(NViewer.this);
            }
        });
        auto.add(ara);
        hack[2] = ara;
        ara.setText("Arabesque mode");
        //ara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
        ara.setState(false);
        ara.setEnabled("2".equals(_dimensions));
        JCheckBoxMenuItem imagest = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(false);
                hack[3].setState(true);
                selectImageStateImage();
            }
        });
        auto.add(imagest);
        imagest.setText("Image initial state ...");
        imagest.setSelected(_init==Rule.Initialization.image);
        */

        JCheckBoxMenuItem gau = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.random;
                _a.chooseGaussian(NViewer.this, _config);
                //_initializer = new RandomInitializer();
                //setActiveCA(getActiveCA().initializer(_initializer));
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(false);
                hack[3].setState(false);
                hack[4].setState(true);
                hack[5].setState(false);
                hack[6].setState(false);
                hack[7].setState(false);
                //_a.generate(NViewer.this);
            }
        });
        auto.add(gau);
        hack[4] = gau;
        gau.setText("Gaussian initial state ...");
        gau.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcut));
        gau.setState(_init==Initializers.gaussian);

        JCheckBoxMenuItem cgau = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.random;
                _a.chooseClusteredGaussian(NViewer.this, _config);
                //_initializer = new RandomInitializer();
                //setActiveCA(getActiveCA().initializer(_initializer));
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(false);
                hack[3].setState(false);
                hack[4].setState(false);
                hack[5].setState(true);
                hack[6].setState(false);
                hack[7].setState(false);
                //_a.generate(NViewer.this);
            }
        });
        auto.add(cgau);
        hack[5] = cgau;
        cgau.setText("Clustered gaussian initial state ...");
        cgau.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));
        cgau.setState(_init==Initializers.clusteredgaussian);

        JCheckBoxMenuItem cai = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.random;
                _a.chooseCAInitializer(NViewer.this, _config);
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(false);
                hack[3].setState(false);
                hack[4].setState(false);
                hack[5].setState(false);
                hack[6].setState(true);
                hack[7].setState(false);
            }
        });
        auto.add(cai);
        hack[6] = cai;
        cai.setText("CA initial state ...");
        //cai.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcut));
        cai.setState(_init==Initializers.ca);

        JCheckBoxMenuItem custi = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.custom;
                _a.chooseCustomInitializer(NViewer.this, _config);
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(false);
                hack[3].setState(false);
                hack[4].setState(false);
                hack[5].setState(false);
                hack[6].setState(false);
                hack[7].setState(true);
            }
        });
        auto.add(custi);
        hack[7] = custi;
        custi.setText("Custom initial state ...");
        //cai.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcut));
        custi.setState(_init==Initializers.custom);

        auto.addSeparator();

        AbstractAction re = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Rand.newSeed();
                _a.reroll(NViewer.this);
            }
        };
        JMenuItem reroll = auto.add(re);
        reroll.setText("Reroll");
        reroll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));

        AbstractAction size = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.resizeCA(NViewer.this);
            }
        };
        JMenuItem siz = auto.add(size);
        siz.setText("Configure parameters ...");
        siz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcut));

        AbstractAction vars = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.configureVariables(NViewer.this);
            }
        };
        JMenuItem vrs = auto.add(vars);
        vrs.setText("Variables ...");
        vrs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, shortcut | InputEvent.SHIFT_DOWN_MASK));

        auto.addSeparator();

        {
            JMenu updateopt = new JMenu("Update mode");
            final JCheckBoxMenuItem[] updatehack = new JCheckBoxMenuItem[5];
            JCheckBoxMenuItem updsync = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    updatehack[0].setState(true);
                    updatehack[1].setState(false);
                    updatehack[2].setState(false);
                    updatehack[3].setState(false);
                    updatehack[4].setState(false);
                    _config.setVariable("updatemode", "sync");
                    setActiveCA(getActiveCA().updateMode(UpdateMode.create("sync", 0, 0)));
                }
            });
            updsync.setText("Synchronous");
            updateopt.add(updsync);

            JCheckBoxMenuItem updasync = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    updatehack[0].setState(false);
                    updatehack[1].setState(true);
                    updatehack[2].setState(false);
                    updatehack[3].setState(false);
                    updatehack[4].setState(false);
                    _config.setVariable("updatemode", "async");
                    _a.chooseAsynchronousUpdate(NViewer.this);
                }
            });
            updasync.setText("Asynchronous ...");
            updateopt.add(updasync);

            JCheckBoxMenuItem updasynclocal = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    updatehack[0].setState(false);
                    updatehack[1].setState(false);
                    updatehack[2].setState(true);
                    updatehack[3].setState(false);
                    updatehack[4].setState(false);
                    _config.setVariable("updatemode", "localasync");
                    _a.chooseAsynchronousLocalUpdate(NViewer.this);
                }
            });
            updasynclocal.setText("Local Asynchronous ...");
            updateopt.add(updasynclocal);

            JCheckBoxMenuItem updenergyasync = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    updatehack[0].setState(false);
                    updatehack[1].setState(false);
                    updatehack[2].setState(false);
                    updatehack[3].setState(true);
                    updatehack[4].setState(false);
                    _config.setVariable("updatemode", "energyasync");
                    _a.chooseAsynchronousEnergyUpdate(NViewer.this);
                }
            });
            updenergyasync.setText("Energy Asynchronous ...");
            updateopt.add(updenergyasync);

            JCheckBoxMenuItem updvars = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    updatehack[0].setState(false);
                    updatehack[1].setState(false);
                    updatehack[2].setState(false);
                    updatehack[3].setState(false);
                    updatehack[4].setState(true);
                    _config.setVariable("updatemode", "variable");
                    _a.chooseVariableUpdate(NViewer.this);
                }
            });
            updvars.setText("Variable ...");
            updateopt.add(updvars);

            updatehack[0] = updsync;
            updatehack[1] = updasync;
            updatehack[2] = updasynclocal;
            updatehack[3] = updenergyasync;
            updatehack[4] = updvars;
            switch(_config.getVariable("updatemode","sync")) {
                case "sync":
                    updatehack[0].setState(true);
                    break;
                case "async":
                    updatehack[1].setState(true);
                    break;
                case "localasync":
                    updatehack[2].setState(true);
                    break;
                case "energyasync":
                    updatehack[3].setState(true);
                    break;
                case "variable":
                    updatehack[4].setState(true);
                    break;
            }
            
            auto.add(updateopt);
        }
        {
            JMenu edgeopt = new JMenu("Edge mode");
            final JCheckBoxMenuItem[] edgehack = new JCheckBoxMenuItem[3];
            JCheckBoxMenuItem edgtor = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    edgehack[0].setState(true);
                    edgehack[1].setState(false);
                    edgehack[2].setState(false);
                    _config.setVariable("edgemode", "toroidal");
                    setActiveCA(getActiveCA().edgeMode(new EdgeMode(EdgeMode.Type.toroidal)));
                }
            });
            edgtor.setText("Toroidal");
            edgeopt.add(edgtor);

            JCheckBoxMenuItem edgzero = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    edgehack[0].setState(false);
                    edgehack[1].setState(true);
                    edgehack[2].setState(false);
                    _config.setVariable("edgemode", "zero");
                    setActiveCA(getActiveCA().edgeMode(new EdgeMode(EdgeMode.Type.zero)));
                }
            });
            edgzero.setText("Zero");
            edgeopt.add(edgzero);

            JCheckBoxMenuItem edgconst = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    edgehack[0].setState(false);
                    edgehack[1].setState(false);
                    edgehack[2].setState(true);
                    _config.setVariable("edgemode", "constant");
                    _a.chooseConstantEdgeMode(NViewer.this);
                }
            });
            edgconst.setText("Constant ...");
            edgeopt.add(edgconst);

            edgehack[0] = edgtor;
            edgehack[1] = edgzero;
            edgehack[2] = edgconst;
            switch(_config.getVariable("edgemode","toroidal")) {
                case "toroidal":
                    edgehack[0].setState(true);
                    break;
                case "zero":
                    edgehack[1].setState(true);
                    break;
                case "constant":
                    edgehack[2].setState(true);
                    break;
            }
            
            auto.add(edgeopt);
        }
        {
            JMenu extopt = new JMenu("External Force");
            final JCheckBoxMenuItem[] exthack = new JCheckBoxMenuItem[3];
            JCheckBoxMenuItem extnop = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    exthack[0].setState(true);
                    exthack[1].setState(false);
                    _config.setVariable("externalforcemode", "nop");
                    setActiveCA(getActiveCA().externalForce(ExternalForce.nop()));
                }
            });
            extnop.setText("None");
            extopt.add(extnop);

            JCheckBoxMenuItem extrand = new JCheckBoxMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    exthack[0].setState(false);
                    exthack[1].setState(true);
                    _config.setVariable("externalforcemode", "random");
                    _a.chooseRandomExternalForce(NViewer.this);
                }
            });
            extrand.setText("Random ...");
            extopt.add(extrand);

            exthack[0] = extnop;
            exthack[1] = extrand;
            switch(_config.getVariable("externalforcemode","nop")) {
                case "nop":
                    exthack[0].setState(true);
                    break;
                case "random":
                    exthack[1].setState(true);
                    break;
            }
            
            auto.add(extopt);
        }
        {
            auto.addSeparator();

            AbstractAction touni = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    Actions.translateToUniversal(NViewer.this);
                }
            };
            JMenuItem univer = auto.add(touni);
            univer.setText("Translate to Universal");
        }


        bar.add(auto);
    }

    private void createMutateMenu(int shortcut, JMenuBar bar) {
        JMenu mutate = new JMenu("Mutate");
        AbstractAction rep = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.repeatLastMutation(NViewer.this, _config, _random);
            }
        };
        _repeat = mutate.add(rep);
        _repeat.setText("Repeat last mutation");
        _repeat.setEnabled(false);
        _repeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcut));

        mutate.addSeparator();

        for(final java.lang.reflect.Method m:GenomeMutators.class.getDeclaredMethods()) {
            final String mname = Character.toUpperCase(m.getName().charAt(0))+m.getName().substring(1);
            if(!mname.startsWith("Lambda$")) {
                AbstractAction mut = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        _repeat.setEnabled(true);
                        _repeat.setText("Repeat "+mname);
                        try {
                            GenomeMutator gm = (GenomeMutator) m.invoke(null, new Object[0]);
                            _a.mutate(NViewer.this, _config, _random, gm);
                        }
                        catch(Exception ex) {
                            LOG.error(mname+" failed: "+ex.getMessage(), ex);
                        }
                    }
                };
                JMenuItem mutat = mutate.add(mut);
                mutat.setText(mname);
            }
        }
        mutate.addSeparator();

        JMenuItem addseg = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.addRuleStage(NViewer.this, _config, _random);
            }
        });
        mutate.add(addseg);
        addseg.setText("Add rule stage");

        JMenuItem adddseg = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.addDataStage(NViewer.this, _config, _random);
            }
        });
        mutate.add(adddseg);
        adddseg.setText("Add data stage");

        JMenuItem remseg = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.removeRuleStage(NViewer.this, _config, _random);
            }
        });
        mutate.add(remseg);
        remseg.setText("Remove active rule stage");

        mutate.addSeparator();
        JMenuItem pushmeta = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.pushMeta(NViewer.this, _config);
            }
        });
        mutate.add(pushmeta);
        pushmeta.setText("Push Meta");
        JMenuItem popmeta = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.popMeta(NViewer.this, _config);
            }
        });
        mutate.add(popmeta);
        popmeta.setText("Pop Meta");
        JMenuItem rotmeta = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.rotateMeta(NViewer.this, _config);
            }
        });
        mutate.add(rotmeta);
        rotmeta.setText("Rotate Meta");

        final JCheckBoxMenuItem[] mhack = new JCheckBoxMenuItem[6];

        mutate.addSeparator();
        JCheckBoxMenuItem incHueVariations = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _config.setHueVariations(!_config.getHueVariations());
                mhack[1].setState(!mhack[1].getState());
                mhack[1].setSelected(!mhack[1].getState());
            }
        });
        mutate.add(incHueVariations);
        mhack[1] = incHueVariations;
        incHueVariations.setText("Hue variations");
        incHueVariations.setState(_config.getHueVariations());
        incHueVariations.setSelected(_config.getHueVariations());

        JCheckBoxMenuItem incWeightVariations = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _config.setWeightVariations(!_config.getWeightVariations());
                mhack[2].setState(!mhack[2].getState());
                mhack[2].setSelected(!mhack[2].getState());
            }
        });
        mutate.add(incWeightVariations);
        mhack[2] = incWeightVariations;
        incWeightVariations.setText("Weight variations");
        incWeightVariations.setState(_config.getWeightVariations());
        incWeightVariations.setSelected(_config.getWeightVariations());

        JCheckBoxMenuItem incRuleVariations = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _config.setRuleVariations(!_config.getRuleVariations());
                mhack[3].setState(!mhack[3].getState());
                mhack[3].setSelected(!mhack[3].getState());
            }
        });
        mutate.add(incRuleVariations);
        mhack[3] = incRuleVariations;
        incRuleVariations.setText("Rule variations");
        incRuleVariations.setState(_config.getRuleVariations());
        incRuleVariations.setSelected(_config.getRuleVariations());

        JCheckBoxMenuItem incParamVariations = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _config.setParamVariations(!_config.getParamVariations());
                mhack[4].setState(!mhack[4].getState());
                mhack[4].setSelected(!mhack[4].getState());
            }
        });
        mutate.add(incParamVariations);
        mhack[4] = incParamVariations;
        incParamVariations.setText("Parameter variations");
        incParamVariations.setState(_config.getParamVariations());
        incParamVariations.setSelected(_config.getParamVariations());

        JCheckBoxMenuItem incInitVariations = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _config.setInitializerVariations(!_config.getInitializerVariations());
                mhack[5].setState(!mhack[5].getState());
                mhack[5].setSelected(!mhack[5].getState());
            }
        });
        mutate.add(incInitVariations);
        mhack[5] = incInitVariations;
        incInitVariations.setText("Initializer variations");
        incInitVariations.setState(_config.getInitializerVariations());
        incInitVariations.setSelected(_config.getInitializerVariations());

        JMenuItem mparams = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.mutationParams(NViewer.this, _config);
            }
        });
        mutate.add(mparams);
        mparams.setText("Parameters ...");

        mutate.addSeparator();
        JMenuItem evolver = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.evolver(NViewer.this, _random);
            }
        });
        mutate.add(evolver);
        evolver.setText("Evolver ...");

        mutate.addSeparator();
        JMenuItem automode = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.automutate(NViewer.this);
            }
        });
        mutate.add(automode);
        automode.setText("Toggle auto mutate");

        bar.add(mutate);
    }

    private void createFunctionsMenu(int shortcut, JMenuBar bar) {
        JMenu functions = new JMenu("Functions");

        Functions fns = new Functions(new File(System.getProperty("app.root")+"/etc/functions"));
        for(Functions.CAFunction fn:fns.catalog()) {
            JMenuItem mfn = new JMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    _a.invokeFunction(NViewer.this, fn);
                }
            });
            functions.add(mfn);
            mfn.setText(fn.getName()+" ...");
        }

        bar.add(functions);
    }

    private void createRenderMenu(int shortcut, JMenuBar bar) {
        JMenu render = new JMenu("Render");

        JMenu rgbopt = new JMenu("RGB compute mode");
        final JCheckBoxMenuItem[] rgbhack = new JCheckBoxMenuItem[2];
        JCheckBoxMenuItem rgbcomb = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                rgbhack[0].setState(true);
                rgbhack[1].setState(false);
                _config.setVariable("rgb_computemode", "combined");
                setActiveCA(getActiveCA().computeMode(ComputeMode.combined));
            }
        });
        rgbcomb.setText("Combined");
        rgbopt.add(rgbcomb);
        JCheckBoxMenuItem rgbchan = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                rgbhack[0].setState(false);
                rgbhack[1].setState(true);
                _config.setVariable("rgb_computemode", "channel");
                setActiveCA(getActiveCA().computeMode(ComputeMode.channel));
            }
        });
        rgbchan.setText("By channel");
        rgbopt.add(rgbchan);
        rgbhack[0] = rgbcomb;
        rgbhack[1] = rgbchan;
        rgbhack[_config.getVariable("rgb_computemode","combined").equals("combined")?0:1].setState(true);

        render.add(rgbopt);

        JMenu metaopt = new JMenu("Meta compute mode");
        final JCheckBoxMenuItem[] metahack = new JCheckBoxMenuItem[2];
        JCheckBoxMenuItem metacomb = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                metahack[0].setState(true);
                metahack[1].setState(false);
                _config.setVariable("meta_computemode", "depth");
                setActiveCA(getActiveCA().metaMode(MetaMode.depth));
            }
        });
        metacomb.setText("Depth");
        metaopt.add(metacomb);
        JCheckBoxMenuItem metachan = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                metahack[0].setState(false);
                metahack[1].setState(true);
                _config.setVariable("meta_computemode", "none");
                setActiveCA(getActiveCA().metaMode(MetaMode.none));
            }
        });
        metachan.setText("None");
        metaopt.add(metachan);
        metahack[0] = metacomb;
        metahack[1] = metachan;
        metahack[_config.getVariable("meta_computemode","depth").equals("depth")?0:1].setState(true);

        render.add(metaopt);

        JMenu compopt = new JMenu("Composition mode");
        final JCheckBoxMenuItem[] comphack = new JCheckBoxMenuItem[9];

        JCheckBoxMenuItem compfirst = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(true);
                comphack[1].setState(false);
                comphack[2].setState(false);
                comphack[3].setState(false);
                comphack[4].setState(false);
                comphack[5].setState(false);
                comphack[6].setState(false);
                comphack[7].setState(false);
                comphack[8].setState(false);
                _config.setVariable("composite_mode", "front");
            }
        });
        compfirst.setText("Nearest only");
        compopt.add(compfirst);

        JCheckBoxMenuItem complast = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(false);
                comphack[1].setState(true);
                comphack[2].setState(false);
                comphack[3].setState(false);
                comphack[4].setState(false);
                comphack[5].setState(false);
                comphack[6].setState(false);
                comphack[7].setState(false);
                comphack[8].setState(false);
                _config.setVariable("composite_mode", "back");
            }
        });
        complast.setText("Farthest only");
        compopt.add(complast);

        JCheckBoxMenuItem compavg = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(false);
                comphack[1].setState(false);
                comphack[2].setState(true);
                comphack[3].setState(false);
                comphack[4].setState(false);
                comphack[5].setState(false);
                comphack[6].setState(false);
                comphack[7].setState(false);
                comphack[8].setState(false);
                _config.setVariable("composite_mode", "wavg");
            }
        });
        compavg.setText("Weighted average");
        compopt.add(compavg);

        JCheckBoxMenuItem compravg = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(false);
                comphack[1].setState(false);
                comphack[2].setState(false);
                comphack[3].setState(true);
                comphack[4].setState(false);
                comphack[5].setState(false);
                comphack[6].setState(false);
                comphack[7].setState(false);
                comphack[8].setState(false);
                _config.setVariable("composite_mode", "revwavg");
            }
        });
        compravg.setText("Weighted average reverse");
        compopt.add(compravg);

        JCheckBoxMenuItem comppavg = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(false);
                comphack[1].setState(false);
                comphack[2].setState(false);
                comphack[3].setState(false);
                comphack[4].setState(true);
                comphack[5].setState(false);
                comphack[6].setState(false);
                comphack[7].setState(false);
                comphack[8].setState(false);
                _config.setVariable("composite_mode", "avg");
            }
        });
        comppavg.setText("Average");
        compopt.add(comppavg);

        JCheckBoxMenuItem compchan = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(false);
                comphack[1].setState(false);
                comphack[2].setState(false);
                comphack[3].setState(false);
                comphack[4].setState(false);
                comphack[5].setState(true);
                comphack[6].setState(false);
                comphack[7].setState(false);
                comphack[8].setState(false);
                _config.setVariable("composite_mode", "channel");
            }
        });
        compchan.setText("Channel");
        compopt.add(compchan);

        JCheckBoxMenuItem compmul = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(false);
                comphack[1].setState(false);
                comphack[2].setState(false);
                comphack[3].setState(false);
                comphack[4].setState(false);
                comphack[5].setState(false);
                comphack[6].setState(true);
                comphack[7].setState(false);
                comphack[8].setState(false);
                _config.setVariable("composite_mode", "multiply");
            }
        });
        compmul.setText("Multiply");
        compopt.add(compmul);

        JCheckBoxMenuItem compdiff = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(false);
                comphack[1].setState(false);
                comphack[2].setState(false);
                comphack[3].setState(false);
                comphack[4].setState(false);
                comphack[5].setState(false);
                comphack[6].setState(false);
                comphack[7].setState(true);
                comphack[8].setState(false);
                _config.setVariable("composite_mode", "difference");
            }
        });
        compdiff.setText("Difference");
        compopt.add(compdiff);

        JCheckBoxMenuItem comptf = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                comphack[0].setState(false);
                comphack[1].setState(false);
                comphack[2].setState(false);
                comphack[3].setState(false);
                comphack[4].setState(false);
                comphack[5].setState(false);
                comphack[6].setState(false);
                comphack[7].setState(false);
                comphack[8].setState(true);
                _config.setVariable("composite_mode", "truefront");
            }
        });
        comptf.setText("True Nearest");
        compopt.add(comptf);

        comphack[0] = compfirst;
        comphack[1] = complast;
        comphack[2] = compavg;
        comphack[3] = compravg;
        comphack[4] = comppavg;
        comphack[5] = compchan;
        comphack[6] = compmul;
        comphack[7] = compdiff;
        comphack[8] = comptf;

        comphack[0].setState(true);

        render.add(compopt);

        final JMenuItem[] vhack = new JMenuItem[1];
        final JMenuItem view3d = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                final ViewType vt = futures()!=null ? futures().getViewType() : ViewType.view2d;
                vhack[0].setText(vt==ViewType.view2d?"View in 2D":"View in 3D");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        futures().setViewType(vt==ViewType.view2d?ViewType.view3d:ViewType.view2d);
                        //_futures.revalidate();
                    }
                });
            }
        });
        render.add(view3d);
        vhack[0] = view3d;
        view3d.setText("View in 3D");
        view3d.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcut | InputEvent.SHIFT_DOWN_MASK));

        //TODO
        // final JMenuItem eview3d = new JMenuItem(new AbstractAction() {
            // public void actionPerformed(ActionEvent e) {
                // SwingUtilities.invokeLater(new Runnable() {
                    // public void run() {
                        // _a.external3dView(NViewer.this, _config);
                    // }
                // });
            // }
        // });
        // eview3d.setText("View in 3D external");
        // render.add(eview3d);

        // final JMenuItem viewh = new JMenuItem(new AbstractAction() {
            // public void actionPerformed(ActionEvent e) {
                // _a.view3d(NViewer.this);
            // }
        // });
        // viewh.setText("View from higher dimension");
        // render.add(viewh);

        bar.add(render);
    }

    private void createExperimentalMenu(int shortcut, JMenuBar bar) {
        JMenu exp = new JMenu("Experimental");
        final JMenuItem iter = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JWindow w = new JWindow(NViewer.this);
                w.add(new JIteratedFunction());
                w.setLocation(0,20);
                w.setSize(600,600);
                w.setVisible(true);
            }
        });
        iter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcut));
        iter.setText("Iterated Function");
        exp.add(iter);
        bar.add(exp);
    }

    private void createWindowMenu(int shortcut, JMenuBar bar) {
        JMenu window = new JMenu("Window");
        final JMenuItem[] fhack = new JMenuItem[1];
        final JMenuItem full = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                boolean cval = futures()!=null ? futures().getShow() : false;
                final boolean nval = !cval;
                fhack[0].setText(nval?"Hide mutations":"Show mutations");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //_a.generate();
                        futures().setShow(nval);
                        //_futures.revalidate();
                    }
                });
            }
        });
        window.add(full);
        fhack[0] = full;
        full.setText("Hide mutations");
        full.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut));

        final JMenuItem peditor = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //_editor = !_editor;
                togglePaletteEditor();
            }
        });
        _pehack = peditor;
        peditor.setText("Show palette editor");
        //peditor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcut));
        window.add(peditor);

        final JMenuItem formed = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleRuleEditor();
            }
        });
        _rehack = formed;
        formed.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcut));
        formed.setText("Show rule editor");
        window.add(formed);

        final JMenuItem langed = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleLangEditor();
            }
        });
        _lehack = langed;
        langed.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut | InputEvent.SHIFT_DOWN_MASK));
        langed.setText("Show language editor");
        window.add(langed);

        window.addSeparator();

        final JMenuItem conso = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // _a.openConsole(NViewer.this, _config);
                toggleConsole();
            }
        });
        conso.setText("Toggle console ...");
        conso.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcut | InputEvent.SHIFT_DOWN_MASK));
        window.add(conso);

        //final JMenuItem closef = new JMenuItem(new AbstractAction() {
            //public void actionPerformed(ActionEvent e) {
                //closeFutures();
            //}
        //});
        //closef.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
        //closef.setText("Close current futures");
        //window.add(closef);

        bar.add(window);
    }

    private void closeFutures() {
        //System.err.println("TABS: "+_tabs.getTabCount()+", SEL: "+_tabs.getSelectedIndex());
        if(_tabs.getTabCount()>1) {
            _tabs.removeTabAt(_tabs.getSelectedIndex());
        }
    }

    private void togglePaletteEditor() {
        if(_peditor!=null) {
            _paletteEditor.disconnect();
            _paletteEditor = null;
            _peditor.setVisible(false);
            _peditor = null;
        }
        else {
            _peditor = new JFrame("Palette Editor");
            _paletteEditor = new PaletteEditor(_peditor, this, _timeline);
            _peditor.getContentPane().add(_paletteEditor);
            _peditor.pack();
            Dimension dim = _peditor.getContentPane().getPreferredSize();
            _peditor.setSize(16+dim.width, 24+dim.height);

            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            JMenuBar bar = new JMenuBar();
            JMenu file = new JMenu("File");
            AbstractAction close = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    _peditor.setVisible(false);
                    _peditor = null;
                }
            };
            JMenuItem cl = file.add(close);
            cl.setText("Close");
            cl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
            bar.add(file);
            _peditor.setJMenuBar(bar);

            _peditor.setVisible(true);

            _peditor.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    _pehack.setText("Show palette editor");
                    _peditor = null;
                    _paletteEditor.disconnect();
                }

                public void windowClosing(WindowEvent e) {
                    _pehack.setText("Show palette editor");
                    _peditor = null;
                }
            });
        }
        _pehack.setText(_peditor!=null?"Hide palette editor":"Show palette editor");
    }

    private JFrame _console;
    private void toggleConsole() {
        if(_console==null) {
            Dimension d = getSize();
            _console = new JFrame("Console");
            _console.setSize(d.width, d.height/3);
            _console.getContentPane().add(new Console(_console));
            _console.pack();
            _console.setSize(d.width, d.height/3);
            // _console.setLocationRelativeTo(this);
            _console.setLocation(getLocation().x, 0);
            loadWindow(getConfig(), _console, "console");
            _console.setVisible(true);
        }
        else {
            _console.setVisible(!_console.isVisible());
        }
    }

    private void toggleRuleEditor() {
        if(_reditor!=null) {
            _ruleEditor.disconnect();
            _ruleEditor = null;
            _reditor.setVisible(false);
            _reditor = null;
        }
        else {
            _reditor = new JFrame("Rule Editor");
            _reditor.setLocation(_root.getLocation().x, _root.getLocation().y);
            _ruleEditor = new RuleEditor(_reditor, this, _timeline,
                Actions.createMutationFactor(getActiveCA(), _config, _random));
            _reditor.getContentPane().add(_ruleEditor);
            _reditor.pack();
            Dimension dim = _reditor.getContentPane().getPreferredSize();
            _reditor.setSize(16+dim.width, 24+dim.height);

            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            JMenuBar bar = new JMenuBar();
            JMenu file = new JMenu("File");
            AbstractAction close = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    // _reditor.setVisible(false);
                    // _reditor = null;
                    toggleRuleEditor();
                }
            };
            JMenuItem cl = file.add(close);
            cl.setText("Close");
            cl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
            bar.add(file);
            _reditor.setJMenuBar(bar);

            _reditor.setVisible(true);

            _reditor.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    _rehack.setText("Show rule editor");
                    _reditor = null;
                    _ruleEditor.disconnect();
                }

                public void windowClosing(WindowEvent e) {
                    _rehack.setText("Show rule editor");
                    _reditor = null;
                }
            });
        }
        _rehack.setText(_reditor!=null?"Hide rule editor":"Show rule editor");
    }

    private void toggleLangEditor() {
        if(_leditor!=null) {
            _langEditor.disconnect();
            _langEditor = null;
            _leditor.setVisible(false);
            _leditor = null;
        }
        else {
            _leditor = new JFrame("Language Editor");
            _langEditor = new LangEditor(_leditor, this, _timeline,
                Actions.createMutationFactor(getActiveCA(), _config, _random));
            _leditor.getContentPane().add(_langEditor);
            _leditor.pack();
            Dimension dim = _leditor.getContentPane().getPreferredSize();
            _leditor.setSize(16+dim.width, 24+dim.height);

            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            JMenuBar bar = new JMenuBar();
            JMenu file = new JMenu("File");
            AbstractAction close = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    _leditor.setVisible(false);
                    _leditor = null;
                    _lehack.setText("Show language editor");
                }
            };
            JMenuItem cl = file.add(close);
            cl.setText("Close");
            cl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
            bar.add(file);
            _leditor.setJMenuBar(bar);

            _leditor.setVisible(true);

            _leditor.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    _lehack.setText("Show language editor");
                    _leditor = null;
                    _langEditor.disconnect();
                }

                public void windowClosing(WindowEvent e) {
                    _lehack.setText("Show language editor");
                    _leditor = null;
                }
            });
        }
        _lehack.setText(_leditor!=null?"Hide language editor":"Show language editor");
    }

    /*
    private static void runCA(Pattern2 p, CA2 ca, int size, int colors, int w, int h) {
        ca.setCell(w/2, 0, 1);
        int[] row = new int[2+w];

        int[] prev = new int[2*size+1];

        int[] pow = new int[p.length()];
        for(int i=0;i<pow.length;i++) {
            pow[pow.length-1-i] = (int) Math.pow(colors, i);
        }

        byte[] pattern = new byte[prev.length];
        System.err.println("model: "+ca.getRaster().getSampleModel());
        int[] first = new int[w];
        ca.getBlock(first, 0, 0, w, 1);
        //for(int i=0;i<first.length;i++) {
            //System.err.println(first[i]+" ");
        //}
        //System.err.println();
        for(int i=1;i<h;i++) {
            //System.err.println(i);
            //ca.getBlock(row, j-size, i-1, prev.length, 1);
            for(int j=0;j<w;j++) {
                ca.getBlock(prev, j-size, i-1, prev.length, 1);
                int idx = 0;
                for(int k=0;k<prev.length;k++) {
                    pattern[k] = (byte) (prev[k]);
                    //idx += prev[k] * pow[pow.length - 1 - k];
                    idx += prev[k] * pow[k];
                }
                //System.err.print(i+","+j+" idx="+idx+", p="+formatPattern(pattern)+" => "+p.next(idx));
                //System.err.println();
                ca.setCell(j, i, p.next(idx));
            }
        }
    }
    */

    private static String formatPattern(byte... ps) {
        StringBuilder b = new StringBuilder();
        for(byte p:ps) {
            b.append((char) (p+'0'));
        }
        return b.toString();
    }
}
