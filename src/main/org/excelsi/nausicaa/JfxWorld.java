package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
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
import javafx.event.EventHandler;
import javafx.animation.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;

import org.fxyz.cameras.CameraTransformer;
import org.fxyz.cameras.AdvancedCamera;
import org.fxyz.cameras.controllers.FPSController;


public class JfxWorld implements PlanescapeProvider, Planescape {
    private static final boolean HANDLE_UNLOCK = true;
    private static final double SCALE = 0.1f;

    private int _w;
    private int _h;
    private int _d;
    private float _scale = 1.5f;
    private CA _c;
    private Plane _p;
    private Group _root;
    private Group _parent;
    private Group _rotParent;
    private Scene _scene;
    private JfxCA _jfxCa;
    private final boolean _useBorder;
    private BorderPane _border;
    private List<RotateTransition> _rots;
    private volatile int _queue;


    public JfxWorld(int w, int h, int d, boolean useBorder) {
        _w = w;
        _h = h;
        _d = d;
        _useBorder = useBorder;
    }

    @Override public Plane getPlane() {
        return _jfxCa.getLastPlane();
    }

    /*
    @Override public void setPlane(final Plane p) {
        if(Platform.isFxApplicationThread()) {
            _jfxCa.addPlane(p);
            p.delegateUnlock();
        }
        else {
            Platform.runLater(()->setPlane(p));
        }
    }
    */

    @Override public Rule getRule() {
        return _jfxCa.getRule();
    }

    @Override public boolean delegateUnlock() {
        return true;
    }

    @Override public Planescape[] getPlanescapes() {
        return new Planescape[]{this};
    }

    public Scene getScene() {
        return _scene;
    }

