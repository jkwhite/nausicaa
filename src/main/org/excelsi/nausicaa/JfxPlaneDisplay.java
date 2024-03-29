package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
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
import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
import javafx.scene.SceneAntialiasing;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.ParallelCamera;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.application.ConditionalFeature;
import javafx.geometry.Point3D;
import javafx.event.EventHandler;
import javafx.animation.*;
import javafx.animation.Animation;

import org.fxyz.cameras.CameraTransformer;
import org.fxyz.cameras.AdvancedCamera;
import org.fxyz.cameras.controllers.FPSController;


public class JfxPlaneDisplay extends PlaneDisplay {
    private static final Logger LOG = LoggerFactory.getLogger(JfxPlaneDisplay.class);
    private static final boolean HANDLE_UNLOCK = false;
    private static final double SCALE = 0.1f;
    private static final float SCALE_MULT = 1f;

    private JLabel _label;
    private JFXPanel _img;
    private JScrollPane _show;
    private List<Animation> _animations = new ArrayList<Animation>();
    // private RotateTransition _animations;
    private boolean _shown = true;
    private Rule _r;
    private CA _c;
    private Plane _p;
    private GOptions _gopt;
    private Sizer _sizer;
    private int _w;
    private int _h;
    private int _d;
    private float _scale = 1.5f;
    private Group _root;
    private Group _parent;
    private Group _rotParent;
    private Group _rotParent2;
    private JfxCA _jfxCa;
    private View3dOptions _opts;
    private volatile int _queue;


    public JfxPlaneDisplay(Sizer sizer, int w, int h, int d, View3dOptions opts) {
        _sizer = sizer;
        _w = w;
        _h = h;
        _d = d;
        _opts = opts;
        _scale = _opts.scale();
        LOG.debug("CA size: "+_w+"x"+_h+"x"+_d+", scale: "+_scale);
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
        // _img.setPreferredSize(new Dimension(2*w, 2*h));
        Dimension sd = cellSize();
        _img.setPreferredSize(new Dimension(sd.width-50, sd.height-50));
        p.add(_img, BorderLayout.CENTER);
        // JScrollPane scr = new JScrollPane(p);
        // scr.setBackground(Color.BLACK);
        // scr.setForeground(Color.BLACK);
        // add(scr, BorderLayout.CENTER);
        // _show = scr;
        add(p, BorderLayout.CENTER);
        // _show = scr;
        Platform.runLater(()->{ initScene(); });
        Platform.setImplicitExit(false);
        LOG.debug("scheduled initScene");
    }

    public JfxPlaneDisplay(Sizer sizer, CA ca, GOptions gopt, View3dOptions opts) {
        this(sizer, ca.getWidth(), ca.getHeight(), ca.getDepth(), opts);
        _gopt = gopt;
        setCA(ca);
    }

    public JfxPlaneDisplay(Sizer sizer, Plane p, View3dOptions opts) {
        this(sizer, p.getWidth(), p.getHeight(), p.creator().getDepth(), opts);
        setCA(p.creator());
    }

    @Override public void setAnimationsEnabled(boolean e) {
        if(e) {
            for(Animation a:_animations) {
                a.play();
            }
        }
        else {
            for(Animation a:_animations) {
                a.pause();
            }
        }
        _opts.animate(e);
    }

    @Override public boolean getAnimationsEnabled() {
        return false;
    }

    private Dimension cellSize() {
        Dimension d = _sizer.getAppSize();
        if(!_opts.root()) {
            return new Dimension(d.width/3, d.height/3);
        }
        return d;
    }

