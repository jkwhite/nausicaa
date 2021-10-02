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


public class JfxCaWorld extends JfxWorld implements PlanescapeProvider, Planescape {
    private JfxCA _jfxCa;
    private CA _c;
    private Plane _p;
    private volatile int _queue;


    public JfxCaWorld(int w, int h, int d, boolean useBorder, boolean useRegion) {
        super(w, h, d, useBorder, useRegion);
    }

    @Override public void load(File selectedFile) {
        if(selectedFile.getName().endsWith(".ca")) {
            try {
                CA ca = CA.fromFile(selectedFile.toString(), "text");
                System.err.println("PRELUDE: "+ca.getPrelude());
                //ca = ca.prelude(10);
                setCA(ca);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override public void save(File f) {
    }

    @Override public Plane getPlane() {
        return _jfxCa.getLastPlane();
    }

    @Override public Rule getRule() {
        return _jfxCa.getRule();
    }

    @Override public Rule compileRule() {
        return _jfxCa.compileRule();
    }

    @Override public boolean delegateUnlock() {
        return true;
    }

    @Override public Planescape[] getPlanescapes() {
        return new Planescape[]{this};
    }

    @Override public void setPlane(final Plane plane) {
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

    @Override public void scaleUp() {
        _jfxCa.setScale(_jfxCa.getScale()*1.2);
    }

    @Override public void scaleDown() {
        _jfxCa.setScale(_jfxCa.getScale()*0.8);
    }

    private Animation _anim;
    @Override public void toggleAnimate() {
        if(_anim==null) {
            _anim = new Animation(new Config(_jfxCa.getCA().getWidth(), _jfxCa.getCA().getHeight(), _jfxCa.getCA().getDepth(), _jfxCa.getCA().getWeight()), this, new Timeline(), -1);
            _anim.start();
        }
        else {
            _anim.stopAnimation();
            _anim = null;
        }
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
            _jfxCa = new JfxCA(ca, sc, _d, JfxWorld.Render.best);
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

    @Override public void setRender(JfxWorld.Render render) {
        _jfxCa.setRender(render);
    }
}
