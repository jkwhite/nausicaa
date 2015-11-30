package org.excelsi.nausicaa;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;

import org.excelsi.nausicaa.ca.*;


public class Futures extends JComponent implements ConfigListener, PlaneDisplayProvider {
    //private int _w, _h;
    private boolean _show = true;
    //private java.util.List<Branch<World>> _timeline = new LinkedList<Branch<World>>();
    private PlaneDisplay[] _displays;
    private int _current = -1;
    private float _scale = 1.0f;
    //private Rule.Initialization _lastInit;
    private Initializer _lastInit;
    private Random _random;
    private long _seed = 8;
    private Config _config;
    private Timeline _timeline;
    private CA _ca;


    //public Futures(int w, int h, Branch<World> b) {
    public Futures(Config config, Timeline timeline, CA ca, Random rand) {
        _config = config;
        _timeline = timeline;
        _config.addListener(this);
        //_w = w;
        //_h = h;
        _random = rand;
        setBackground(java.awt.Color.BLACK);
        setForeground(java.awt.Color.BLACK);
        _ca = ca;
        //addBranch(b);
        reinit();
        addKeyListener(new KeyAdapter() {
            public void keyTyped( KeyEvent e ) {
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

    @Override public Plane getActivePlane() {
        return getDisplays()[_show?4:0].getPlane();
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
        System.err.println("setting ca "+ca);
        final int width = getCAWidth();
        final int height = getCAHeight();
        _ca = ca.size(width, height);
        _lastInit = _ca.getInitializer();
        NViewer.getUIActions().doWait(new Runnable() {
            public void run() {
                reroll(_lastInit);
                //_timeline.notifyListeners(new TimelineEvent("tock"));
            }
        }, _show?0:500);
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
                NViewer.getUIActions().doWait(new Runnable() {
                    public void run() {
                        //_ca = mutate(_ca);
                        _ca = d.getCA();
                        reroll(_lastInit);
                        //_timeline.notifyListeners(new TimelineEvent("tock"));
                    }
                }, _show?0:500);
            }
        });
    }

    public CA mutate(CA ca) {
        return new MultiTransform(_random, _config.getForceSymmetry()?new Symmetry(true):null).transform(ca);
        //return ca.mutate(createMutation(ca.getRule()));
        /*
        Transform t;
        switch(_random.nextInt(2)) {
            case 1:
                t = new RuleTransform();
                break;
            default:
            case 0:
                t = new HueTransform();
                break;
        }
        return t.transform(ca);
        */
    }

    public int getCAWidth() {
        //return _w;
        return _config.getWidth();
    }

    public int getCAHeight() {
        //return _h;
        return _config.getHeight();
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
            int width = getCAWidth()/3-10;
            int height = getCAHeight()/3-10;
            //PlaneDisplay root = new PlaneDisplay(width, height, getBranch());
            PlaneDisplay root = new PlaneDisplay(_ca.size(width, height));
            root.setScale(_scale);
            for(int i=0;i<_displays.length;i++) {
                if(i==4) {
                    _displays[i] = root;
                }
                else {
                    //PlaneDisplay d = new PlaneDisplay(width, height, createMutation(root.getRule()));
                    //PlaneDisplay d = new PlaneDisplay(_ca.mutate(createMutation(root.getRule())).size(width, height));
                    PlaneDisplay d = new PlaneDisplay(mutate(_ca).size(width, height));
                    d.setScale(_scale);
                    _displays[i] = d;
                }
                futures.add(_displays[i]);
                final PlaneDisplay td = _displays[i];
                _displays[i].getLabel().addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        tick(td);
                    }
                });
            }
        }
        else {
            futures.setLayout(new GridLayout(1, 1));
            _displays = new PlaneDisplay[1];
            final int width = getCAWidth();
            final int height = getCAHeight();
            //System.err.println("BRANCH: "+getBranch());
            //PlaneDisplay d = new PlaneDisplay(width, height, getBranch());
            PlaneDisplay d = new PlaneDisplay(_ca.size(width, height));
            d.setScale(_scale);
            futures.add(d);
            _displays[0] = d;
        }
        revalidate();
    }

    private Rule createMutation(Rule root) {
        /*
        while(true) {
            try {
                long start = System.currentTimeMillis();
                Mutator mu = Viewer.createMutator(Rand.om);
                Rule m = root.mutate(mu);
                System.err.println(mu.name()+" took "+(System.currentTimeMillis()-start)+" msec");
                return m;
            }
            catch(MutationFailedException e) {
                System.err.println(e.getMessage());
            }
        }
        */
        System.err.println("orig: "+root);
        Rule m = root.origin().random(_random).next();
        System.err.println("mut: "+m);
        return m;
    }

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

    //public Branch<World> getBranch() {
        //return _current>=0?_timeline.get(_current):null;
    //}