    private void initScene() {
        System.err.println("running initScene");
        final double INC = 5d;
        _root = new Group();
        _root.setScaleX(_scale);
        _root.setScaleY(_scale);
        _root.setScaleZ(_scale);
        // final int sz = 300;
        final int sz = _opts.root()?900:300;
        Dimension sd = cellSize(); //_sizer.getAppSize();
        LOG.info("isRoot="+_opts.root());
        Scene s = new Scene(_root, sd.width, sd.height, true, SceneAntialiasing.BALANCED);
        s.setFill(javafx.scene.paint.Color.BLACK);
        final PerspectiveCamera cam = new PerspectiveCamera(false);
        // final ParallelCamera cam = new ParallelCamera();
        cam.setFarClip(12000);
        s.setCamera(cam);

        // cam.setTranslateX(-600);
        // cam.setTranslateY(-900);
        //cam.getTransforms().add(new Rotate(45, new Point3D(1,0,0)));

        Group rotParent2 = new Group();
        Group rotParent = new Group();
        Group parent = new Group();
        // parent.setTranslateX((int)(sz*.8));
        // parent.setTranslateY((int)(sz*.4));
        parent.setTranslateX(sd.width/2);
        parent.setTranslateY(sd.height/2);
        parent.setTranslateZ((int)(sz));
        parent.getTransforms().add(new Rotate(-45, new Point3D(1,0,0)));

        //_jfxCa = new JfxCA(_ca);
        //final Plane p = _ca.createPlane();
        //_jfxCa.addPlane(p);
        //parent.getChildren().add(_jfxCa);
        _root.getChildren().add(parent);
        _parent = parent;
        parent.getChildren().add(rotParent2);
        rotParent2.getChildren().add(rotParent);
        // parent.getChildren().add(rotParent);
        _rotParent = rotParent;

        // Sphere sph = new Sphere(20);
        // sph.setTranslateZ(20);
        // sph.setTranslateX(sd.width/2);
        // sph.setTranslateY(sd.height/2);
        // _rotParent.getChildren().add(sph);

        RotateTransition t = new RotateTransition(Duration.millis(36000), rotParent);
        t.setByAngle(360);
        t.setCycleCount(t.INDEFINITE);
        _animations.add(t);
        if(_opts.animate()) {
            t.play();
        }

        RotateTransition t2 = new RotateTransition(Duration.millis(76000), rotParent2);
        t2.setAxis(Rotate.X_AXIS);
        t2.setByAngle(360);
        t2.setCycleCount(t.INDEFINITE);
        _animations.add(t2);
        if(_opts.animate()) {
            t2.play();
        }

        /*
        Group move = new Group();
        CameraTransformer t = new CameraTransformer();
        t.getChildren().add(cam);
        move.getChildren().add(t);
        _root.getChildren().add(move);

        Group camGroup = move;
        final CameraAnimator forwardX = new CameraAnimator(()->camGroup.setTranslateX(camGroup.getTranslateX()+INC));
        final CameraAnimator forwardY = new CameraAnimator(()->camGroup.setTranslateY(camGroup.getTranslateY()+INC));
        final CameraAnimator forwardZ = new CameraAnimator(()->camGroup.setTranslateZ(camGroup.getTranslateZ()+INC));
        final CameraAnimator backwardX = new CameraAnimator(()->camGroup.setTranslateX(camGroup.getTranslateX()-INC));
        final CameraAnimator backwardY = new CameraAnimator(()->camGroup.setTranslateY(camGroup.getTranslateY()-INC));
        final CameraAnimator backwardZ = new CameraAnimator(()->camGroup.setTranslateZ(camGroup.getTranslateZ()-INC));

        CameraTransformer rotateX = t;
        CameraTransformer rotateY = t;
        CameraTransformer rotateZ = t;
        final double ROT = 1.5d;
        final CameraAnimator rotRight = new CameraAnimator(()->rotateY.setRy(rotateY.getRy()+ROT));
        final CameraAnimator rotLeft = new CameraAnimator(()->rotateY.setRy(rotateY.getRy()-ROT));
        final CameraAnimator rotUp = new CameraAnimator(()->rotateX.setRx(rotateX.getRx()+ROT));
        final CameraAnimator rotDown = new CameraAnimator(()->rotateX.setRx(rotateX.getRx()-ROT));
        final CameraAnimator rotFront = new CameraAnimator(()->rotateZ.setRz(rotateZ.getRz()+ROT));
        final CameraAnimator rotBack = new CameraAnimator(()->rotateZ.setRz(rotateZ.getRz()-ROT));

        s.setOnKeyPressed(new EventHandler<javafx.scene.input.KeyEvent>() {
            public void handle(javafx.scene.input.KeyEvent e) {
                switch(e.getCode()) {
                    case D:
                        forwardX.start();
                        break;
                    case W:
                        forwardZ.start();
                        break;
                    case Z:
                        forwardY.start();
                        break;
                    case A:
                        backwardX.start();
                        break;
                    case S:
                        backwardZ.start();
                        break;
                    case Q:
                        backwardY.start();
                        break;
                    case LEFT:
                        rotLeft.start();
                        break;
                    case RIGHT:
                        rotRight.start();
                        break;
                    case UP:
                        rotUp.start();
                        break;
                    case DOWN:
                        rotDown.start();
                        break;
                    case O:
                        rotFront.start();
                        break;
                    case P:
                        rotBack.start();
                        break;
                }
            }
        });
        s.setOnKeyReleased(new EventHandler<javafx.scene.input.KeyEvent>() {
            public void handle(javafx.scene.input.KeyEvent e) {
                switch(e.getCode()) {
                    case D:
                        forwardX.stop();
                        break;
                    case W:
                        forwardZ.stop();
                        break;
                    case Z:
                        forwardY.stop();
                        break;
                    case A:
                        backwardX.stop();
                        break;
                    case S:
                        backwardZ.stop();
                        break;
                    case Q:
                        backwardY.stop();
                        break;
                    case LEFT:
                        rotLeft.stop();
                        break;
                    case RIGHT:
                        rotRight.stop();
                        break;
                    case UP:
                        rotUp.stop();
                        break;
                    case DOWN:
                        rotDown.stop();
                        break;
                    case O:
                        rotFront.stop();
                        break;
                    case P:
                        rotBack.stop();
                        break;
                }
            }
        });

        Group parent = new Group();
        _root.getChildren().add(parent);
        _parent = parent;
        _img.setScene(s);
        */
        _img.setScene(s);
    }

