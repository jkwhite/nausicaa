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
import javafx.scene.Camera;
import javafx.scene.ParallelCamera;
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


public abstract class JfxWorld {
    public enum Render { cells, bounds, mesh, single_mesh, cube_mesh, blob_mesh, best };
    protected static final boolean HANDLE_UNLOCK = true;
    private static final double SCALE = 0.1f;

    protected int _w;
    protected int _h;
    protected int _d;
    protected float _scale = 1.5f;
    //private CA _c;
    //private Plane _p;
    protected Group _parent;
    protected Group _rotParent;
    protected Group _root;
    private Scene _scene;
    //private JfxCA _jfxCa;
    private final boolean _useBorder;
    private BorderPane _border;
    private List<RotateTransition> _rots;
    //private volatile int _queue;


    public JfxWorld(int w, int h, int d, boolean useBorder) {
        _w = w;
        _h = h;
        _d = d;
        _useBorder = useBorder;
    }

    public Scene getScene() {
        return _scene;
    }

    public BorderPane getBorder() {
        return _border;
    }

    abstract public void scaleUp();

    abstract public void scaleDown();

    abstract public void toggleAnimate();

    abstract public void setRender(Render render);

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
            s = new Scene(bp, 2*_w, 2*_h, _d>0?true:false, SceneAntialiasing.BALANCED);
            _root = g;
            _border = bp;
        }
        else {
            _root = new Group();
            s = new Scene(_root, _w, _h, true, SceneAntialiasing.BALANCED);
        }
        s.setFill(javafx.scene.paint.Color.BLACK);
        Camera cam;
        if(_d>0) {
            cam = new PerspectiveCamera(false);
        }
        else {
            cam = new ParallelCamera();
        }
        cam.setFarClip(10000);
        cam.setNearClip(0);
        s.setCamera(cam);

        /*
        Group move = new Group();
        CameraTransformer t = new CameraTransformer();
        t.getChildren().add(cam);
        move.getChildren().add(t);
        _root.getChildren().add(move);

        move.setTranslateX(_w/2);
        move.setTranslateX(_h/2);
        move.setTranslateZ(_d);
        */

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

        //Group camGroup = move;
        Group camGroup = _rotParent;
        final CameraAnimator forwardX = new CameraAnimator(()->camGroup.setTranslateX(camGroup.getTranslateX()-INC));
        final CameraAnimator forwardY = new CameraAnimator(()->camGroup.setTranslateY(camGroup.getTranslateY()-INC));
        final CameraAnimator forwardZ = new CameraAnimator(()->camGroup.setTranslateZ(camGroup.getTranslateZ()-INC));
        final CameraAnimator backwardX = new CameraAnimator(()->camGroup.setTranslateX(camGroup.getTranslateX()+INC));
        final CameraAnimator backwardY = new CameraAnimator(()->camGroup.setTranslateY(camGroup.getTranslateY()+INC));
        final CameraAnimator backwardZ = new CameraAnimator(()->camGroup.setTranslateZ(camGroup.getTranslateZ()+INC));

        /*
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
        */

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
                    //case LEFT:
                        //rotLeft.start();
                        //break;
                    //case RIGHT:
                        //rotRight.start();
                        //break;
                    //case UP:
                        //rotUp.start();
                        //break;
                    //case DOWN:
                        //rotDown.start();
                        //break;
                    //case O:
                        //rotFront.start();
                        //break;
                    //case P:
                        //rotBack.start();
                        //break;
                    case B:
                        System.err.println("pos: x="+camGroup.getTranslateX()+", y="+camGroup.getTranslateY()+", z="+camGroup.getTranslateZ());
                        //System.err.println("rot: x="+rotateX.getRx()+"y="+rotateY.getRy()+", z="+rotateZ.getRz());
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
                    //case LEFT:
                        //rotLeft.stop();
                        //break;
                    //case RIGHT:
                        //rotRight.stop();
                        //break;
                    //case UP:
                        //rotUp.stop();
                        //break;
                    //case DOWN:
                        //rotDown.stop();
                        //break;
                    //case O:
                        //rotFront.stop();
                        //break;
                    //case P:
                        //rotBack.stop();
                        //break;
                }
            }
        });
    }
}
