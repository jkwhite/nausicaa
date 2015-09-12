package org.excelsi.ca;


import java.awt.peer.ContainerPeer;
import java.awt.peer.ComponentPeer;
import org.redsails.graph.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jgraph.*;
import javax.imageio.ImageIO;
import org.jgraph.graph.VertexView;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.layout.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Comparator;
import java.io.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.awt.Dimension;
import javax.swing.plaf.basic.BasicToolTipUI;
import java.util.WeakHashMap;


public class Viewer extends JFrame {
//public class Viewer extends JInternalFrame {
    public static final String STORE = System.getProperty("user.home")+File.separator+".yggdrasil";
    public static final int ATTR = Happiness.STABLE_ATTRACTION;
    public static final int SPACING = 1;
    private static int _width;
    private static int _height;
    //private int _w;
    //private int _h;
    private int _lastWidth = 4096;
    private int _lastHeight = 4096;
    private int _lastFrames = 1000;
    private Mutator _lastMutator = null;
    private JMenuItem _repeat;
    private JMenuItem _selectLast, _selectNext;
    private JMenuItem _ehack;
    private JMenuItem _pehack;
    private static JTabbedPane _tabs;
    private static JFrame _root;
    private static JDesktopPane _desktop;
    private Rule.Initialization _init = Rule.Initialization.random;
    private boolean _forceSymmetry = false;
    private boolean _evaporate = true;
    private boolean _phasetrans = true;
    private boolean _stabilityreset = false;
    private boolean _futures = true;
    //private boolean _editor = false;
    private JFrame _editor;
    private JFrame _peditor;
    private File _dir = new File(System.getProperty("user.home"));
    private Yggdrasil _y;
    private CAGraph _cagraph;
    //private static Viewer.Worker _worker = new Viewer.Worker();
    private int _initial = 10;
    private java.util.List<ViewerListener> _listeners = new ArrayList<ViewerListener>();
    private BufferedImage _initImage;
    private static int _variations = 1;
    private static String _dimensions = "2";
    private static boolean _wrap = true;
    private static Viewer _instance;
    //static {
        //_worker.setDaemon(true);
        //_worker.start();
    //}

    static {
        _width = Toolkit.getDefaultToolkit().getScreenSize().width;
        _height = Toolkit.getDefaultToolkit().getScreenSize().height;
    }

    public static void main(String[] args) {
        int initial = 10;
        for(int i=0;i<args.length;i++) {
            if("-g".equals(args[i])) {
            }
            else if("-n".equals(args[i])) {
                initial = Integer.parseInt(args[++i]);
            }
            else if("-v".equals(args[i])) {
                _variations = Integer.parseInt(args[++i]);
            }
            else if("-d".equals(args[i])) {
                _dimensions = args[++i];
            }
            else if("-w".equals(args[i])) {
                _wrap = false;
            }
            else {
                System.err.println("unknown option '"+args[i]+"'");
                System.exit(1);
            }
        }

        /*
        JDesktopPane d = new JDesktopPane();
        _desktop = d;
        JFrame root = new JFrame("Root");
        root.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        root.getContentPane().add(d);
        root.setVisible(true);
        _root = root;
        */
        Viewer v = new Viewer();
        _instance = v;
        _root = v;
        v.setInitial(initial);
        v.init();
        v.setVisible(true);
        //d.add(v);
        //d.setSelected(true);
        //d.setVisible(true);

        //setSize(_width, _height);
    }

    public static JFrame root() {
        return _root;
    }

    public static JDesktopPane desktop() {
        return _desktop;
    }

    public static Viewer getInstance() {
        return _instance;
    }

    public Viewer() {
        super("Multiverse");
    }

    public void setInitial(int i) {
        _initial = i;
    }

    public void addViewerListener(ViewerListener l) {
        _listeners.add(l);
    }

    public void removeViewerListener(ViewerListener l) {
        _listeners.remove(l);
    }