    public boolean delegateUnlock() {
        return HANDLE_UNLOCK;
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
        setCA(ca, Pools.prelude(), _gopt!=null?_gopt:new GOptions(true,1,0,1f));
    }

    public void setCA(CA ca, ExecutorService pool, GOptions opt) {
        if(ca==null) {
            throw new IllegalArgumentException("null ca");
        }
        _c = ca;
        if(Platform.isFxApplicationThread()) {
            _c = ca;
            if(_jfxCa!=null) {
                //_parent.getChildren().remove(_jfxCa);
                _rotParent.getChildren().remove(_jfxCa);
            }
            _jfxCa = new JfxCA(ca, /*_scale* */SCALE_MULT, 40, JfxWorld.Render.blob_mesh);
            // _jfxCa.setTranslateX(ca.getWidth()*_scale*SCALE_MULT/2);
            // _jfxCa.setTranslateY(ca.getDepth()*_scale*SCALE_MULT/1.5);
            // _jfxCa.setTranslateZ(-ca.getHeight()*_scale*SCALE_MULT/1.5);
            _jfxCa.setTranslateX(ca.getWidth()*SCALE_MULT/2);
            _jfxCa.setTranslateY(ca.getDepth()*SCALE_MULT/1.5);
            _jfxCa.setTranslateZ(-ca.getHeight()*SCALE_MULT/1.5);
            //_parent.getChildren().add(_jfxCa);
            _rotParent.getChildren().add(_jfxCa);
            //RotateTransition t = new RotateTransition(Duration.millis(36000), _jfxCa);
            //t.setByAngle(360);
            //t.setCycleCount(t.INDEFINITE);
            //t.play();
            //RotateTransition t = new RotateTransition(Duration.millis(72000), _jfxCa);
            //t.setByAngle(360);
            //t.setCycleCount(t.INDEFINITE);
            //t.play();

            //Platform.runLater(()->{_jfxCa.clear();});
            //PAR1
            setPlane(_c.createPlane(pool, opt));
        }
        else {
            Platform.runLater(()->{setCA(ca, pool, opt);});
        }
    }

    public void setPlane(final Plane plane) {
        _p = plane;
        _c = plane.creator();
        if(_queue<2) {
            Runnable compute = _jfxCa.precompute(plane,
                new Runnable() {
                    public void run() {
                        try {
                            // _jfxCa.addPlane(plane);
                            // _jfxCa.renderPrecompute();
                            _queue--;
                        }
                        finally {
                            if(HANDLE_UNLOCK) {
                                plane.unlockRead();
                            }
                        }
                    }
                });
            // LOG.debug("queuing setPlane", new Exception());
            // Platform.runLater(r);
            Pools.adhoc().submit(compute);
            _queue++;
        }
        else {
            if(HANDLE_UNLOCK) {
                plane.unlockRead();
            }
        }
    }

    private ScaleTransition _scaler;
    public void setScale(float scale) {
        if(_scale!=scale) {
            double signed = 0.5d * (_scale < scale ? 1d : -1d);
            _scale = scale;
            // setCA(_c);
            if(_scaler==null) {
                _scaler = new ScaleTransition(Duration.millis(1000), _root);
            }
            if(_scaler.getStatus()==Animation.Status.RUNNING) {
                _scaler.stop();
            }
            _scaler.setFromX(_root.getScaleX());
            _scaler.setFromY(_root.getScaleY());
            _scaler.setFromZ(_root.getScaleZ());
            _scaler.setToX(_scale);
            _scaler.setToY(_scale);
            _scaler.setToZ(_scale);
            // _scaler.setByX(signed);
            // _scaler.setByY(signed);
            // _scaler.setByZ(signed);
            _scaler.play();
        }
    }

    private double computeScale() {
        return 0.5 * _scale;
    }

    public float getScale() {
        return _scale;
    }

    @Override public Rendering getRendering() {
        return new Rendering();
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
        setPlane(_c.createPlane(Pools.adhoc(), _gopt));
    }

    public void generate(Initializer i) {
        _c.setInitializer(i);
        setPlane(_c.createPlane(Pools.adhoc(), _gopt));
    }

    @Override public void save(String file, Rendering r) throws java.io.IOException {
        if(!file.endsWith(".png")) {
            file = file+".png";
        }
        JfxUtils.snap(new java.io.File(file), _img.getScene());
    }

    public void lookAt(Camera cam, Point3D cameraPosition, Point3D lookAtPos) {        
        //Create direction vector
        Point3D camDirection = lookAtPos.subtract(cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ());
        camDirection = camDirection.normalize();
          
        double xRotation = Math.toDegrees(Math.asin(-camDirection.getY()));
        double yRotation =  Math.toDegrees(Math.atan2( camDirection.getX(), camDirection.getZ()));
        
        Rotate rx = new Rotate(xRotation, cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ(), Rotate.X_AXIS);
        Rotate ry = new Rotate(yRotation, cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ(),  Rotate.Y_AXIS);
        
        cam.getTransforms().addAll( ry, rx, 
                new Translate(
                        cameraPosition.getX(), 
                        cameraPosition.getY(), 
                        cameraPosition.getZ()));
    }
}
