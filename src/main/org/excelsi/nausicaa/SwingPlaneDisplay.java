package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import javax.swing.Icon;
import javax.swing.JTextArea;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.awt.Dimension;
import javax.swing.SwingUtilities;
import javax.swing.JProgressBar;
import javax.swing.BorderFactory;
import java.awt.Font;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;
import java.awt.Cursor;
import javax.swing.RootPaneContainer;
import javax.swing.JRootPane;
import javax.swing.JComponent;


public class SwingPlaneDisplay extends PlaneDisplay {
    private JLabel _label;
    private JLabel _meta;
    private JLabel _hyper;
    private JCA _img;
    private JScrollPane _show;
    private boolean _shown = true;
    private Rule _r;
    private CA _c;
    private Plane _p;
    private GOptions _gopt;
    private Config _config;
    private Rendering _rend;
    private int _w;
    private int _h;
    private float _scale = 1.0f;
    private final boolean useNew = false;


    public SwingPlaneDisplay(CA ca, GOptions g, Config c) {
        this(ca.getWidth(), ca.getHeight());
        _gopt = g;
        _config = c;
        setCA(ca);
    }

    private SwingPlaneDisplay(int w, int h) {
        _w = w;
        _h = h;
        setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        setForeground(Color.BLACK);
        setBackground(Color.BLACK);
        p.setBackground(Color.BLACK);
        p.setForeground(Color.BLACK);
        _label = new JLabel("NOTHING", javax.swing.SwingConstants.CENTER);
        _label.setBackground(Color.BLACK);
        _label.setForeground(Color.BLACK);
        if(useNew) {
            _img = new JCA(w, h);
            _img.setAlignmentX(0.5f);
            _img.setAlignmentY(0.5f);
            p.add(_img, BorderLayout.CENTER);
        }
        else {
            p.add(_label);
        }

        _meta = new JLabel("", javax.swing.SwingConstants.CENTER);
        _meta.setForeground(Color.WHITE);
        _hyper = new JLabel("");
        p.add(_meta, BorderLayout.SOUTH);
        p.add(_hyper, BorderLayout.EAST);

        JScrollPane scr = new JScrollPane(p);
        scr.setBackground(Color.BLACK);
        scr.setForeground(Color.BLACK);
        add(scr, BorderLayout.CENTER);
        _show = scr;
    }

    private SwingPlaneDisplay(Plane p) {
        this(p.getWidth(), p.getHeight());
        setCA(p.creator());
    }

    private SwingPlaneDisplay(int w, int h, Rule r) {
        this(w, h);
        _r = r;
    }

    public boolean delegateUnlock() {
        return false;
    }

    public JLabel getLabel() {
        return _label;
    }

    public JComponent getDisplayComponent() {
        return useNew?_img:_label;
    }

    private Icon _oldIcon;
    public void toggleShow() {
        if(_shown) {
            _oldIcon = _label.getIcon();
            _label.setIcon(null);
        }
        else {
            _label.setIcon(_oldIcon);
        }
        _shown = ! _shown;
        invalidate();
    }

    public CA getCA() {
        return _p.creator();
    }

    public Rule getRule() {
        return _c.getRule();
    }

    public Rule compileRule() {
        return _c.compileRule();
    }

    public long getRuleSeed() {
        return _c.getRule().hashCode();
    }

    public Plane getPlane() {
        return _p;
    }

    public void setCA(CA ca) {
        setCA(ca, Pools.prelude(), _gopt);
    }

    public void setCA(CA ca, ExecutorService pool, GOptions opt) {
        _c = ca;
        _rend = new Rendering()
            .composition(Rendering.Composition.from(_config.getVariable("composite_mode","front")));
        setPlane(_c.createPlane(pool, opt));
        Info i = new Info(_c);
        Rule r = ca.getRule();
        String text = r.humanize();
        _meta.setToolTipText("");
        if(text.length()>42) {
            _meta.setToolTipText(text);
            text = "..."+text.substring(text.length()-42,text.length());
        }
        _meta.setText(text);
    }

    public void setPlane(Plane plane) {
        if(useNew) {
            setPlaneNew(plane);
        }
        else {
            setPlaneOld(plane);
        }
    }

    public void setPlaneNew(Plane plane) {
        _p = plane;
        final Image b;
        if(_scale==1f) {
            b = plane.toBufferedImage();
        }
        else {
            b = _p.toImage((int)(_p.getWidth()*_scale), (int) (_p.getHeight()*_scale));
        }
        final Runnable r = new Runnable() {
            public void run() {
                _img.setImage(b);
                invalidate();
            }
        };
        if(SwingUtilities.isEventDispatchThread()) {
            r.run();
        }
        else {
            SwingUtilities.invokeLater(r);
        }
    }

    private final boolean FAIL3D = false;
    public void setPlaneOld(Plane plane) {
        _p = plane;
        final Runnable r;
        if(FAIL3D && plane instanceof BlockPlane) {
            r = new Runnable() {
                public void run() {
                    _label.setForeground(Color.WHITE);
                    _label.setText("Cannot display BlockPlane");
                    invalidate();
                }
            };
        }
        else {
            ImageIcon i;
            if(_scale==1f) {
                i = new ImageIcon(_p.toImage(_rend));
            }
            else {
                i = new ImageIcon(_p.toImage(_rend, (int)(_p.getWidth()*_scale), (int) (_p.getHeight()*_scale)));
            }
            final ImageIcon icon = i;
            r = new Runnable() {
                public void run() {
                    _label.setIcon(icon);
                    _label.setText("");
                    invalidate();
                }
            };
        }
        if(SwingUtilities.isEventDispatchThread()) {
            r.run();
        }
        else {
            SwingUtilities.invokeLater(r);
        }
    }

    public void setScale(float scale) {
        if(_scale!=scale) {
            _scale = scale;
            if(_p!=null) {
                setPlane(_p);
            }
        }
    }

    public float getScale() {
        return _scale;
    }

    @Override public void setAnimationsEnabled(boolean e) {
    }

    @Override public boolean getAnimationsEnabled() {
        return false;
    }

    @Override public Rendering getRendering() {
        return _rend;
    }

    public int getCAWidth() {
        return _w;
    }

    public int getCAHeight() {
        return _h;
    }

    public void setCAWidth(int w) {
        _w = w;
    }

    public void setCAHeight(int h) {
        _h = h;
    }

    public void reroll(Initializer i) {
        if(i==null) {
            throw new IllegalArgumentException("null initializer");
        }
        _c.setInitializer(i);
        setPlane(_c.createPlane(Pools.prelude(), _gopt));
    }

    public void generate(Initializer i) {
        _c.setInitializer(i);
        setPlane(_c.createPlane(Pools.prelude(), _gopt));
    }

    @Override public void save(String file, Rendering r) throws java.io.IOException {
        getPlane().save(file, r);
    }
}
