package org.excelsi.nausicaa;


import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.Random;
import org.excelsi.nausicaa.ca.*;


public class NViewer extends JFrame implements UIActions {
    private static int _width;
    private static int _height;
    private static JFrame _root;
    private JMenuItem _repeat;
    private Actions _a = new Actions();
    private static NViewer _instance;
    private Initializers _init = Initializers.random;
    private Initializer _initializer;
    private Futures _futures;
    private Config _config;
    private Timeline _timeline;
    private JFrame _peditor;
    private JFrame _reditor;
    private PaletteEditor _paletteEditor;
    private RuleEditor _ruleEditor;
    private JMenuItem _pehack;
    private JMenuItem _rehack;
    private Random _random;


    static {
        _width = Toolkit.getDefaultToolkit().getScreenSize().width;
        _height = Toolkit.getDefaultToolkit().getScreenSize().height;
    }

    public static UIActions getUIActions() {
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

    @Override
    public CA getActiveCA() {
        return _futures.getCA();
    }

    @Override public void setActiveCA(CA ca) {
        _futures.setCA(ca);
    }

    public void setInitializer(Initializer initializer) {
        _initializer = initializer;
        setActiveCA(getActiveCA().initializer(_initializer));
    }

    @Override
    public PlaneDisplayProvider getPlaneDisplayProvider() {
        return _futures;
    }

    @Override public Frame getRoot() {
        return this;
    }

    public void init() {
        //final int w = 600, h = 600, d = 1;
        final int w = 300, h = 300, d = 3, pre = 20;
        //final int w = 3, h = 3, d = 1;
        _config = new Config(w, h, d);
        createMenu();
        setSize(_width, _height);
        int dims = 3;
        int size = 1;
        int colors = 8;
        _timeline = new Timeline();
        org.excelsi.nausicaa.ca.Archetype a = new org.excelsi.nausicaa.ca.Archetype(dims, size, colors);
        org.excelsi.nausicaa.ca.Archetype a1 = new org.excelsi.nausicaa.ca.Archetype(1, size, colors);
        org.excelsi.nausicaa.ca.Archetype a2 = new org.excelsi.nausicaa.ca.Archetype(2, size, colors);
        Random rand = new Random();
        _random = rand;
        //Ruleset rs = new IndexedRuleset1d(a);
        //Ruleset rs = new IndexedRuleset1d(a1, new IndexedRuleset2d(a2));
        //Ruleset rs = new IndexedRuleset2d(a);
        Ruleset rs = new ComputedRuleset(a);
        //Ruleset rs = new IndexedRuleset1d(a1, new IndexedRuleset1d(a1));
        Rule rule = rs.random(rand).next();
        //Palette pal = Palette.random(colors, rand, true);
        //Palette pal = Palette.grey(colors);
        Palette pal = Palette.rainbow(colors,true);
        //pal = new Palette(Colors.pack(0,0,0,255), Colors.pack(255,255,255,255));
        org.excelsi.nausicaa.ca.CA ca = new org.excelsi.nausicaa.ca.CA(rule, pal, Initializers.random.create(), rand, 0, w, h, d, pre);

        JPanel main = new JPanel(new BorderLayout());

        Futures f = new Futures(_config, _timeline, ca, new Random());
        _futures = f;
        main.add(f, BorderLayout.CENTER);
        getContentPane().add(main);
        //d.setCA(ca);
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
        createPaletteMenu(shortcut, bar);
        createMutateMenu(shortcut, bar);
        createWindowMenu(shortcut, bar);
        root().setJMenuBar(bar);
    }

    private void createFileMenu(int shortcut, JMenuBar bar) {
        JMenu file = new JMenu("File");
        AbstractAction newCA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.newCA(NViewer.this);
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
                _a.close(NViewer.this);
            }
        };
        AbstractAction exportImg = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.exportImage(NViewer.this, _config);
            }
        };
        AbstractAction exportRule = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.exportRule(NViewer.this, _config);
            }
        };
        JMenuItem ni = file.add(newCA);
        ni.setText("New ...");
        ni.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
        JMenuItem oi = file.add(open);
        oi.setText("Open ...");
        oi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
        JMenuItem si = file.add(save);
        si.setText("Save");
        si.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
        JMenuItem cl = file.add(close);
        cl.setText("Close");
        cl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
        file.addSeparator();
        JMenuItem expImg = file.add(exportImg);
        expImg.setText("Export image...");
        JMenuItem expRule = file.add(exportRule);
        expRule.setText("Export rule...");
        //exp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
        bar.add(file);
    }

    private void createPaletteMenu(int shortcut, JMenuBar bar) {
        JMenu pal = new JMenu("Palette");

        AbstractAction greys = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.grey(getActiveCA().archetype().colors())));
            }
        };
        JMenuItem grey = pal.add(greys);
        grey.setText("Grayscale");

        AbstractAction rands = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.random(getActiveCA().archetype().colors(), getActiveCA().getRandom())));
            }
        };
        JMenuItem rand = pal.add(rands);
        rand.setText("Random");

        AbstractAction rains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.rainbow(getActiveCA().archetype().colors(), false)));
            }
        };
        JMenuItem rain = pal.add(rains);
        rain.setText("Rainbow");

        AbstractAction brains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.rainbow(getActiveCA().archetype().colors(), true)));
            }
        };
        JMenuItem brain = pal.add(brains);
        brain.setText("Black Rainbow");

        AbstractAction rrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getActiveCA().getRandom().nextBoolean();
                setActiveCA(getActiveCA().palette(Palette.randomRainbow(getActiveCA().getRandom(), getActiveCA().archetype().colors(), false, getActiveCA().archetype().colors()/2)));
            }
        };
        JMenuItem rrain = pal.add(rrains);
        rrain.setText("Spectrum");

        AbstractAction rdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getActiveCA().getRandom().nextBoolean();
                setActiveCA(getActiveCA().palette(Palette.randomRainbow(getActiveCA().getRandom(), getActiveCA().archetype().colors(), false, getActiveCA().archetype().colors())));
            }
        };
        JMenuItem rdrain = pal.add(rdrains);
        rdrain.setText("Dense Spectrum");

        AbstractAction srdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getActiveCA().getRandom().nextBoolean();
                setActiveCA(getActiveCA().palette(Palette.randomShinyRainbow(getActiveCA().getRandom(), getActiveCA().archetype().colors(), false, getActiveCA().archetype().colors())));
            }
        };
        JMenuItem srdrain = pal.add(srdrains);
        srdrain.setText("Sparkly Spectrum");

        AbstractAction wrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getActiveCA().getRandom().nextBoolean();
                setActiveCA(getActiveCA().palette(Palette.randomWrappedRainbow(getActiveCA().getRandom(), getActiveCA().archetype().colors(), false, getActiveCA().archetype().colors()/2)));
            }
        };
        JMenuItem wrain = pal.add(wrains);
        wrain.setText("Wrapped Spectrum");

        AbstractAction wbrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getActiveCA().getRandom().nextBoolean();
                setActiveCA(getActiveCA().palette(Palette.randomWrappedRainbow(getActiveCA().getRandom(), getActiveCA().archetype().colors(), true, getActiveCA().archetype().colors()/2)));
            }
        };
        JMenuItem wbrain = pal.add(wbrains);
        wbrain.setText("Wrapped Black Spectrum");

        AbstractAction wdrains = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getActiveCA().getRandom().nextBoolean();
                setActiveCA(getActiveCA().palette(Palette.randomWrappedRainbow(getActiveCA().getRandom(), getActiveCA().archetype().colors(), false, getActiveCA().archetype().colors())));
            }
        };
        JMenuItem wdrain = pal.add(wdrains);
        wdrain.setText("Dense Wrapped Spectrum");

        pal.addSeparator();

        AbstractAction reds = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.shades(getActiveCA().archetype().colors(), new int[]{255,0,0})));
            }
        };
        JMenuItem red = pal.add(reds);
        red.setText("Red shades");

        AbstractAction breds = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.shades(getActiveCA().archetype().colors(), new int[]{255,128,128})));
            }
        };
        JMenuItem bred = pal.add(breds);
        bred.setText("Bright red shades");

        AbstractAction blues = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.shades(getActiveCA().archetype().colors(), new int[]{0,0,255})));
            }
        };
        JMenuItem blue = pal.add(blues);
        blue.setText("Blue shades");

        AbstractAction bblues = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.shades(getActiveCA().archetype().colors(), new int[]{128,128,255})));
            }
        };
        JMenuItem bblue = pal.add(bblues);
        bblue.setText("Bright blue shades");

        AbstractAction greens = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.shades(getActiveCA().archetype().colors(), new int[]{0,255,0})));
            }
        };
        JMenuItem green = pal.add(greens);
        green.setText("Green shades");

        AbstractAction bgreens = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.shades(getActiveCA().archetype().colors(), new int[]{128,255,128})));
            }
        };
        JMenuItem bgreen = pal.add(bgreens);
        bgreen.setText("Bright green shades");

        AbstractAction sepia = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.rainbow(getActiveCA().archetype().colors(), false, new int[][]{ {34,27,1}, {84,49,13}, {112,66,20}, {165,139,36}, {196,145,35} })));
            }
        };
        JMenuItem isepia = pal.add(sepia);
        isepia.setText("Sepia");

        AbstractAction acbgreens = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setActiveCA(getActiveCA().palette(Palette.allShades(getActiveCA().archetype().colors(), new int[]{255,255,255})));
            }
        };
        JMenuItem acbgreen = pal.add(acbgreens);
        acbgreen.setText("All Creation");

        bar.add(pal);
    }

    private void createAutomataMenu(int shortcut, JMenuBar bar) {
        final JCheckBoxMenuItem[] hack = new JCheckBoxMenuItem[5];
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
                //_a.generate(NViewer.this);
            }
        });
        auto.add(ran);
        hack[0] = ran;
        ran.setText("Random initial state ...");
        ran.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcut));
        ran.setState(_init==Initializers.random);

        JCheckBoxMenuItem fix = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Initializers.single;
                _initializer = new SingleInitializer();
                setActiveCA(getActiveCA().initializer(_initializer));
                hack[0].setState(false);
                hack[1].setState(true);
                hack[2].setState(false);
                hack[3].setState(false);
                hack[4].setState(false);
                //_a.generate(NViewer.this);
            }
        });
        auto.add(fix);
        fix.setText("Fixed initial state");
        fix.setSelected(_init==Initializers.single);
        fix.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcut));
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
                //_a.generate(NViewer.this);
            }
        });
        auto.add(gau);
        hack[4] = gau;
        gau.setText("Gaussian initial state ...");
        gau.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcut));
        gau.setState(_init==Initializers.gaussian);

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

        //AbstractAction spl = new AbstractAction() {
            //public void actionPerformed(ActionEvent e) {
                //split();
            //}
        //};
        //JMenuItem split = auto.add(spl);
        //split.setText("Branch");
        //split.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcut));

        AbstractAction an = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.animate(NViewer.this, _timeline, -1);
            }
        };
        JMenuItem animate = auto.add(an);
        animate.setText("Animate");
        animate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcut));

        AbstractAction stp = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.animate(NViewer.this, _timeline, 2);
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

        AbstractAction size = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.resizeCA(NViewer.this);
            }
        };
        JMenuItem siz = auto.add(size);
        siz.setText("Set size ...");
        siz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcut));

        auto.addSeparator();

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

        //AbstractAction dimup = new AbstractAction() {
            //public void actionPerformed(ActionEvent e) {
                //open3d();
            //}
        //};
        //JMenuItem dimensions = auto.add(dimup);
        //dimensions.setText("View from higher dimension ...");
        //siz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcut));

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
        _repeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));

        AbstractAction rand = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.randomMutation(NViewer.this);
            }
        };
        final JMenuItem random = mutate.add(rand);
        random.setText("Random mutation");
        random.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcut));
        mutate.addSeparator();

        for(final Mutator m:MutatorFactory.defaultMutators().getAll()) {
            AbstractAction mut = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    _repeat.setEnabled(true);
                    _repeat.setText("Repeat "+m.name());
                    try {
                        _a.mutate(NViewer.this, _config, _random, m);
                    }
                    catch(MutationFailedException ex) {
                        System.err.println(m.name()+" failed: "+ex.getMessage());
                    }
                }
            };
            JMenuItem mutat = mutate.add(mut);
            mutat.setText(m.name());
        }
        mutate.addSeparator();

        final JCheckBoxMenuItem[] mhack = new JCheckBoxMenuItem[2];
        JCheckBoxMenuItem forceSym = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _config.setForceSymmetry(!_config.getForceSymmetry());
                mhack[0].setState(!mhack[0].getState());
                mhack[0].setSelected(!mhack[0].getState());
            }
        });
        mutate.add(forceSym);
        mhack[0] = forceSym;
        forceSym.setText("Force symmetry");
        forceSym.setState(_config.getForceSymmetry());
        forceSym.setSelected(_config.getForceSymmetry());

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

        bar.add(mutate);
    }

    private void createWindowMenu(int shortcut, JMenuBar bar) {
        JMenu window = new JMenu("Window");
        final JMenuItem[] fhack = new JMenuItem[1];
        final JMenuItem full = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                boolean cval = _futures!=null ? _futures.getShow() : false;
                final boolean nval = !cval;
                fhack[0].setText(nval?"Hide mutations":"Show mutations");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //_a.generate();
                        _futures.setShow(nval);
                        //_futures.revalidate();
                    }
                });
            }
        });
        window.add(full);
        fhack[0] = full;
        full.setText("Hide mutations");
        full.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut));

        final JMenuItem[] vhack = new JMenuItem[1];
        final JMenuItem view3d = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                final ViewType vt = _futures!=null ? _futures.getViewType() : ViewType.view2d;
                vhack[0].setText(vt==ViewType.view2d?"View in 2D":"View in 3D");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //_a.generate();
                        _futures.setViewType(vt==ViewType.view2d?ViewType.view3d:ViewType.view2d);
                        //_futures.revalidate();
                    }
                });
            }
        });
        window.add(view3d);
        vhack[0] = view3d;
        view3d.setText("View in 3D");
        //view3d.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut));

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

        final JMenuItem viewh = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _a.view3d(NViewer.this);
            }
        });
        viewh.setText("View from higher dimension");
        window.add(viewh);

        final JMenuItem formed = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleRuleEditor();
            }
        });
        _rehack = formed;
        formed.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcut));
        formed.setText("Show rule editor");
        window.add(formed);

        bar.add(window);
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

    private void toggleRuleEditor() {
        if(_reditor!=null) {
            _ruleEditor.disconnect();
            _ruleEditor = null;
            _reditor.setVisible(false);
            _reditor = null;
        }
        else {
            _reditor = new JFrame("Rule Editor");
            _ruleEditor = new RuleEditor(_reditor, this, _timeline);
            _reditor.getContentPane().add(_ruleEditor);
            _reditor.pack();
            Dimension dim = _reditor.getContentPane().getPreferredSize();
            _reditor.setSize(16+dim.width, 24+dim.height);

            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            JMenuBar bar = new JMenuBar();
            JMenu file = new JMenu("File");
            AbstractAction close = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    _reditor.setVisible(false);
                    _reditor = null;
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
