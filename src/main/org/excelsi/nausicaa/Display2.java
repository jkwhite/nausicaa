package org.excelsi.nausicaa;


import org.excelsi.ca.*;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
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


public class Display2 extends JComponent {
    private JLabel _label;
    private JScrollPane _show;
    private boolean _shown = true;
    private Rule _r;
    private Branch<World> _b;
    private CA2 _c;
    private int _w;
    private int _h;
    private float _scale = 1.0f;


    public Display2(int w, int h) {
        _w = w;
        _h = h;
        setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        setForeground(Color.BLACK);
        setBackground(Color.BLACK);
        p.setBackground(Color.BLACK);
        p.setForeground(Color.BLACK);
        _label = new JLabel("", javax.swing.SwingConstants.CENTER);
        _label.setBackground(Color.BLACK);
        _label.setForeground(Color.BLACK);
        p.add(_label);
        //JScrollPane scr = new JScrollPane(_label);
        JScrollPane scr = new JScrollPane(p);
        scr.setBackground(Color.BLACK);
        scr.setForeground(Color.BLACK);
        add(scr, BorderLayout.CENTER);
        _show = scr;
    }

    public Display2(CA2 ca) {
        this(ca.getWidth(), ca.getHeight());
        _scale = ca.getScale();
        setCA(ca);
    }

    public Display2(int w, int h, Rule r) {
        this(w, h);
        _r = r;
    }

    public Display2(int w, int h, Branch<World> b) {
        this(w, h);
        _b = b;
        _label.requestFocus();
    }

    public Branch<World> getBranch() {
        return _b;
    }

    public Rule getRule() {
        return getBranch()!=null?getBranch().data().getRule():_r;
    }

    public void setBranch(Branch<World> b) {
        _b = b;
    }

    public void setRule(Rule r) {
        _b = null;
        _r = r;
    }

    public JLabel getLabel() {
        return _label;
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

    public CA2 getCA() {
        return _c;
    }

    public void setCA(CA2 ca) {
        _c = ca;
        ImageIcon i;
        if(_scale==1f) {
            i = new ImageIcon(ca.toImage());
        }
        else {
            i = new ImageIcon(ca.toImage((int)(ca.getWidth()*_scale), (int) (ca.getHeight()*_scale)));
        }
        final ImageIcon icon = i;
        Runnable r = new Runnable() {
            public void run() {
                _label.setIcon(icon);
                _label.setText("");
                invalidate();
            }
        };
        /*
        final Image img = ca.toImage();
        Runnable r = new Runnable() {
            public void run() {
                ImageIcon ii = (ImageIcon) _label.getIcon();
                if(ii==null) {
                    ii = new ImageIcon(img);
                    _label.setIcon(ii);
                }
                else {
                    ii.setImage(img);
                    _label.repaint();
                }
            }
        };
        */
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
            if(_c!=null) {
                setCA(_c);
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
}
