package org.excelsi.ca;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;


public class Futures extends JComponent {
    private int _w, _h;
    private boolean _show = true;
    private java.util.List<Branch<World>> _timeline = new LinkedList<Branch<World>>();
    private Display[] _displays;
    private int _current = -1;
    private float _scale = 1.0f;
    private Rule.Initialization _lastInit;


    public Futures(int w, int h, Branch<World> b) {
        _w = w;
        _h = h;
        setBackground(java.awt.Color.BLACK);
        setForeground(java.awt.Color.BLACK);
        addBranch(b);
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
                Display d = _displays[n];
                tick(d);
            }
        });
    }

    public Display[] getDisplays() {
        return _displays;
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

    public void tick(Rule r) {
        Branch<World> child = getBranch().grow(new World(r, World.getSize(), World.getSize()), "");
        addBranch(child);
        Viewer.getInstance().doWait(new Runnable() {
            public void run() {
                reroll(_lastInit);
            }
        }, _show?0:500);
    }

    private void tick(final Display d) {
        Branch<World> child = getBranch().grow(new World(d.getRule(), World.getSize(), World.getSize()), "");
        addBranch(child);
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
                Viewer.getInstance().doWait(new Runnable() {
                    public void run() {
                        reroll(_lastInit);
                    }
                }, _show?0:500);
            }
        });
    }

    public int getCAWidth() {
        return _w;
    }

    public int getCAHeight() {
        return _h;
    }

    public void setCASize(int w, int h) {
        if(w!=_w||h!=_h) {
            _w = w;
            _h = h;
            Worker.instance().push(new Runnable() {
                public void run() {
                    Viewer.getInstance().doWait(new Runnable() {
                        public void run() {
                            reinit();
                        }
                    }, 1000);
                }
            });
        }
    }

    public void setScale(final float scale) {
        if(_scale!=scale) {
            _scale = scale;
            Worker.instance().push(new Runnable() {
                public void run() {
                    Viewer.getInstance().doWait(new Runnable() {
                        public void run() {
                            for(Display d:_displays) {
                                d.setScale(scale);
                            }
                        }
                    }, 500);
                }
            });
        }
    }

    public Display getMainDisplay() {
        return _displays.length==1?_displays[0]:_displays[4];
    }

    public float getScale() {
        return _scale;
    }

    public void reinit() {
        if(_displays!=null) {
            for(Display d:_displays) {
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
        if(_show) {
            futures.setLayout(new GridLayout(3, 3, 0, 0));
            _displays = new Display[9];
            int width = _w/3-10;
            if(false) {
                width -= edsize/3;
            }
            int height = _h/3-10;
            Display root = new Display(width, height, getBranch());
            root.setScale(_scale);
            for(int i=0;i<_displays.length;i++) {
                if(i==4) {
                    _displays[i] = root;
                }
                else {
                    Display d = new Display(width, height, createMutation(root.getRule()));
                    d.setScale(_scale);
                    _displays[i] = d;
                }
                futures.add(_displays[i]);
                final Display td = _displays[i];
                _displays[i].getLabel().addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        tick(td);
                    }
                });
            }
        }
        else {
            futures.setLayout(new GridLayout(1, 1));
            _displays = new Display[1];
            int width = _w;
            int height = _h;
            if(false) {
               width -= edsize+20;
                height -= 20;
            }
            //System.err.println("BRANCH: "+getBranch());
            Display d = new Display(width, height, getBranch());
            d.setScale(_scale);
            futures.add(d);
            _displays[0] = d;
        }
    }

    private Rule createMutation(Rule root) {
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
    }

    public void setShow(boolean show) {
        if(_show!=show) {
            _show = show;
            reinit();
        }
    }

    public Branch<World> getBranch() {
        return _current>=0?_timeline.get(_current):null;
    }

    public boolean hasPrevious() {
        return _current > 0;
    }

    public boolean hasNext() {
        return _current < _timeline.size() - 1;
    }

    public void setCurrent(int current) {
        _current = current;
    }

    public int getCurrent() {
        return _current;
    }

    public CA getCA() {
        return _displays[0].getCA();
    }

    public void addBranch(Branch<World> b) {
        if(_current>=0&&_current<_timeline.size()-1) {
            _timeline = _timeline.subList(0, _current+1);
        }
        _timeline.add(b);
        _current = _timeline.size() - 1;
    }

    public Rule getRule() {
        return getBranch().data().getRule();
    }

    public void reroll(final Rule.Initialization init) {
        generate(init, true);
    }

    public void generate(final Rule.Initialization init) {
        generate(init, true);
    }

    public void generate(final Rule.Initialization init, final boolean reroll) {
        Viewer.getInstance().notifyFutureChanging(Futures.this);
        java.util.List<Thread> threads = new ArrayList<Thread>();
        _lastInit = init;
        if(_show) {
            final Vector<Long> created = new Vector<Long>();
            _displays[4].setBranch(getBranch());
            created.add(_displays[4].getRuleSeed());
            Thread tdm = new Thread() {
                public void run() {
                    if(reroll) {
                        _displays[4].reroll(init);
                    }
                    else {
                        _displays[4].generate(init);
                    }
                }
            };
            threads.add(tdm);
            tdm.start();
            //_displays[4].generate(init);
            final int[] order = {2, 5, 8, 7, 6, 3, 0, 1, 4};
            for(int i=0;i<_displays.length;i++) {
                if(order[i]!=4) {
                    int tries = 0;
                    Rule temp = getRule().copy();
                    do {
                        _displays[order[i]].setRule(createMutation(temp));
                        tries++;
                    } while(tries<100&&created.contains(_displays[order[i]].getRuleSeed()));
                    created.add(_displays[order[i]].getRuleSeed());
                    final int ix = i;
                    Thread td = new Thread() {
                        public void run() {
                            if(reroll) {
                                _displays[order[ix]].reroll(init);
                            }
                            else {
                                _displays[order[ix]].generate(init);
                            }
                        }
                    };
                    threads.add(td);
                    td.start();
                }
            }
        }
        else {
            _displays[0].setBranch(getBranch());
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
        Viewer.getInstance().notifyFutureChanged(Futures.this);
    }
}
