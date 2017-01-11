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
    //private Branch<World> _b;
    private CA _c;
    private Plane _p;
    private int _w;
    private int _h;
    private float _scale = 1.0f;
    private final boolean useNew = false;


    public SwingPlaneDisplay(CA ca) {
        this(ca.getWidth(), ca.getHeight());
        setCA(ca);
    }

    private SwingPlaneDisplay(int w, int h) {
        _w = w;
        _h = h;
        setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        //p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        setForeground(Color.BLACK);
        setBackground(Color.BLACK);
        p.setBackground(Color.BLACK);
        p.setForeground(Color.BLACK);
        _label = new JLabel("NOTHING", javax.swing.SwingConstants.CENTER);
        _label.setBackground(Color.BLACK);
        _label.setForeground(Color.BLACK);
        if(useNew) {
            _img = new JCA(w, h);
            //_img.setPreferredSize(new java.awt.Dimension(w,h));
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

        //JScrollPane scr = new JScrollPane(_label);
        JScrollPane scr = new JScrollPane(p);
        scr.setBackground(Color.BLACK);
        scr.setForeground(Color.BLACK);
        add(scr, BorderLayout.CENTER);
        _show = scr;
    }

    //public PlaneDisplay(CA ca) {
        //this(ca.getWidth(), ca.getHeight());
        //_scale = ca.getScale();
        //setCA(ca);
    //}

    private SwingPlaneDisplay(Plane p) {
        this(p.getWidth(), p.getHeight());
        setCA(p.creator());
        //setPlane(p);
        //_p = p;
    }

    private SwingPlaneDisplay(int w, int h, Rule r) {
        this(w, h);
        _r = r;
    }

    //public PlaneDisplay(int w, int h, Branch<World> b) {
        //this(w, h);
        //_b = b;
        //_label.requestFocus();
    //}

    //public Branch<World> getBranch() {
        //return _b;
    //}
//
    //public Rule getRule() {
        //return getBranch()!=null?getBranch().data().getRule():_r;
    //}
//
    //public void setBranch(Branch<World> b) {
        //_b = b;
    //}
//
    //public void setRule(Rule r) {
        //_b = null;
        //_r = r;
    //}

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
            //remove(_show);
        }
        else {
            _label.setIcon(_oldIcon);
            //add(_show);
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

    public long getRuleSeed() {
        return _c.getRule().hashCode();
    }

    public Plane getPlane() {
        return _p;
    }

    public void setCA(CA ca) {
        _c = ca;
        setPlane(_c.createPlane());
        Info i = new Info(_c);
        Rule r = ca.getRule();
        _meta.setText(r.humanize());
        /*
        if(r.dimensions()==1) {
            if(r.getHyperrule()!=null) {
                final RulePattern rp = new RulePattern(r, r.getHyperrule());
                Plane p = rp.generate(_c.getHeight(), _c.getPalette());
                _hyper.setIcon(new ImageIcon(p.toImage((int)(p.getWidth()*_scale), (int) (p.getHeight()*_scale))));
            }
            else {
                _hyper.setIcon(null);
            }
        }
        */
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

    public void setPlaneOld(Plane plane) {
        _p = plane;
        final Runnable r;
        if(plane instanceof BlockPlane) {
            r = new Runnable() {
                public void run() {
                    //_label.setIcon(icon);
                    _label.setForeground(Color.WHITE);
                    _label.setText("Cannot display BlockPlane");
                    invalidate();
                }
            };
        }
        else {
            ImageIcon i;
            if(_scale==1f) {
                i = new ImageIcon(_p.toImage());
            }
            else {
                i = new ImageIcon(_p.toImage((int)(_p.getWidth()*_scale), (int) (_p.getHeight()*_scale)));
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
        setPlane(_c.createPlane());
    }

    public void generate(Initializer i) {
        _c.setInitializer(i);
        setPlane(_c.createPlane());
    }
}