    public Futures currentFuture() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            return (Futures) c;
        }
        return null;
    }

    public void notifyFutureChanged(Futures f) {
        for(ViewerListener l:new ArrayList<ViewerListener>(_listeners)) {
            l.futureChanged(f);
        }
    }

    public void notifyFutureChanging(Futures f) {
        for(ViewerListener l:new ArrayList<ViewerListener>(_listeners)) {
            l.futureChanging(f);
        }
    }

    public void init() {
        int initial = _initial;
        createMenu();
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        _tabs = tabs;
        getContentPane().add(tabs);
        _tabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                Component c = _tabs.getSelectedComponent();
                if(c instanceof Futures) {
                    Futures d = (Futures) c;
                    _selectLast.setEnabled(d.hasPrevious());
                    _selectNext.setEnabled(d.hasNext());
                    notifyFutureChanged(d);
                }
            }
        });
        EdgeView.renderer = new EdgeRenderer() {
            public void paint(Graphics g) {
                try {
                    super.paint(g);
                }
                catch(NullPointerException e) {
                }
            }
        };
        ArrayList<Ruleset> rules = new ArrayList<Ruleset>();
        Yggdrasil y;
        if("2".equals(_dimensions)) {
            rules.add(new Ruleset2D(new int[]{
                        CA.randomColor(),
                        CA.randomColor()}));
            y = Yggdrasil.root(null, new Rulespace2D((Ruleset2D[]) rules.toArray(new Ruleset2D[0])));
        }
        else if("1".equals(_dimensions)) {
            rules.add(new Ruleset1D(new int[]{
                        CA.randomColor(),
                        CA.randomColor()}));
            y = Yggdrasil.root(null, new Rulespace1D((Ruleset1D[]) rules.toArray(new Ruleset1D[0])));
        }
        else if("1s".equals(_dimensions)) {
            rules.add(new RulesetSquare(new int[]{
                        CA.randomColor(),
                        CA.randomColor()}, _wrap));
            y = Yggdrasil.root(null, new Rulespace1D((RulesetSquare[]) rules.toArray(new RulesetSquare[0])));
        }
        else {
            throw new IllegalStateException("unsupported dimensionality '"+_dimensions+"'");
        }
        /*
        if(new File(STORE).exists()) {
            y = Yggdrasil.revive(STORE);
        }
        else {
            rules.add(new Ruleset1D(new int[]{
                        CA.randomColor(),
                        CA.randomColor()}));
            y = Yggdrasil.root(STORE, new Rulespace1D((Ruleset1D[]) rules.toArray(new Ruleset1D[0])));
        }
        */
        _y = y;
        Branch<World> root = y.root();
        final Vertex darkling = root;
        root.setDefaultDimensions(new int[]{World.getSize(), World.getSize()});
        ArrayList<Branch<World>> all = new ArrayList<Branch<World>>();
        all.add(root);
        for(int i=0;i<initial;i++) {
            try {
                Mutator m = createMutator(Rand.om);
                Branch<World> child = root.grow(new World(root.data().getRule().mutate(m), World.getSize(), World.getSize()), m.toString());
                if(Rand.om.nextBoolean()) {
                    root = child;
                }
                else if(Rand.om.nextBoolean()) {
                    root = all.get(Rand.om.nextInt(all.size()));
                }
                all.add(child);
                root = all.get(Rand.om.nextInt(all.size()));
            }
            catch(Throwable t) {
            }
        }

        GraphModel g = root.meta();
        Bloom b = new Bloom();
        b.setRoot(darkling);
        b.setSpacing(World.getSize()*2*SPACING);
        b.setSpaceType(Bloom.Spacing.log);
        b.setUpdateView(true);
        final CAGraph gr = new CAGraph((DefaultVertex)g, b);
        _cagraph = gr;
        gr.setBackground(Color.BLACK);
        gr.setAntiAliased(true);
        gr.addKeyListener(new KeyAdapter() {
            public void keyTyped( KeyEvent e ) {
                switch(e.getKeyChar()) {
                    //case KeyEvent.VK_MINUS:
                        //gr.setScale(gr.getScale()/1.1);
                        //break;
                    //case KeyEvent.VK_PLUS:
                    //case '+':
                        //gr.setScale(gr.getScale()*1.1);
                        //break;
                    //case KeyEvent.VK_EQUALS:
                        //gr.setScale(1.0);
                        //break;
                    case 'a':
                        gr.arrange();
                        break;
                    //case 'e':
                        //gr.toggleSpacing();
                        //break;
                    //case 'g':
                        //gr.grow();
                        //break;
                    case ' ':
                        gr.setSelectionCell(gr.getTarget());
                        break;
                    //case 'm':
                        //new Thread() {
                            //public void run() {
                                //gr.mutateSelected(createMutator());
                            //}
                        //}.start();
                        //break;
                    case '\t':
                    case 'n':
                        Rectangle rect = ((JViewport)gr.getParent()).getViewRect();
                        Object[] roots = gr.getRoots();
                        CellView[] views = gr.getGraphLayoutCache().getMapping(roots, true);
                        LinkedList rot = new LinkedList();
                        for(int i=0;i<roots.length;i++) {
                            if(!(roots[i] instanceof Branch)) {
                                continue;
                            }
                            Rectangle2D r = views[i].getBounds();
                            double sc = gr.getScale();
                            if(r.getX()+r.getWidth()*sc>=rect.getX()&&r.getX()*sc<=rect.getX()+rect.getWidth()
                                && r.getY()+r.getHeight()*sc>=rect.getY()&&r.getY()*sc<=rect.getY()+rect.getHeight()) {
                                rot.add(roots[i]);
                            }
                        }
                        Object[] selec = gr.getSelectionCells();
                        for(Object obj:selec) {
                            Branch<World> wor = (Branch<World>) obj;
                            for(Object adj:wor.adjacentVertices()) {
                                if(!rot.contains(adj)) {
                                    rot.add(adj);
                                }
                            }
                        }
                        gr.nextTarget(rot.toArray());
                        break;
                    //case 'o':
                        //newTab();
                        //break;
                    case 'p':
                        Object[] sels = gr.getSelectionCells();
                        for(Object o:sels) {
                            System.err.println(o+" => "+gr.getCellBounds(o));
                        }
                        break;
                    //case 'r':
                        //gr.regenerateSelected();
                        //break;
                    //case 'z':
                        //Object[] sele = gr.getSelectionCells();
                        //gr.setScale(1);
                        //gr.scrollCellToVisible(sele[0]);
                        //break;
                }
            }
        } );
        JScrollPane ma = new JScrollPane(gr);
        //f.getContentPane().add(new JScrollPane(gr));
        tabs.add("Yggdrasil", ma);
        setSize(new java.awt.Dimension(800,800));
        b.run(gr, g.vertices().toArray(), 20); 
        b.updateView();
        Rectangle2D r = gr.getCellBounds(gr.getRoots());
        setSize(new java.awt.Dimension(Math.min(Toolkit.getDefaultToolkit().getScreenSize().width,100+(int)r.getWidth()), Math.min(Toolkit.getDefaultToolkit().getScreenSize().height,100+(int)r.getHeight())));
        setSize(_width, _height);
        setVisible(true);
        gr.requestFocus();
        b.updateView();
    }

    private static JComponent createRuleText(String str) {
        JTextArea a = new JTextArea(str, Math.max(1,Math.min(7,str.length()/80)), 80);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        Font f = a.getFont();
        a.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
        return new JScrollPane(a);
    }

    public boolean toggleEvaporate() {
        _evaporate = !_evaporate;
        return _evaporate;
    }

    public boolean togglePhaseTransition() {
        _phasetrans = !_phasetrans;
        return _phasetrans;
    }

    public boolean toggleStabilityReset() {
        _stabilityreset = !_stabilityreset;
        return _stabilityreset;
    }

    public boolean getEvaporation() {
        return _evaporate;
    }

    public boolean getPhaseTransition() {
        return _phasetrans;
    }

    public boolean getStabilityReset() {
        return _stabilityreset;
    }

    private static JComponent createColorPanel(int[] cols) {
        JPanel colors = new JPanel();
        colors.setAlignmentY(0);
        colors.add(new JLabel(""+cols.length+": "));
        for(int col:cols) {
            colors.add(new CAEditor.Cell(col));
            colors.add(new JLabel(CA.toColorString(col)));
        }
        return colors;
    }

    private void info() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            Futures d = (Futures) c;
            Branch<World> w = d.getBranch();
            Rule r = w.data().getRule();
            final JFrame i = new JFrame("Info");
            //i.getContentPane().setLayout(new GridLayout(1, 2));
            //i.getContentPane().setLayout(new BoxLayout(i.getContentPane(), BoxLayout.X_AXIS));
            //int count = 0;
            InfoPanel p = new InfoPanel();
            p.addPair("Universe", chop(r.toString(),66));
            p.addPair("Incantation", createRuleText(r.toIncantation()));
            p.addPair("Colors", createColorPanel(r.colors()));
            //count = pair(count, i.getContentPane(), "Rule", chop(rstr,16));
            //count = pair(count, i.getContentPane(), "Colors", r.colors().length);
            if(r instanceof Multirule) {
                Rule[] chs = ((Multirule)r).rules();
                if(chs.length>1) {
                    //count = pair(count, i.getContentPane(), "Rule count", chs.length);
                    for(Rule ch:((Multirule)r).rules()) {
                        p.addPair(" ", " ");
                        p.addPair("  Rule", chop(ch.toString(),66));
                        p.addPair("  Verse", createRuleText(ch.toIncantation()));
                        p.addPair("  Colors", createColorPanel(ch.colors()));
                        //p.addPair("  Colors", ch.colors().length);
                    }
                }
            }
            //i.getContentPane().setLayout(new GridLayout(count, 2));
            //i.pack();
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
        else {
        }
    }

    /*
    private void info() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            Futures d = (Futures) c;
            Branch<World> w = d.getBranch();
            Rule r = w.data().getRule();
            JFrame i = new JFrame("Info");
            //i.getContentPane().setLayout(new GridLayout(1, 2));
            i.getContentPane().setLayout(new BoxLayout(i.getContentPane(), BoxLayout.X_AXIS));
            int count = 0;
            String rstr = r.toString();
            count = pair(count, i.getContentPane(), "Rule", chop(rstr,16));
            count = pair(count, i.getContentPane(), "Colors", r.colors().length);
            if(r instanceof Multirule) {
                Rule[] chs = ((Multirule)r).rules();
                count = pair(count, i.getContentPane(), "Rule count", chs.length);
                for(Rule ch:((Multirule)r).rules()) {
                    count = pair(count, i.getContentPane(), "  Rule", chop(ch.toString(), 16));
                    count = pair(count, i.getContentPane(), "  Colors", r.colors().length);
                }
            }
            i.getContentPane().setLayout(new GridLayout(count, 2));
            i.pack();
            Things.centerWindow(i);
            i.setVisible(true);
        }
        else {
        }
    }
    */

    private static String chop(String s, int max) {
        return (s.length()>max?s.substring(0,max)+"...":s)+" ("+s.length()+" bytes)";
    }

    private static int pair(int count, java.awt.Container c, String field, Object value) {
        JLabel l1 = new JLabel(field);
        JLabel l2 = new JLabel(value.toString());
        c.add(l1);
        c.add(l2);
        return ++count;
    }

    private Futures branch(Branch<World> w) {
        Rule r = w.data().getRule();
        Futures last = new Futures(_width-32, _height-96, w);
        String rulestr = r.toString();
        CA icon = new CA(16, 16);
        initCA(icon, r);
        r.generate(icon, 1, 15, false, true, null);
        String title = rulestr;
        if(title.length()>12) {
            title = title.substring(0,9)+"...";
        }
        //_tabs.addTab("", new ImageIcon(icon.toImage()), last, rulestr);
        _tabs.addTab("", createIcon(r), last, rulestr);
        generate(last);
        return last;
    }

    private void newTab() {
        Object[] sel = _cagraph.getSelectionCells();
        //Display last = null;
        Futures last = null;
        for(Object o:sel) {
            if(o instanceof Branch) {
                Branch<World> w = (Branch<World>) o;
                Rule r = w.data().getRule();
                last = branch(w);
                /*
                last = new Futures(_width-32, _height-96, w);
                String rulestr = r.toString();
                CA icon = new CA(16, 16);
                initCA(icon, r);
                r.generate(icon, 1, 15, false, null);
                String title = rulestr;
                if(title.length()>12) {
                    title = title.substring(0,9)+"...";
                }
                //_tabs.addTab("", new ImageIcon(icon.toImage()), last, rulestr);
                _tabs.addTab("", createIcon(r), last, rulestr);
                generate(last);
                */
            }
        }
        if(last!=null) {
            _tabs.setSelectedComponent(last);
        }
    }

    private ImageIcon createIcon(Rule r) {
        CA icon = new CA(16, 16);
        initCA(icon, r);
        r.generate(icon, 1, 15, false, false, null);
        return new ImageIcon(icon.toImage());
    }

    private Map<Object,Animation> _animators = new WeakHashMap<Object,Animation>();
    public void animate(Futures f, int steps) {
        Animation a = new Animation(f, this, steps);
        a.start();
        _animators.put(f, a);
    }

    public void deanimate(Futures f) {
        Animation an = _animators.get(f);
        if(an!=null) {
            if(an.isAlive()) {
                an.interrupt();
            }
            _animators.remove(f);
            removeViewerListener(an);
        }
    }

    private void animate(int steps) {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            final Futures f = (Futures) c;
            Animation an = _animators.get(f);
            if(an!=null) {
                deanimate(f);
            }
            else {
                animate(f, steps);
            }
        }
        else {
            Branch<World> sel = (Branch<World>) _cagraph.getSelectionCells()[0];
            Animation an = _animators.get(sel);
            if(an!=null&&an.isAlive()) {
                an.interrupt();
                _animators.remove(sel);
            }
            else {
                Animation a = new Animation(sel, _cagraph, steps);
                _animators.put(sel, a);
                a.start();
            }
        }
    }

    private void grow() {
        _cagraph.grow();
    }

    private void updateTabIcon() {
        Futures d = (Futures) _tabs.getSelectedComponent();
        Branch<World> b = d.getBranch();
        _tabs.setIconAt(_tabs.getSelectedIndex(), createIcon(b.data().getRule()));
        String rulestr = b.data().getRule().toString();
        String title = rulestr;
        if(title.length()>12) {
            title = title.substring(0,9)+"...";
        }
        _tabs.setToolTipTextAt(_tabs.getSelectedIndex(), title);
    }

    private void mutate(Mutator mutator) throws MutationFailedException {
        _lastMutator = mutator;
        _repeat.setEnabled(true);
        _repeat.setText("Repeat "+mutator.name());
        final Mutator m = _forceSymmetry?new SymmetryForcer(mutator):mutator;
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            //Display d = (Display) c;
            Futures d = (Futures) c;
            final Branch<World> b = d.getBranch();
            Branch<World> child = b.grow(new World(b.data().getRule().mutate(m), World.getSize(), World.getSize()), m.toString());
            d.addBranch(child);
            _selectLast.setEnabled(d.hasPrevious());
            _selectNext.setEnabled(d.hasNext());
            updateTabIcon();
            generate(d);
            Viewer.getInstance().doWait(new Runnable() {
                public void run() {
                    _cagraph.arrange(new Object[]{b}, new Object[0], b, false);
                }
            }, 2000);
        }
        else {
            Worker.instance().push(new Runnable() {
                public void run() {
                    _cagraph.mutateSelected(m, _variations);
                }
            });
        }
    }

    private void randomMutation() {
        while(true) {
            try {
                mutate(MUTATORS[Rand.om.nextInt(MUTATORS.length)]);
                break;
            }
            catch(MutationFailedException e) {
                System.err.println(_lastMutator.name()+" failed: "+e.getMessage());
            }
        }
    }

    private void repeatLastMutation() {
        try {
            mutate(_lastMutator);
        }
        catch(MutationFailedException e) {
            System.err.println(_lastMutator.name()+" failed: "+e.getMessage());
        }
    }

    private void zoomToSelection() {
        Object[] sele = _cagraph.getSelectionCells();
        _cagraph.setScale(1);
        _cagraph.scrollCellToVisible(sele[0]);
    }

    private void zoomIn() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            Futures f = (Futures) c;
            f.setScale(f.getScale()*1.5f);
        }
        else {
            _cagraph.setScale(_cagraph.getScale()*1.1);
        }
    }

    private void zoomOut() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            Futures f = (Futures) c;
            f.setScale(f.getScale()/1.5f);
        }
        else {
            _cagraph.setScale(_cagraph.getScale()/1.1);
        }
    }

    private void zoomOne() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            Futures f = (Futures) c;
            f.setScale(1.0f);
        }
        else {
            _cagraph.setScale(1.0);
        }
    }

    private void close() {
        _tabs.removeTabAt(_tabs.getSelectedIndex());
        _tabs.setSelectedIndex(0);
    }

    private void newCA() {
        final JDialog d = new JDialog(root(), "New automata", false);
        d.getContentPane().setLayout(new BorderLayout());
        final JTextArea t = new JTextArea(80, 5);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        d.getContentPane().add(new JLabel("Incantation"), BorderLayout.WEST);
        d.getContentPane().add(t, BorderLayout.CENTER);
        JButton b = new JButton(new AbstractAction("Create") {
            public void actionPerformed(ActionEvent e) {
                Rule r = Multirule1D.fromIncantation(t.getText());
                Branch<World> w = _y.root().grow(new World(r, World.getSize(), World.getSize()));
                d.setVisible(false);
                _tabs.setSelectedComponent(branch(w));
            }
        });
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(b, BorderLayout.EAST);
        d.getContentPane().add(buttons, BorderLayout.SOUTH);
        d.setSize(new Dimension(500, 300));
        Things.centerWindow(d);
        d.setVisible(true);
    }

    private void initCA(CA ca, Rule r) {
        r.init(ca, _init);
        /*
        switch(_init) {
            case random:
                ca.initRandom(r.colors());
                break;
            case single:
                ca.initPlain(r.background());
                ca.set(ca.getWidth()/2, 0, r.colors()[1]);
                break;
        }
        */
    }

    private void split() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
        }
        else {
            Object[] sel = _cagraph.getSelectionCells();
            for(Object o:sel) {
                Branch<World> w = (Branch<World>) o;
                Multirule mr = (Multirule) w.data().getRule();
                for(Rule r:mr.rules()) {
                    w.grow(new World(mr.origin().create(new Rule[]{r}), World.getSize(), World.getSize()));
                }
            }
            _cagraph.arrange();
        }
    }

    private void togglePaletteEditor() {
        if(_peditor!=null) {
            _peditor.setVisible(false);
            _peditor = null;
        }
        else {
            _peditor = new JFrame("Palette Editor");
            _peditor.getContentPane().add(new PaletteEditor(_peditor, this));
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
                }

                public void windowClosing(WindowEvent e) {
                    _pehack.setText("Show palette editor");
                    _peditor = null;
                }
            });
        }
        _pehack.setText(_peditor!=null?"Hide palette editor":"Show palette editor");
    }

    private void toggleEditor() {
        if(_editor!=null) {
            _editor.setVisible(false);
            _editor = null;
        }
        else {
            _editor = new JFrame("Pattern Editor");
            _editor.getContentPane().add(new CAEditor(_editor, this));
            _editor.pack();
            Dimension dim = _editor.getContentPane().getPreferredSize();
            _editor.setSize(16+dim.width, 24+dim.height);

            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            JMenuBar bar = new JMenuBar();
            JMenu file = new JMenu("File");
            AbstractAction close = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    _editor.setVisible(false);
                    _editor = null;
                }
            };
            JMenuItem cl = file.add(close);
            cl.setText("Close");
            cl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcut));
            bar.add(file);
            _editor.setJMenuBar(bar);

            _editor.setVisible(true);

            _editor.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    _ehack.setText("Show pattern editor");
                    _editor = null;
                }

                public void windowClosing(WindowEvent e) {
                    _ehack.setText("Show pattern editor");
                    _editor = null;
                }
            });
        }
        _ehack.setText(_editor!=null?"Hide pattern editor":"Show pattern editor");
    }

    private void cancel() {
        Worker.instance().interrupt();
    }

    private void reroll() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            final Futures f = (Futures) _tabs.getSelectedComponent();
            Viewer.getInstance().doWait(new Runnable() {
                public void run() {
                    generate(f, true);
                }
            }, 1000);
        }
    }

    private void generate() {
        Component c = _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            final Futures disp = (Futures) _tabs.getSelectedComponent();
            disp.setShow(_futures);
            Viewer.getInstance().doWait(new Runnable() {
                public void run() {
                    generate(disp);
                }
            }, 1000);
        }
    }

    private void generate(Futures disp) {
        generate(disp, false);
    }

    private void generate(final Futures disp, final boolean reroll) {
        final Rule _r = disp.getRule();
        final JRootPane root = getRootPane();

        System.err.println("generating in mode "+_init);
        Worker.instance().push(new Runnable() {
            public void run() {
                try {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Cursor w = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                            root.getGlassPane().setCursor(w);
                            root.getGlassPane().setVisible(true);
                        }
                    });
                    if(reroll) {
                        disp.reroll(_init);
                    }
                    else {
                        disp.generate(_init);
                    }
                }
                finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Cursor w = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                            root.getGlassPane().setCursor(w);
                            root.getGlassPane().setVisible(false);
                        }
                    });
                }
            }
        });
    }

    public void resizeCA() {
        Component c = _tabs.getSelectedComponent();
        Futures disp = null;
        if(c instanceof Futures) {
            disp = (Futures) _tabs.getSelectedComponent();
        }
        if(disp==null) {
            return;
        }
        final Futures di = disp;
        final JDialog d = new JDialog(root(), "Size");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new GridLayout(2,2));
        top.add(new JLabel("Width"));
        final JTextField width = new JTextField();
        width.setText(""+disp.getCAWidth());
        width.setColumns(6);
        JPanel wp = new JPanel();
        wp.add(width);
        wp.add(new JLabel("px"));
        top.add(wp);
        JPanel hp = new JPanel();
        top.add(new JLabel("Height"));
        final JTextField height = new JTextField();
        height.setText(""+disp.getCAHeight());
        height.setColumns(6);
        hp.add(height);
        hp.add(new JLabel("px"));
        top.add(hp);
        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        JButton de = new JButton("Reset");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                di.setCASize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
                generate(di);
            }
        });
        bot.add(ne);
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
        p.add(bot, BorderLayout.SOUTH);
        d.getContentPane().add(p);
        Dimension dim = p.getPreferredSize();
        dim.height += 40;
        d.setSize(dim);
        Things.centerWindow(d);
        d.setVisible(true);
    }

    public void generateLarge() {
        Component c = _tabs.getSelectedComponent();
        Rule rule = null;
        if(c instanceof Futures) {
            Futures disp = (Futures) _tabs.getSelectedComponent();
            rule = disp.getRule();
        }
        else {
            Object[] sels = _cagraph.getSelectionCells();
            if(sels.length==1) {
                Branch<World> w = (Branch<World>) sels[0];
                rule = w.data().getRule();
            }
        }
        final Rule _r = rule;
        final JDialog d = new JDialog(root(), "Generate automata to disk");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridLayout gl = null;
        if(rule.dimensions()==1) {
            gl = new GridLayout(2,2);
        }
        else {
            gl = new GridLayout(11,2);
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
        if(rule.dimensions()==2) {
            JPanel an = new JPanel();
            animat = new JCheckBox("Animate");
            animat.setSelected(true);
            JPanel fr = new JPanel();
            frame = new JTextField();
            frame.setText(""+_lastFrames);
            frame.setColumns(6);
            JPanel frate = new JPanel();
            framerate = new JTextField();
            framerate.setText("15");
            framerate.setColumns(4);
            JPanel scalingp = new JPanel();
            scalingf = new JTextField();
            scalingf.setText("1.0");
            scalingf.setColumns(4);

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
        final JCheckBox reverse = revers;

        p.add(top, BorderLayout.NORTH);
        JPanel bot = new JPanel();
        JButton ne = new JButton("Generate ...");
        d.getRootPane().setDefaultButton(ne);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
                final JFileChooser f = new JFileChooser(_dir);
                f.setDialogTitle("Save generated automata");
                f.setDialogType(f.SAVE_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showSaveDialog(Viewer.this);
                if(ret==f.APPROVE_OPTION) {
                    _dir = f.getSelectedFile().getParentFile();
                    final JDialog gene = new JDialog(root(), "Generating");
                    JPanel main = new JPanel(new BorderLayout());
                    final JLabel task = new JLabel("Building automata");
                    Font font = task.getFont();
                    task.setFont(font.deriveFont(font.getSize()-2f));
                    final int w = Integer.parseInt(width.getText());
                    final int h = Integer.parseInt(height.getText());
                    _lastWidth = w;
                    _lastHeight = h;
                    final JProgressBar prog = new JProgressBar(1, h);
                    final File selfile = f.getSelectedFile();
                    prog.setValue(0);
                    main.add(prog, BorderLayout.NORTH);
                    main.add(task, BorderLayout.WEST);
                    main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    gene.add(main, BorderLayout.CENTER);
                    final JButton[] hack = new JButton[1];

                    final Thread builder = new Thread() {
                        public void run() {
                            try {
                                if(_r.dimensions()==1) {
                                    CA c = new CA(w, h);
                                    initCA(c, _r);
                                    _r.generate(c, 1, h-1, false, false, new Rule.Updater() {
                                        public void update(Rule r, int start, int current, int end) {
                                            prog.setValue(current);
                                        }

                                        public long interval() {
                                            return -1;
                                        }
                                    });
                                    if(!Thread.currentThread().isInterrupted()) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                task.setText("Writing "+selfile.getName());
                                                prog.setIndeterminate(true);
                                            }
                                        });
                                        hack[0].setEnabled(false);
                                        c.save(selfile);
                                    }
                                }
                                else {
                                    //ffmpeg -f image2 -i life-%d.jpg -ab 128kb -vcodec mpeg4 -b 1200kb -mbd 2 -flags +4mv -trellis 2 -cmp 2 -subcmp 2 life.mp4
                                    int numFrames = Integer.parseInt(frames.getText());
                                    int frameRate = Integer.parseInt(frates.getText());
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
                                    CA c = new CA(w, h);
                                    initCA(c, _r);
                                    Iterator<CA> cas = ((Multirule2D)_r).frames(c);
                                    if(createGif) {
                                        AnimatedGifEncoder age = new AnimatedGifEncoder();
                                        age.start(selfile+".gif");
                                        if(bb) {
                                            age.setRepeat(0);
                                        }
                                        else {
                                            age.setDelay(100);
                                        }
                                        LinkedList<BufferedImage> frames = new LinkedList<BufferedImage>();
                                        for(int i=0;i<numFrames;i++) {
                                            if(Thread.currentThread().isInterrupted()) {
                                                break;
                                            }
                                            if(rever||bb) {
                                                BufferedImage orig = c.getImageBuffer();
                                                BufferedImage copy = new BufferedImage(orig.getColorModel(), orig.copyData(null), orig.isAlphaPremultiplied(), null);
                                                frames.add(copy);
                                            }
                                            else {
                                                age.addFrame(c.getImageBuffer());
                                            }
                                            prog.setValue(i);
                                            c = cas.next();
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
                                    }
                                    else {
                                        for(int i=0;i<numFrames;i++) {
                                            if(Thread.currentThread().isInterrupted()) {
                                                break;
                                            }
                                            if(intermediate&&(!createGif||createMp4||createWebm)) {
                                                if(scale!=1f) {
                                                    c = c.toScaledCA(scale);
                                                }
                                                c.save(selfile+"-"+(rever?numFrames-i-1:i)+ext);
                                            }
                                            if(Thread.currentThread().isInterrupted()) {
                                                break;
                                            }
                                            prog.setValue(i);
                                            c = cas.next();
                                        }
                                    }
                                    if(createWebm) {
                                        // ffmpeg -y -f image2 -threads 3 -i frame-%d.png -c:v libvpx -crf 10 -b:v 1M -c:a libvorbis -deadline best output.webm
                                        Runner pb1 = new Runner(
                                            "ffmpeg", "-y",
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

    public void selectImageStateImage() {
        SwingUtilities.invokeLater(new Runnable() {
        //new Thread() {
            public void run() {
                final JFileChooser f = new JFileChooser(_dir);
                f.setDialogTitle("Initial state image");
                f.setDialogType(f.OPEN_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showOpenDialog(Viewer.this);
                if(ret==f.APPROVE_OPTION) {
                    File img = f.getSelectedFile();
                    try {
                        _initImage = ImageIO.read(img);
                        _init = Rule.Initialization.image;
                        System.err.println("read image "+img);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                generate();
                            }
                        });
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        //}.start();
        });
    }

    public BufferedImage getInitialImage() {
        return _initImage;
    }

    public void save() {
        if(_tabs.getSelectedComponent() instanceof Futures) {
            Futures d = (Futures) _tabs.getSelectedComponent();
            JFileChooser f = new JFileChooser(_dir);
            f.setDialogTitle("Save automata");
            f.setDialogType(f.SAVE_DIALOG);
            f.setMultiSelectionEnabled(false);
            int ret = f.showSaveDialog(this);
            CA _c = d.getCA();
            if(ret==f.APPROVE_OPTION) {
                try {
                    _dir = f.getSelectedFile().getParentFile();
                    _c.save(f.getSelectedFile());
                }
                catch(IOException e) {
                    JDialog er = new JDialog(root(), "Well, shit.");
                    er.getContentPane().add(new JLabel("Failed to save "+f.getSelectedFile()+": "+e.getClass().getName()+": "+e.getMessage()));
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    JTextArea text = new JTextArea(sw.toString());
                    er.getContentPane().add(text);
                    er.setSize(300, 300);
                    Things.centerWindow(er);
                    er.setVisible(true);
                }
            }
        }
        else {
            try {
                _y.save(STORE);
            }
            catch(IOException e) {
                JDialog er = new JDialog(root(), "Well, shit.");
                er.getContentPane().add(new JLabel("Failed to save "+STORE+": "+e.getClass().getName()+": "+e.getMessage()));
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                JTextArea text = new JTextArea(sw.toString());
                er.getContentPane().add(text);
                er.setSize(300, 300);
                Things.centerWindow(er);
                er.setVisible(true);
            }
        }
    }

    public void selectLast() {
        Futures d = (Futures) _tabs.getSelectedComponent();
        if(d.hasPrevious()) {
            d.setCurrent(d.getCurrent()-1);
            _selectLast.setEnabled(d.hasPrevious());
            _selectNext.setEnabled(d.hasNext());
            updateTabIcon();
            generate(d);
        }
    }

    public void selectNext() {
        Futures d = (Futures) _tabs.getSelectedComponent();
        if(d.hasNext()) {
            d.setCurrent(d.getCurrent()+1);
            _selectLast.setEnabled(d.hasPrevious());
            _selectNext.setEnabled(d.hasNext());
            updateTabIcon();
            generate(d);
        }
    }

    public void open3d() {
        Component c = (Component) _tabs.getSelectedComponent();
        if(c instanceof Futures) {
            final Futures f = (Futures) c;
            Thread t = new Thread() {
                public void run() {
                    try {
                        String rule = f.getRule().toIncantation();
                        //String rule = f.getRule().toString();
                        String[] cmd = {System.getProperty("user.home")+"/code/working/ca3d/target/automatron3d-1.0/bin/automatron3d",
                            "-"};
                        Process p = Runtime.getRuntime().exec(cmd);
                        new Streamer(p.getInputStream()).start();
                        new Streamer(p.getErrorStream()).start();
                        OutputStream out = p.getOutputStream();
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
                        pw.println(rule);
                        pw.close();
                        p.waitFor();
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();

            /*
            View3d v = new View3d();
            v.setRule(f.getRule());
            _tabs.addTab("", createIcon(f.getRule()), v, "3D");
            */
        }
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
        JMenu file = new JMenu("File");
        AbstractAction newCA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                newCA();
            }
        };
        AbstractAction save = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        };
        AbstractAction close = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        };
        JMenuItem ni = file.add(newCA);
        ni.setText("New ...");
        ni.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
        JMenuItem si = file.add(save);
        si.setText("Save");
        si.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
        JMenuItem cl = file.add(close);
        cl.setText("Close");
        cl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
        bar.add(file);

        JMenu ygg = new JMenu("Yggdrasil");
        AbstractAction grow = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                grow();
            }
        };
        JMenuItem gro = ygg.add(grow);
        gro.setText("Grow");
        gro.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcut));
        ygg.addSeparator();
        JMenu spacing = new JMenu("Spacing");

        final JCheckBoxMenuItem[] hack2 = new JCheckBoxMenuItem[3];
        JCheckBoxMenuItem logs = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _cagraph.setSpacing(Bloom.Spacing.log);
                hack2[0].setState(true);
                hack2[1].setState(false);
                hack2[2].setState(false);
            }
        });
        logs.setText("Logarithmic");
        logs.setState(true);
        spacing.add(logs);
        hack2[0] = logs;
        JCheckBoxMenuItem lins = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _cagraph.setSpacing(Bloom.Spacing.linear);
                hack2[0].setState(false);
                hack2[1].setState(true);
                hack2[2].setState(false);
            }
        });
        lins.setText("Linear");
        spacing.add(lins);
        hack2[1] = lins;
        JCheckBoxMenuItem sq = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _cagraph.setSpacing(Bloom.Spacing.square);
                hack2[0].setState(false);
                hack2[1].setState(false);
                hack2[2].setState(true);
            }
        });
        sq.setText("Square");
        spacing.add(sq);
        hack2[2] = sq;

        ygg.add(spacing);

        //JMenuItem zoomin = ygg.add(zi);
        //zoomin.setText("Zoom in");
        //zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut));

        AbstractAction zi = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                zoomIn();
            }
        };
        JMenuItem zoomin = ygg.add(zi);
        zoomin.setText("Zoom in");
        zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut));

        AbstractAction zo = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                zoomOut();
            }
        };
        JMenuItem zoomout = ygg.add(zo);
        zoomout.setText("Zoom out");
        zoomout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, shortcut));

        AbstractAction z1 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                zoomOne();
            }
        };
        JMenuItem zoomone = ygg.add(z1);
        zoomone.setText("Actual size");
        zoomone.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, shortcut));

        AbstractAction zt = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                zoomToSelection();
            }
        };
        JMenuItem zoomt = ygg.add(zt);
        zoomt.setText("Zoom to selection");
        zoomt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut));
        bar.add(ygg);

        final JCheckBoxMenuItem[] hack = new JCheckBoxMenuItem[4];
        JMenu auto = new JMenu("Automata");

        AbstractAction opentab = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                newTab();
            }
        };
        JMenuItem openi = auto.add(opentab);
        openi.setText("Open");
        openi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));

        AbstractAction inf = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                info();
            }
        };
        JMenuItem info = auto.add(inf);
        info.setText("Info");
        info.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, shortcut));

        AbstractAction cancel = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        };
        JMenuItem canc = auto.add(cancel);
        canc.setText("Cancel current calculation");
        canc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcut));
        auto.addSeparator();

        //JMenu viewtype = new JMenu("View");
        /*
        full.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, shortcut));
        full.setState(true);
        JCheckBoxMenuItem fut = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _futures = true;
                fhack[0].setState(false);
                fhack[1].setState(true);
                generate();
            }
        });
        viewtype.add(fut);
        fut.setText("Futures");
        fut.setSelected(false);
        fut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, shortcut));
        fhack[1] = fut;
        auto.add(viewtype);
        auto.addSeparator();
        */

        JCheckBoxMenuItem ran = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Rule.Initialization.random;
                hack[0].setState(true);
                hack[1].setState(false);
                hack[2].setState(false);
                hack[3].setState(false);
                generate();
            }
        });
        auto.add(ran);
        hack[0] = ran;
        ran.setText("Random initial state");
        ran.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
        ran.setState(_init==Rule.Initialization.random);
        JCheckBoxMenuItem fix = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Rule.Initialization.single;
                hack[0].setState(false);
                hack[1].setState(true);
                hack[2].setState(false);
                hack[3].setState(false);
                generate();
            }
        });
        auto.add(fix);
        fix.setText("Fixed initial state");
        fix.setSelected(_init==Rule.Initialization.single);
        fix.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcut));
        hack[1] = fix;
        JCheckBoxMenuItem ara = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _init = Rule.Initialization.arabesque;
                hack[0].setState(false);
                hack[1].setState(false);
                hack[2].setState(true);
                hack[3].setState(false);
                generate();
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
        //imagest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcut));
        imagest.setEnabled("2".equals(_dimensions));
        hack[3] = imagest;
        auto.addSeparator();

        /*
        final JCheckBoxMenuItem[] pthack = new JCheckBoxMenuItem[3];
        final JCheckBoxMenuItem evaporation = new JCheckBoxMenuItem(new AbstractAction("Evaporation") {
            public void actionPerformed(ActionEvent e) {
                pthack[0].setState(toggleEvaporate());
                generate();
            }
        });
        evaporation.setState(_evaporate);
        auto.add(evaporation);
        pthack[0] = evaporation;

        final JCheckBoxMenuItem phasetrans = new JCheckBoxMenuItem(new AbstractAction("Phase transitions") {
            public void actionPerformed(ActionEvent e) {
                pthack[1].setState(togglePhaseTransition());
                generate();
            }
        });
        phasetrans.setState(_phasetrans);
        auto.add(phasetrans);
        pthack[1] = phasetrans;

        final JCheckBoxMenuItem stabilityreset = new JCheckBoxMenuItem(new AbstractAction("Reset stability on phase transition") {
            public void actionPerformed(ActionEvent e) {
                pthack[2].setState(toggleStabilityReset());
                generate();
            }
        });
        stabilityreset.setState(_stabilityreset);
        auto.add(stabilityreset);
        pthack[2] = stabilityreset;
        */

        final java.util.List<JCheckBoxMenuItem> opthack = new ArrayList<JCheckBoxMenuItem>();
        int x = 0;
        for(final Options o:java.util.EnumSet.allOf(Options.class)) {
            final int cidx = x;
            JCheckBoxMenuItem option = new JCheckBoxMenuItem(new AbstractAction(o.description()) {
                public void actionPerformed(ActionEvent e) {
                    o.toggle();
                    opthack.get(cidx).setState(o.get());
                    generate();
                }
            });
            option.setState(o.get());
            auto.add(option);
            opthack.add(option);
            x++;
        }

        auto.addSeparator();

        AbstractAction re = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Rand.newSeed();
                reroll();
            }
        };
        JMenuItem reroll = auto.add(re);
        reroll.setText("Reroll");
        reroll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));

        AbstractAction spl = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                split();
            }
        };
        JMenuItem split = auto.add(spl);
        split.setText("Branch");
        split.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcut));

        AbstractAction an = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                animate(-1);
            }
        };
        JMenuItem animate = auto.add(an);
        animate.setText("Animate");
        animate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcut));

        AbstractAction stp = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                animate(1);
            }
        };
        JMenuItem stepone = auto.add(stp);
        stepone.setText("Step");
        stepone.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, shortcut));

        auto.addSeparator();

        AbstractAction gen = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                generateLarge();
            }
        };
        JMenuItem genl = auto.add(gen);
        genl.setText("Generate to disk ...");
        genl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, shortcut));

        AbstractAction size = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                resizeCA();
            }
        };
        JMenuItem siz = auto.add(size);
        siz.setText("Set size ...");
        siz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcut));

        AbstractAction dimup = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                open3d();
            }
        };
        JMenuItem dimensions = auto.add(dimup);
        dimensions.setText("View from higher dimension ...");
        //siz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcut));

        bar.add(auto);

        JMenu mutate = new JMenu("Mutate");
        AbstractAction rep = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                repeatLastMutation();
            }
        };
        _repeat = mutate.add(rep);
        _repeat.setText("Repeat last mutation");
        _repeat.setEnabled(false);
        _repeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));

        AbstractAction rand = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                randomMutation();
            }
        };
        final JMenuItem random = mutate.add(rand);
        random.setText("Random mutation");
        random.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcut));
        mutate.addSeparator();

        for(final Mutator m:MUTATORS) {
            AbstractAction mut = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        mutate(m);
                    }
                    catch(MutationFailedException ex) {
                        System.err.println(m.name()+" failed: "+ex.getMessage());
                    }
                }
            };
            JMenuItem mutat = mutate.add(mut);
            mutat.setText(m.name());
        }
        if(_dimensions.equals("2")) {
            mutate.addSeparator();
            for(final Mutator m:MUTATORS2) {
                AbstractAction mut = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            mutate(m);
                        }
                        catch(MutationFailedException ex) {
                            System.err.println(m.name()+" failed: "+ex.getMessage());
                        }
                    }
                };
                JMenuItem mutat = mutate.add(mut);
                mutat.setText(m.name());
            }
        }
        mutate.addSeparator();
        final JCheckBoxMenuItem[] mhack = new JCheckBoxMenuItem[1];
        JCheckBoxMenuItem forceSym = new JCheckBoxMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _forceSymmetry = !_forceSymmetry;
                MutatorFactory.instance().setForceSymmetry(_forceSymmetry);
                mhack[0].setState(!mhack[0].getState());
                mhack[0].setSelected(!mhack[0].getState());
            }
        });
        mutate.add(forceSym);
        mhack[0] = forceSym;
        forceSym.setText("Force symmetry");
        forceSym.setState(_forceSymmetry);
        forceSym.setSelected(_forceSymmetry);

        bar.add(mutate);

        JMenu window = new JMenu("Window");
        AbstractAction sellast = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectLast();
            }
        };
        _selectLast = window.add(sellast);
        _selectLast.setEnabled(false);
        _selectLast.setText("Previous automata");
        _selectLast.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, shortcut));

        AbstractAction selnext = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectNext();
            }
        };
        _selectNext = window.add(selnext);
        _selectNext.setEnabled(false);
        _selectNext.setText("Next automata");
        _selectNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, shortcut));

        final JMenuItem[] fhack = new JMenuItem[1];
        final JMenuItem full = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _futures = !_futures;
                fhack[0].setText(_futures?"Hide mutations":"Show mutations");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        generate();
                    }
                });
            }
        });
        window.addSeparator();
        window.add(full);
        fhack[0] = full;
        full.setText(_futures?"Hide mutations":"Show mutations");
        full.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut));

        //_ehack = new JMenuItem[1];
        final JMenuItem editor = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //_editor = !_editor;
                toggleEditor();
            }
        });
        _ehack = editor;
        editor.setText("Show pattern editor");
        editor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcut));
        window.add(editor);

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
        bar.add(window);

        root().setJMenuBar(bar);
    }

    static class WorldRenderer extends JComponent implements CellViewRenderer, java.io.Serializable {
        private static final AffineTransformOp AFFINE = new AffineTransformOp(new AffineTransform(), AffineTransformOp.TYPE_BICUBIC);
        private static final Stroke STROKE = new BasicStroke();
        private static final Stroke TARGET_STROKE = new BasicStroke(1,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5f,5f}, 0.0f);
        private BufferedImage _bi;
        private JGraph _graph;
        private boolean _selected;
        private boolean _target;
        static double scale = 1f;


        static {
            //AFFINE.getRenderingHints().add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            //AFFINE.getRenderingHints().add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        }

        public void paint(Graphics g) { 
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(_bi, AFFINE, 0, 0);
            if(_selected) {
                paintSelectionBorder(g);
            }
            if(_target) {
                paintTargetBorder(g);
            }
        }

        public Component getRendererComponent(JGraph graph, CellView 
            view, boolean sel, boolean focus, boolean preview) { 
            CAGraph g = (CAGraph) graph;
            Branch<World> branch = (Branch<World>) view.getCell();
            _bi = (BufferedImage) branch.data().getCA().toImage();
            _graph = graph;
            _selected = sel;
            _target = g.isTarget(branch);
            return this;
        }

        protected void paintSelectionBorder(Graphics g) {
            ((Graphics2D) g).setStroke(STROKE);
            //g.setColor(_graph.getHighlightColor());
            g.setColor(Color.WHITE);
            Dimension d = getSize();
            g.drawRect(0, 0, d.width-1, d.height-1);
        }

        protected void paintTargetBorder(Graphics g) {
            ((Graphics2D) g).setStroke(TARGET_STROKE);
            //g.setColor(_graph.getHighlightColor());
            g.setColor(Color.WHITE);
            Dimension d = getSize();
            g.drawRect(0, 0, d.width-1, d.height-1);
        }

        //public Point2D getPerimeterPoint(VertexView view, Point2D source, Point2D p) {
        //}
    }

    static class FastEdgeRenderer extends EdgeRenderer implements CellViewRenderer, java.io.Serializable {
        //public void paint(Graphics g) { 
        //}

        public void validate() {
            boolean valid = isValid();
            if (!valid) {
                boolean updateCur = false;
                synchronized (getTreeLock()) {
                    ComponentPeer peer = getPeer();
                    if (!valid && peer != null) {
                        ContainerPeer p = null;
                        if (peer instanceof ContainerPeer) {
                            p = (ContainerPeer) peer;
                        }
                        if (p != null) {
                            p.beginValidate();
                        }
                        validateTree();
                        valid = true;
                        if (p != null) {
                            p.endValidate();
                            updateCur = isVisible();
                        }
                    }
                }            
                if (updateCur) {
                        //updateCursorImmediately();
                }
            }
        }
    }

    public static final Mutator[] MUTATORS = {new Collapse(), new Order(),
        new Cull(), new Clone(), new Hue(), new org.excelsi.ca.Color(),
        new Expand(), new Noise(), new Tangle(), new Diverge(), new Thin(),
        new Thicken(), new Symmetry(), new Transpose(), new Stability(-1),
        new Stability(1)};
    public static final Mutator[] MUTATORS2 = {new Life()};

    public static Mutator createMutator(Random om) {
        return Viewer.getInstance().internalCreateMutator(om);
    }

    public Mutator internalCreateMutator(Random om) {
        Mutator m = createRandomMutator(om);
        if(_forceSymmetry) {
            return new SymmetryForcer(m);
        }
        else {
            return m;
        }
    }

    protected static Mutator createRandomMutator(Random om) {
        Mutator m = null;
        switch(om.nextInt(16)) {
            case 13:
                m = new Symmetry(); break;
            case 12:
                m = new Thicken(); break;
            case 11:
                m = new Thin(); break;
            case 10:
                m = new Diverge(); break;
            case 9:
                m = new Collapse(); break;
            case 8:
                m = new Order(); break;
            case 7:
                m = new Cull(); break;
            case 6:
                m = new Clone(); break;
            case 5:
                m = new Hue(); break;
            case 4:
                m = new org.excelsi.ca.Color(); break;
            case 3:
                m = new Expand(); break;
            case 2:
                m = new Tangle(); break;
            default:
                m = new Noise(); break;
        }
        m.setRandom(om);
        return m;
    }

    static class CAToolTipUI extends BasicToolTipUI {
        private Image _i;

        public CAToolTipUI(Image i) {
            _i = i;
        }

        public void paint(Graphics g, JComponent c) {
            g.drawImage(_i, 0, 0, null);
        }
    }

    static class CAToolTip extends JToolTip {
        public void setUI(CAToolTipUI u) {
            super.setUI(u);
        }
    }

    static class CAGraph extends JGraph {
        private CellViewRenderer _renderer = new WorldRenderer();
        private CellViewRenderer _erenderer = new FastEdgeRenderer();
        private Happiness _h;
        private Thread _bloom;
        //private Worker _worker = new Worker();
        private boolean _growing;
        private Bloom _b;
        private Object _target;
        private int _targetIndex;
        private Object _lastSource;
        private CAToolTip _tt = new CAToolTip();

        public CAGraph(DefaultVertex v, Bloom b) {
            super(v);
            _tt.setComponent(this);
            _tt.add(new JLabel());
            setUI(new CAGraphUI());
            _b = b;
            //_worker.start();
            ToolTipManager.sharedInstance().registerComponent(this);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    System.err.println(e);
                    if(e.getClickCount()==2) {
                        Object cell = getFirstCellForLocation(e.getX(), e.getY());
                        if(cell!=null) {
                            Viewer.getInstance().newTab();
                        }
                    }
                }
            });
        }

        public boolean isEditable() { return false; }

        /*
        public JToolTip createToolTip() {
            //String text = getToolTipText();
            //System.err.println( "in here") ;
            //JLabel c = new JLabel();
            //c.setText("Positive Negative");
            //JToolTip t = new JToolTip();
            //t.add(c);
            //return t;
            System.err.println("returning "+_tt);
            return _tt;
        }
        */


        public String getToolTipText() {
            return _lastToolTipString;
        }

        private Object _lastToolTipCell;
        private String _lastToolTipString;
        public String getToolTipText(MouseEvent e) {
            if(e!=null) {
                Object cell = getFirstCellForLocation(e.getX(), e.getY());
                String s = convertValueToString(cell);
                if(s==null) {
                    s = "";
                }
                if(cell instanceof Branch) {
                    if(cell==_lastToolTipCell) {
                        return _lastToolTipString;
                    }
                    Branch<World> w = (Branch<World>) cell;
                    CA t = new CA(World.getSize(), World.getSize());
                    Viewer.getInstance().initCA(t, w.data().getRule());
                    w.data().getRule().generate(t, 1, World.getSize()-1, false, false, null);
                    try {
                        File tmp = File.createTempFile("catooltip", ".png");
                        tmp.deleteOnExit();
                        t.save(tmp);
                        int rc = ((Multirule)w.data().getRule()).rules().length;
                        _lastToolTipString = "<html>"+w.data().getCA().getGoodness()+" ("+rc+")<br><img src=\"file:///"+tmp+"\"></html>";
                        _lastToolTipCell = cell;
                        //System.err.println( "set to: "+_lastToolTipString);
                        return _lastToolTipString;
                    }
                    catch(IOException ex) {
                        //ex.printStackTrace();
                        return ex.toString();
                    }
                }
                return null;
            }
            return null;
        }
        /*
        public String getToolTipText(MouseEvent e) {
            if(e!=null) {
                Object cell = getFirstCellForLocation(e.getX(), e.getY());
                String s = convertValueToString(cell);
                if(s==null) {
                    s = "";
                }
                if(cell instanceof Branch) {
                    Branch<World> w = (Branch<World>) cell;
                    CA t = new CA(World.getSize(), World.getSize());
                    Viewer.getInstance().initCA(t, w.data().getRule());
                    w.data().getRule().generate(t, 1, World.getSize()-1, false, null);
                    JLabel l = (JLabel) _tt.getComponent(0);
                    l.setIcon(new ImageIcon(t.toImage()));
                    _tt.setUI(new CAToolTipUI(t.toImage()));
                    System.err.println("set ui");

                    return "";
                }
                return s;
            }
            return null;
        }
        */

        public void setScale(double scale) {
            super.setScale(scale);
            WorldRenderer.scale = scale;
            // jgraph scales in a really dumb way
            Object[] cells = getSelectionCells();
            if(cells.length>0) {
                scrollCellToVisible(cells[0]);
            }
            if(_target!=null) {
                scrollCellToVisible(_target);
            }
        }

        public void nextTarget(Object[] cells) {
            if(cells.length==0) {
                return;
            }
            Object[] sel = getSelectionCells();
            Object s = null;
            if(sel.length>0) {
                s = sel[0];
            }
            if(cells.length==1&&cells[0]==s) {
                nextTarget();
                return;
            }
            do {
                if(++_targetIndex>=cells.length) {
                    _targetIndex = 0;
                }
            } while(cells[_targetIndex]==s);
            setTarget(cells[_targetIndex]);
            graphDidChange();
            scrollCellToVisible(cells[_targetIndex]);
            //setSelectionCell(cells[_targetIndex]);
        }

        public void nextTarget() {
            Object[] cells = getSelectionCells();
            if(cells.length>1) {
                return;
            }
            if(cells.length==0) {
                setSelectionCell(_b.getRoot());
                scrollCellToVisible(_b.getRoot());
                cells = new Object[]{_b.getRoot()};
            }
            Branch<World> s = (Branch<World>) cells[0];
            Branch<World>[] adj = (Branch<World>[]) s.adjacentVertices().toArray(new Branch[0]);
            if(s!=_lastSource) {
                _targetIndex = 0;
                _lastSource = s;
            }
            else {
                if(++_targetIndex>=adj.length) {
                    _targetIndex = 0;
                }
            }
            setTarget(adj[_targetIndex]);
            graphDidChange();
            scrollCellToVisible(adj[_targetIndex]);
        }

        public void setTarget(Object cell) {
            _target = cell;
        }

        public Object getTarget() {
            return _target;
        }

        public boolean isTarget(Object cell) {
            return cell==_target;
        }

        public void setSpacing(Bloom.Spacing s) {
            _b.setSpaceType(s);
            arrange();
        }

        public void toggleSpacing() {
            Bloom.Spacing n = null;
            switch(_b.getSpaceType()) {
                case linear:
                    n = Bloom.Spacing.square;
                    break;
                case square:
                    n = Bloom.Spacing.log;
                    break;
                case log:
                    n = Bloom.Spacing.linear;
                    break;
            }
            _b.setSpaceType(n);
            arrange();
        }

        public void arrange() {
            Worker.instance().push(new Runnable() {
                public void run() {
                    _b.run(CAGraph.this, ((Vertex)getModel()).vertices().toArray(), 10);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            firePropertyChange(SCALE_PROPERTY, 1, 2);
                        }
                    });
                }
            });
        }

        public void grow() {
            if(_growing) {
                Worker.instance().interrupt();
            }
            else {
                _growing = true;
                Worker.instance().push(new Runnable() {
                    public void run() {
                        Object[] cells = ((Vertex)getModel()).vertices().toArray();
                        Arrays.sort(cells, new Comparator() {
                            public boolean equals(Object o) {
                                return true;
                            }

                            public int compare(Object o1, Object o2) {
                                Branch<World> w1 = (Branch<World>) o1;
                                Branch<World> w2 = (Branch<World>) o2;
                                return (int)w2.data().getCA().getGoodness() - (int)w1.data().getCA().getGoodness();
                            }
                        });
                        //System.err.println("g0="+((Branch<World>)cells[0]).data().getCA().getGoodness());

                        int count = Math.max(1, (int) (Math.log(cells.length)/2));
                        //count = 1;
                        Object[] mut = new Object[count];
                        System.err.println(cells.length+"+"+count);
                        for(int i=0;i<count;i++) {
                            //do {
                                mut[i] = cells[Rand.om.nextInt(Math.max(1,cells.length/3))];
                            //} while(((Vertex)mut[i]).adjacentVertices().size()>2);
                        }
                        mutate(mut, createMutator(Rand.om), false);
                        try {
                            Thread.sleep(200);
                        }
                        catch(InterruptedException e) {
                            _growing = false;
                            return;
                        }
                        if(!Thread.currentThread().interrupted()) {
                            Worker.instance().push(this);
                        }
                        else {
                            _growing = false;
                        }
                    }
                });
            }
        }

        public boolean isCellEditable() {
            return false;
        }

        public void mutateSelected(Mutator m) {
            mutateSelected(m, 1);
        }

        public void mutateSelected(final Mutator m, final int vars) {
            final Object[] cells = getSelectionCells();
            Worker.instance().push(new Runnable() {
                public void run() {
                    mutate(cells, m, true, vars);
                }
            });
        }

        public void regenerateSelected() {
            for(Object cell:getSelectionCells()) {
                Branch<World> b = (Branch<World>) cell;
                b.data().clearCA();
                //WorldRenderer.clearCache(b);
            }
            graphDidChange();
        }

        public void mutate(final Object[] sels, Mutator m, final boolean scroll) {
            mutate(sels, m, scroll, 1);
        }

        public void mutate(final Object[] sels, Mutator mu, final boolean scroll, final int vars) {
            if(mu==null) {
                mu = createMutator(Rand.om);
            }
            final Mutator m = mu;
            Viewer.getInstance().doWait(new Runnable() {
                public void run() {
                    Branch<World> lastChild = null;
                    java.util.Set all = ((Vertex)getModel()).vertices();
                    ArrayList vis = new ArrayList(sels.length*2);
                    for(int i=0;i<sels.length;i++) {
                        if(sels[i] instanceof Branch) {
                            vis.add(sels[i]);
                            Branch<World> b = (Branch<World>) sels[i];
                            for(int j=0;j<vars;j++) {
                                try {
                                    lastChild = b.grow(new World(b.data().getRule().mutate(m), World.getSize(), World.getSize()), m.toString());
                                    vis.add(lastChild);
                                }
                                catch(Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                        }
                    }
                    if(lastChild!=null) {
                        //_h.run(CAGraph.this, sels, 1);
                        arrange(sels, vis.toArray(), lastChild, scroll);
                        /*
                        if(scroll) {
                            _b.setKeepVisible(vis.toArray());
                        }
                        else {
                            _b.setKeepVisible(null);
                        }
                        _b.run(CAGraph.this, sels, 10);
                        //_b.updateView();
                        //_h.run(CAGraph.this, all.toArray(), 10);
                        final Branch<World> last = lastChild;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                firePropertyChange(SCALE_PROPERTY, 1, 2);
                                if(scroll) {
                                    Rectangle2D r = getCellBounds(((Vertex)sels[0]).adjacentVertices().toArray());
                                    scrollRectToVisible(new Rectangle((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight()));
                                    scrollCellToVisible(last);
                                }
                            }
                        });
                        */
                    }
                }
            }, 2000);
        }

        public void arrange(final Object[] sels, final Object[] vis, final Branch<World> lastChild, final boolean scroll) {
            if(scroll) {
                _b.setKeepVisible(vis);
            }
            else {
                _b.setKeepVisible(null);
            }
            _b.run(CAGraph.this, sels, 10);
            //_b.updateView();
            //_h.run(CAGraph.this, all.toArray(), 10);
            final Branch<World> last = lastChild;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    firePropertyChange(SCALE_PROPERTY, 1, 2);
                    if(scroll) {
                        Rectangle2D r = getCellBounds(((Vertex)sels[0]).adjacentVertices().toArray());
                        scrollRectToVisible(new Rectangle((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight()));
                        scrollCellToVisible(last);
                    }
                }
            });
        }

        protected EdgeView createEdgeView(JGraph graph, CellMapper mapper, Object cell) {
            return new EdgeView(cell, graph, mapper) {
                public CellViewRenderer getRenderer() {
                    return _erenderer;
                }

                public Rectangle2D getBounds() {
                    try {
                        return super.getBounds();
                    }
                    catch(NullPointerException e) {
                        return new Rectangle2D.Double();
                    }
                }
            };
        }

        protected VertexView createVertexView(JGraph g, CellMapper map, Object cell) {
            if(cell instanceof Branch) {
                VertexView v = new VertexView(cell, g, map) {
                    public CellViewRenderer getRenderer() {
                        return _renderer;
                    }

                    public void setCachedBounds(Rectangle2D bounds) {
                        super.setCachedBounds(bounds);
                        GraphConstants.setBounds(allAttributes, bounds);
                    }

                    /*
                    public Rectangle2D getBounds() {
                        Rectangle2D r = super.getBounds();
                        System.err.println("BOUNDS: "+r);
                        return r;
                    }
                    */

                    /*
                    public void update() {
                        Rectangle2D b = bounds;
                        super.update();
                        bounds = b;
                    }
                    */
                };
                v.setBounds(GraphConstants.getBounds(((DefaultVertex)cell).getAttributes()));
                return v;
            }
            else {
                return super.createVertexView(g, map, cell);
            }
        }
    }
}