    public BorderPane getBorder() {
        return _border;
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
            final double sc = _scale*8;
            _jfxCa = new JfxCA(ca, sc, _d, JfxCA.Render.best);
            //_parent.getChildren().add(_jfxCa);
            _rotParent.getChildren().add(_jfxCa);

            _jfxCa.setTranslateX(ca.getWidth()*sc/2);
            _jfxCa.setTranslateY(ca.getHeight()*sc/2);
            _jfxCa.setTranslateZ(ca.getDepth()*sc/2);
            //_rotParent.setTranslateX(-ca.getWidth()*sc/2);
            //_rotParent.setTranslateY(-ca.getHeight()*sc/2);
            //_rotParent.setTranslateZ(-ca.getDepth()*sc/2);
            //RotateTransition t = new RotateTransition(Duration.millis(72000), _jfxCa);
            //t.setByAngle(360);
            //t.setCycleCount(t.INDEFINITE);
            //t.play();

            // ????? Platform.runLater(()->{_jfxCa.clear();});
            ExecutorService comp = Pools.named("compute", 4);
            try {
                setPlane(_c.createPlane(comp, new GOptions(true, 4, 0, 1f)));
            }
            finally {
                comp.shutdown();
            }
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
                    try {
                        _jfxCa.addPlane(plane);
                        _queue--;
                    }
                    finally {
                        if(HANDLE_UNLOCK) {
                            plane.unlockRead();
                        }
                    }
                }
            };
            Platform.runLater(r);
            _queue++;
        }
        else {
            if(HANDLE_UNLOCK) {
                plane.unlockRead();
            }
        }
    }

    public void setRender(JfxCA.Render render) {
        _jfxCa.setRender(render);
    }

    public void scaleUp() {
        _jfxCa.setScale(_jfxCa.getScale()*1.2);
    }

    public void scaleDown() {
        _jfxCa.setScale(_jfxCa.getScale()*0.8);
    }

    private Animation _anim;
    public void toggleAnimate() {
        if(_anim==null) {
            _anim = new Animation(new Config(_jfxCa.getCA().getWidth(), _jfxCa.getCA().getHeight(), _jfxCa.getCA().getDepth(), _jfxCa.getCA().getWeight()), this, new Timeline(), -1);
            _anim.start();
        }
        else {
            _anim.stopAnimation();
            _anim = null;
        }
    }

    private boolean _rotsEnabled;
    public void toggleRotation() {
        for(RotateTransition t:_rots) {
            if(_rotsEnabled) {
                t.pause();
            }
            else {
                t.play();
            }
        }
        _rotsEnabled = !_rotsEnabled;
    }

    public void initScene() {
        final double INC = 15d;
        Scene s;
        if(_useBorder) {
            BorderPane bp = new BorderPane();
            bp.setStyle("-fx-background-color: transparent;");
            Group g = new Group();
            bp.setCenter(g);
            s = new Scene(bp, 800, 800, true, SceneAntialiasing.BALANCED);
            _root = g;
            _border = bp;
        }
        else {
            _root = new Group();
            s = new Scene(_root, 800, 800, true, SceneAntialiasing.BALANCED);
        }
        s.setFill(javafx.scene.paint.Color.BLACK);
        final PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.setFarClip(10000);
        s.setCamera(cam);

        Group move = new Group();
        CameraTransformer t = new CameraTransformer();
        t.getChildren().add(cam);
        move.getChildren().add(t);
        _root.getChildren().add(move);

        //move.setTranslateX(2000);
        //move.setTranslateY(700);
        //move.setTranslateZ(6000);

        //move.getTransforms().add(new Rotate(-45, new Point3D(1,0,0)));
        //pos: x=2070.0, y=665.0, z=6200.0
        //rot: x=-10.5y=183.0, z=0.0

        Group camGroup = move;
        final CameraAnimator forwardX = new CameraAnimator(()->camGroup.setTranslateX(camGroup.getTranslateX()-INC));
        final CameraAnimator forwardY = new CameraAnimator(()->camGroup.setTranslateY(camGroup.getTranslateY()-INC));
        final CameraAnimator forwardZ = new CameraAnimator(()->camGroup.setTranslateZ(camGroup.getTranslateZ()-INC));
        final CameraAnimator backwardX = new CameraAnimator(()->camGroup.setTranslateX(camGroup.getTranslateX()+INC));
        final CameraAnimator backwardY = new CameraAnimator(()->camGroup.setTranslateY(camGroup.getTranslateY()+INC));
        final CameraAnimator backwardZ = new CameraAnimator(()->camGroup.setTranslateZ(camGroup.getTranslateZ()+INC));

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

        camGroup.setTranslateX(2000);
        camGroup.setTranslateY(700);
        camGroup.setTranslateZ(3000);
        rotateY.setRy(180);
        rotateX.setRx(-10);
        rotateZ.setRz(0);

        s.setOnKeyPressed(new EventHandler<javafx.scene.input.KeyEvent>() {
            public void handle(javafx.scene.input.KeyEvent e) {
                switch(e.getCode()) {
                    case D:
                        forwardX.start();
                        break;
                    case W:
                        forwardZ.start();
                        break;
                    case Q:
                        forwardY.start();
                        break;
                    case A:
                        backwardX.start();
                        break;
                    case S:
                        backwardZ.start();
                        break;
                    case Z:
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
                    case B:
                        System.err.println("pos: x="+camGroup.getTranslateX()+", y="+camGroup.getTranslateY()+", z="+camGroup.getTranslateZ());
                        System.err.println("rot: x="+rotateX.getRx()+"y="+rotateY.getRy()+", z="+rotateZ.getRz());
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
                    case Q:
                        forwardY.stop();
                        break;
                    case A:
                        backwardX.stop();
                        break;
                    case S:
                        backwardZ.stop();
                        break;
                    case Z:
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

        //Light.Point l = new Light.Point();
        //Light.Distant l = new Light.Distant();
        //Lighting li = new Lighting();
        //li.setLight(l);
        //li.setSurfaceScale(50.0);
        //parent.setEffect(li);

        List<RotateTransition> trans = new ArrayList<>();
        RotateTransition tr = new RotateTransition(Duration.millis(36000), parent);
        tr.setByAngle(360);
        tr.setCycleCount(tr.INDEFINITE);
        //tr.play();
        trans.add(tr);

        Group rotParent = new Group();
        //rotParent.getTransforms().add(new Rotate(-45, new Point3D(1,0,0)));
        parent.getChildren().add(rotParent);

        //RotateTransition tr2 = new RotateTransition(Duration.millis(48000), rotParent);
        //tr2.setByAngle(360);
        //tr2.setCycleCount(tr2.INDEFINITE);
        //tr2.play();

        _parent = parent;
        _rotParent = rotParent;
        _rots = trans;
        _rotsEnabled = false;
        _scene = s;
    }
}
