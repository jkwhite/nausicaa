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
import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
import javafx.scene.SceneAntialiasing;
import javafx.scene.PerspectiveCamera;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.application.ConditionalFeature;
import javafx.geometry.Point3D;


public class JfxPlaneDisplay extends PlaneDisplay {
    private static final double SCALE = 0.1f;

    private JLabel _label;
    private JFXPanel _img;
    private JScrollPane _show;
    private boolean _shown = true;
    private Rule _r;
    private CA _c;
    private Plane _p;
    private int _w;
    private int _h;
    private int _d;
    private float _scale = 1.0f;
    private Group _root;
    private Group _parent;
    private JfxCA _jfxCa;
    private volatile int _queue;


    public JfxPlaneDisplay(int w, int h, int d) {
        _w = w;
        _h = h;
        _d = d;
        setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        setForeground(Color.BLACK);
        setBackground(Color.BLACK);
        p.setBackground(Color.BLACK);
        p.setForeground(Color.BLACK);
        _label = new JLabel("NOTHING", javax.swing.SwingConstants.CENTER);
        _label.setBackground(Color.BLACK);
        _label.setForeground(Color.BLACK);
        _img = new JFXPanel();
        _img.setPreferredSize(new Dimension(w, h));
        p.add(_img, BorderLayout.CENTER);
        JScrollPane scr = new JScrollPane(p);
        scr.setBackground(Color.BLACK);
        scr.setForeground(Color.BLACK);
        add(scr, BorderLayout.CENTER);
        _show = scr;
        Platform.runLater(()->{ initScene(); });
    }

    public JfxPlaneDisplay(CA ca) {
        this(ca.getWidth(), ca.getHeight(), ca.getDepth());
        setCA(ca);
    }

    public JfxPlaneDisplay(Plane p) {
        this(p.getWidth(), p.getHeight(), p.creator().getDepth());
        setCA(p.creator());
    }

    private void initScene() {
        _root = new Group();
        Scene s = new Scene(_root, 400, 400, true, SceneAntialiasing.DISABLED);
        s.setFill(javafx.scene.paint.Color.BLACK);
        s.setCamera(new PerspectiveCamera());

        Group parent = new Group();
        if(_w<60) {
            parent.setTranslateX(200);
            parent.setTranslateY(100);
        }
        else {
            parent.setTranslateX(400);
            parent.setTranslateY(200);
        }
        parent.setTranslateZ(50);
        parent.getTransforms().add(new Rotate(-45, new Point3D(1,0,0)));
        RotateTransition t = new RotateTransition(Duration.millis(36000), parent);
        t.setByAngle(360);
        t.setAxis(new Point3D(1,0,0));
        t.setCycleCount(t.INDEFINITE);
        t.play();
        _root.getChildren().add(parent);
        _parent = parent;
        _img.setScene(s);
    }

    public JLabel getLabel() {
        return _label;
    }

    public JComponent getDisplayComponent() {
        return _img;
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

    public long getRuleSeed() {
        return _c.getRule().hashCode();
    }

    public Plane getPlane() {
        return _p;
    }

    public void setCA(CA ca) {
        if(ca==null) {
            throw new IllegalArgumentException("null ca");
        }
        _c = ca;
        if(Platform.isFxApplicationThread()) {
            _c = ca;
            if(_jfxCa!=null) {
                _parent.getChildren().remove(_jfxCa);
            }
            _jfxCa = new JfxCA(ca, _scale*4, _d);
            _parent.getChildren().add(_jfxCa);
            RotateTransition t = new RotateTransition(Duration.millis(72000), _jfxCa);
            t.setByAngle(360);
            t.setCycleCount(t.INDEFINITE);
            t.play();
            Platform.runLater(()->{_jfxCa.clear();});
            setPlane(_c.createPlane());
        }
        else {
            Platform.runLater(()->{setCA(ca);});
        }
    }

    public void setPlane(final Plane plane) {
        _p = plane;
        _c = plane.creator();
        if(_queue<2) {
            Runnable r = new Runnable() {
                public void run() {
                    _jfxCa.addPlane(plane);
                    _queue--;
                }
            };
            Platform.runLater(r);
            _queue++;
        }
    }

    public void setScale(float scale) {
        if(_scale!=scale) {
            _scale = scale;
            setCA(_c);
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