//
    //public boolean hasPrevious() {
        //return _current > 0;
    //}
//
    //public boolean hasNext() {
        //return _current < _timeline.size() - 1;
    //}
//
    //public void setCurrent(int current) {
        //_current = current;
    //}
//
    //public int getCurrent() {
        //return _current;
    //}

    public CA getCA() {
        //return _displays[0].getCA();
        return _ca;
    }

    /*
    public void addBranch(Branch<World> b) {
        if(_current>=0&&_current<_timeline.size()-1) {
            _timeline = _timeline.subList(0, _current+1);
        }
        _timeline.add(b);
        _current = _timeline.size() - 1;
    }
    */

    public Rule getRule() {
        //return getBranch().data().getRule();
        return _ca.getRule();
    }

    //public void reroll(final Rule.Initialization init) {
        //generate(init, true);
    //}
//
    //public void generate(final Rule.Initialization init) {
        //generate(init, true);
    //}

    public void reroll(final Initializer init) {
        generate(init, true);
    }

    public void generate(final Initializer init, final boolean reroll) {
        //NViewer.getUIActions().notifyFutureChanging(Futures.this);
        _timeline.notifyListeners(new TimelineEvent("tick"));
        java.util.List<Thread> threads = new ArrayList<Thread>();
        _lastInit = init;
        if(_show) {
            int width = getCAWidth()/3-10;
            int height = getCAHeight()/3-10;
            final CA ca = _ca.size(width,height);
            final Vector<Long> created = new Vector<Long>();
            //_displays[4].setBranch(getBranch());
            //created.add(_displays[4].getRuleSeed());
            Thread tdm = new Thread() {
                public void run() {
                    _displays[4].setCA(ca);
                    //if(reroll) {
                        //_displays[4].reroll(init);
                    //}
                    //else {
                        //_displays[4].generate(init);
                    //}
                }
            };
            threads.add(tdm);
            tdm.start();
            //_displays[4].generate(init);
            final int[] order = {2, 5, 8, 7, 6, 3, 0, 1, 4};
            for(int i=0;i<_displays.length;i++) {
                if(order[i]!=4) {
                    /*
                    int tries = 0;
                    Rule temp = getRule().copy();
                    do {
                        _displays[order[i]].setRule(createMutation(temp));
                        tries++;
                    } while(tries<100&&created.contains(_displays[order[i]].getRuleSeed()));
                    */
                    //created.add(_displays[order[i]].getRuleSeed());
                    final int ix = i;
                    Thread td = new Thread() {
                        public void run() {
                            CA nca = mutate(ca);
                            if(reroll) {
                                //_displays[order[ix]].reroll(init);
                                nca = nca.seed();
                            }
                            else {
                                //_displays[order[ix]].generate(init);
                            }
                            _displays[order[ix]].setCA(nca);
                        }
                    };
                    threads.add(td);
                    td.start();
                }
            }
        }
        else {
            //_displays[0].setBranch(getBranch());
            int width = getCAWidth();
            int height = getCAHeight();
            final CA ca = _ca.size(width,height);
            _ca = ca;
            _displays[0].setCA(_ca);
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
        //Viewer.getInstance().notifyFutureChanged(Futures.this);
    }
}
