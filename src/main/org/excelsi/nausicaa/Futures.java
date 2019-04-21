package org.excelsi.nausicaa;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.awt.event.KeyAdapter;
import java.awt.event.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.excelsi.nausicaa.ca.*;


public class Futures extends JComponent implements ConfigListener, PlaneDisplayProvider, PlanescapeProvider {
    private static final Logger LOG = LoggerFactory.getLogger(Futures.class);
    //private int _w, _h;
    private boolean _show = true;
    //private java.util.List<Branch<World>> _timeline = new LinkedList<Branch<World>>();
    private PlaneDisplay[] _displays;
    private int _current = -1;
    private float _scale = 1.0f;
    //private Rule.Initialization _lastInit;
    private Initializer _lastInit;
    private Random _random;
    private final String _name;
    private long _seed = 8;
    private Config _config;
    private Timeline _timeline;
    private ViewType _viewType = ViewType.view2d;
    private CA _ca;


    //public Futures(int w, int h, Branch<World> b) {
    public Futures(Config config, Timeline timeline, CA ca, Random rand, String name) {
        _config = config;
        _timeline = timeline;
        _config.addListener(this);
        //_w = w;
        //_h = h;
        _random = rand;
        _name = name;
        setBackground(java.awt.Color.BLACK);
        setForeground(java.awt.Color.BLACK);
        _ca = ca;
        //addBranch(b);
        reinit();
        addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if(!_show||e.getModifiers()!=0) {
                    return;
                }
                int n = -1;
                switch(e.getKeyChar()) {
                    case 'y':
                        n = 0;
                        break;
                    case 'k':
                        n = 1;
                        break;
                    case 'u':
                        n = 2;
                        break;
                    case 'h':
                        n = 3;
                        break;
                    case '.':
                        n = 4;
                        break;
                    case 'l':
                        n = 5;
                        break;
                    case 'b':
                        n = 6;
                        break;
                    case 'j':
                        n = 7;
                        break;
                    case 'n':
                        n = 8;
                        break;
                }
                PlaneDisplay d = _displays[n];
                tick(d);
            }
        });
    }

    @Override
    public void configChanged(Config c, String p) {
        switch(p) {
            case "composite_mode":
            case "mutator":
            case "size":
                reinit();
                _timeline.notifyListeners(new TimelineEvent("futures"));
                break;
            case "scale":
                setScale(c.getScale());
                break;
            case "seed":
                _ca.reseed(c.getSeed());
                reroll(_lastInit);
                break;
        }
    }

    @Override
    public PlaneDisplay[] getDisplays() {
        return _displays;
    }

    @Override public Planescape[] getPlanescapes() {
        PlaneDisplay[] planes = getDisplays();
        Planescape[] ps = new Planescape[planes.length];
        for(int i=0;i<planes.length;i++) {
            ps[i] = planes[i];
        }
        return ps;
    }

    @Override public Plane getActivePlane() {
        return getDisplays()[_show?4:0].getPlane();
    }

    @Override public PlaneDisplay getActivePlaneDisplay() {
        return getDisplays()[_show?4:0];
    }

    public void setViewType(ViewType viewType) {
        if(viewType!=_viewType) {
            _viewType = viewType;
            reinit();
            _timeline.notifyListeners(new TimelineEvent("futures"));
        }
    }

    public ViewType getViewType() {
        return _viewType;
    }

    public void toggleEditor() {
        /*
        if(_editor!=null) {
            _editor.setVisible(false);
            _editor = null;
        }
        else {
            _editor = new JFrame("Editor");
            _editor.getContentPane().add(new CAEditor(this));
            _editor.pack();
            _editor.setSize(_editor.getContentPane().getPreferredSize());
            _editor.setVisible(true);
        }
        */
    }

    public void setCA(CA ca) {
        final int width = getCAWidth();
        final int height = getCAHeight();
        _ca = ca.size(width, height, getCADepth(), getCAPrelude());
        _lastInit = _ca.getInitializer();
        NViewer.getUIActions().doWait(new Runnable() {
            public void run() {
                reroll(_lastInit);
                //_timeline.notifyListeners(new TimelineEvent("tock"));
            }
        }, _show?0:500);
    }

    public void pickRandom() {
        if(_displays.length==1) {
            tick(_displays[0], true);
        }
        else {
            tick(_displays[_random.nextInt(_displays.length)]);
        }
    }

    public void tick(Rule r) {
        //Branch<World> child = getBranch().grow(new World(r, World.getSize(), World.getSize()), "");
        //addBranch(child);
        //_timeline.notifyListeners(new TimelineEvent("tick"));
        _ca = mutate(_ca);
        NViewer.getUIActions().doWait(new Runnable() {
            public void run() {
                reroll(_lastInit);
                //_timeline.notifyListeners(new TimelineEvent("tock"));
            }
        }, _show?0:500);
    }

    private void tick(final PlaneDisplay d) {
        tick(d, false);
    }

    private void tick(final PlaneDisplay d, boolean mutate) {
        tick(d, false, false);
    }

    private void tick(final PlaneDisplay d, final boolean mutate, final boolean branch) {
        //Branch<World> child = getBranch().grow(new World(d.getRule(), World.getSize(), World.getSize()), "");
        //addBranch(child);
        //_timeline.notifyListeners(new TimelineEvent("tick"));
        Worker.instance().push(new Runnable() {
            public void run() {
                for(int i=0;i<4;i++) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            d.toggleShow();
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch(InterruptedException e) {
                    }
                }
                if(branch) {
                    NViewer.getUIActions().branch(d.getCA());
                }
                else {
                    NViewer.getUIActions().doWait(new Runnable() {
                        public void run() {
                            if(mutate) {
                                _ca = mutate(d.getCA());
                            }
                            else {
                                _ca = d.getCA();
                            }
                            _config.setWeight(_ca.getWeight());
                            reroll(_lastInit);
                            //_timeline.notifyListeners(new TimelineEvent("tock"));
                        }
                    }, _show?0:500);
                }
            }
        });
    }

    private MutationFactor createMutationFactor(CA ca) {
        //return MutationFactor.defaultFactor().withAlpha(Integer.parseInt(_config.getVariable("mutator_alpha", "20")));
        MutationFactor mf = Actions.createMutationFactor(ca, _config, _random);
        return mf;
    }

    public CA mutate(CA ca) {
        final CA res = new MultiTransform(_random, createMutationFactor(ca), _config.getForceSymmetry()?new Symmetry(true):null)
            .ruleVariations(_config.getRuleVariations())
            .hueVariations(_config.getHueVariations())
            .weightVariations(_config.getWeightVariations())
            .paramVariations(_config.getParamVariations())
            .transform(ca);
        return res;
    }

    public int getCAWidth() {
        //return _w;
        return _config.getWidth();
    }

    public int getCAHeight() {
        //return _h;
        return _config.getHeight();
    }

    public int getCADepth() {
        return _config.getDepth();
    }

    public int getCAPrelude() {
        return _config.getPrelude();
    }

    public double getCAWeight() {
        return _config.getWeight();
    }

    public float getFrameWeight() {
        return _config.getFloatVariable("weight", 1f);
    }

    /*
    public void setCASize(int w, int h) {
        if(w!=_w||h!=_h) {
            _w = w;
            _h = h;
            Worker.instance().push(new Runnable() {
                public void run() {
                    NViewer.getUIActions().doWait(new Runnable() {
                        public void run() {
                            reinit();
                        }
                    }, 1000);
                }
            });
        }
    }
    */

    public void setScale(final float scale) {
        if(_scale!=scale) {
            _scale = scale;
            Worker.instance().push(new Runnable() {
                public void run() {
                    NViewer.getUIActions().doWait(new Runnable() {
                        public void run() {
                            for(PlaneDisplay d:_displays) {
                                d.setScale(scale);
                            }
                        }
                    }, 500);
                }
            });
        }
    }

    public PlaneDisplay getMainDisplay() {
        return _displays.length==1?_displays[0]:_displays[4];
    }

    public float getScale() {
        return _scale;
    }

    public void reinit() {
        if(_displays!=null) {
            for(PlaneDisplay d:_displays) {
                if(d!=null) {
                    for(MouseListener m:d.getMouseListeners()) {
                        d.removeMouseListener(m);
                    }
                }
            }
        }
        removeAll();
        setBackground(java.awt.Color.BLACK);
        setForeground(java.awt.Color.BLACK);
        JComponent futures = this;
        JSplitPane split = null;
        final int edsize = 100;
        if(false) {
            setLayout(new BorderLayout());
            futures = new JPanel();
            split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
            split.setOneTouchExpandable(true);
            split.setRightComponent(futures);
            add(split, BorderLayout.CENTER);
        }
        _lastInit = _ca.getInitializer();
        if(_show) {
            futures.setLayout(new GridLayout(3, 3, 0, 0));
            _displays = new PlaneDisplay[9];
            int width = getCAWidth() > 60 ? getCAWidth()/3-10 : getCAWidth();
            int height = getCAHeight() > 60 ? getCAHeight()/3-10 : getCAHeight();
            int depth = _config.getDepth();
            int prelude = _config.getPrelude();
            //float weight = _config.getWeight();
            double weight = _ca.getWeight();
            //WHAT
            _ca = _ca.size(width, height, depth, prelude).weight(weight);
            PlaneDisplay root = createPlaneDisplay(_ca);
            //PlaneDisplay root = createPlaneDisplay(_ca.size(width, height, depth, prelude));
            root.setScale(_scale);
            for(int i=0;i<_displays.length;i++) {
                if(i==4) {
                    _displays[i] = root;
                }
                else {
                    PlaneDisplay d = createPlaneDisplay(mutate(_ca).size(width, height, depth, prelude));
                    d.setScale(_scale);
                    _displays[i] = d;
                }
                futures.add(_displays[i]);
                final PlaneDisplay td = _displays[i];
                _displays[i].getDisplayComponent().addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        tick(td, false, e.isMetaDown());
                    }
                });
            }
        }
        else {
            futures.setLayout(new GridLayout(1, 1));
            _displays = new PlaneDisplay[1];
            final int width = getCAWidth();
            final int height = getCAHeight();
            _ca = _ca.size(width, height, _config.getDepth(), _config.getPrelude()).weight(_config.getWeight());
            PlaneDisplay d = createPlaneDisplay(_ca);
            //PlaneDisplay d = createPlaneDisplay(_ca.size(width, height, _config.getDepth(), _config.getPrelude()));
            d.setScale(_scale);
            futures.add(d);
            _displays[0] = d;
        }
        revalidate();
    }

    private PlaneDisplay createPlaneDisplay(final CA ca) {
        GOptions g = new GOptions(true, 1, 0, ca.getWeight())
            .computeMode(ComputeMode.from(_config.<String>getVariable("rgb_computemode","combined")));
        switch(_viewType) {
            case view3d:
                LOG.debug("creating 3d view");
                return new JfxPlaneDisplay(ca, g);
            case view2d:
            default:
                LOG.debug("creating 2d view");
                return new SwingPlaneDisplay(ca, g, _config);
        }
    }

    /*
    private Rule createMutation(Rule root) {
        System.err.println("orig: "+root);
        Rule m = root.origin().random(_random).next();
        System.err.println("mut: "+m);
        return m;
    }
    */

    public void setShow(boolean show) {
        if(_show!=show) {
            _show = show;
            _timeline.notifyListeners(new TimelineEvent("tick"));
            reinit();
            _timeline.notifyListeners(new TimelineEvent("tock"));
        }
    }

    public boolean getShow() {
        return _show;
    }

    public CA getCA() {
        return _ca;
    }

    public Rule getRule() {
        return _ca.getRule();
    }

    public Rule compileRule() {
        return _ca.compileRule();
    }

    public void reroll(final Initializer init) {
        generate(init, true);
    }

    private static long _tid = 0;
    public void generate(final Initializer init, final boolean reroll) {
        _timeline.notifyListeners(new TimelineEvent("tick"));
        java.util.List<Thread> threads = new ArrayList<Thread>();
        _lastInit = init;
        //final ExecutorService pool = Pools.named("compute", 3);
        final ExecutorService pool = Pools.prelude();
        final boolean useHistory = _config.getBooleanVariable("mutator_neverrepeat",false);
        final GOptions opt = new GOptions(true, _show?1:Pools.preludeSize(), 0, _ca.getWeight() /*getFrameWeight()*/);
        if(_show) {
            int width = getCAWidth() > 60 ? getCAWidth()/3-10 : getCAWidth();
            int height = getCAHeight() > 60 ? getCAHeight()/3-10 : getCAHeight();
            final CA ca = _ca.size(width,height,getCADepth(),getCAPrelude());
            //final Vector<Long> created = new Vector<Long>();
            Thread tdm = new Thread() {
                public void run() {
                    _displays[4].setCA(ca, pool, opt);
                }
            };
            threads.add(tdm);
            tdm.setName("Futures-4-"+(_tid++));
            tdm.start();
            final int[] order = {2, 5, 8, 7, 6, 3, 0, 1, 4};
            for(int i=0;i<_displays.length;i++) {
                if(order[i]!=4) {
                    final int ix = i;
                    Thread td = new Thread() {
                        public void run() {
                            CA nca;
                            do {
                                //System.err.print(".");
                                nca = mutate(ca);
                            } while(useHistory && History.named(_name).contains(nca));
                            System.err.println();
                            if(reroll) {
                                nca = nca.seed();
                            }
                            if(useHistory) {
                                History.named(_name).push(nca);
                            }
                            _displays[order[ix]].setCA(nca, pool, opt);
                        }
                    };
                    threads.add(td);
                    td.setName("Futures-"+ix+"-"+(_tid++));
                    td.start();
                }
            }
        }
        else {
            int width = getCAWidth();
            int height = getCAHeight();
            final CA ca = _ca.size(width,height,getCADepth(),getCAPrelude());
            _ca = ca;
            _displays[0].setCA(_ca, pool, opt);
            if(reroll) {
                _displays[0].reroll(init);
            }
            else {
                _displays[0].generate(init);
            }
        }
        for(Thread t:threads) {
            try {
                t.join();
            }
            catch(InterruptedException e) {
                break;
            }
        }
        _timeline.notifyListeners(new TimelineEvent("tock"));
    }
}
