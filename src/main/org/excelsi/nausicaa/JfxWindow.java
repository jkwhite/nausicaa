package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.CA;
import org.excelsi.nausicaa.ca.Plane;
import org.excelsi.nausicaa.ca.GOptions;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

import javafx.geometry.Point3D;
import javafx.scene.SceneAntialiasing;
import javafx.scene.PerspectiveCamera;
import javafx.application.Application;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import javafx.application.Platform;


public class JfxWindow extends Application {
    private static CA _ca;
    private Group _root;
    private JfxCA _jfxCa;
    private ScheduledExecutorService _sched = Executors.newScheduledThreadPool(1);
    private ExecutorService _pool = Executors.newFixedThreadPool(1);


    public static void setCA(CA ca) {
        _ca = ca;
    }

    public void start(Stage stage) {
        //Circle circ = new Circle(40, 40, 30);
        //_root = new Group(circ);
        _root = new Group();
        //Sphere sphere = new Sphere(30);
        //sphere.setTranslateZ(10);
        //_root.getChildren().add(sphere);
        //Scene scene = new Scene(root, 400, 300);
        Scene s = new Scene(_root, 1000, 600, true, SceneAntialiasing.DISABLED);
        s.setFill(javafx.scene.paint.Color.BLACK);
        s.setCamera(new PerspectiveCamera(false));

        Group parent = new Group();
        //parent.setTranslateZ(1);
        parent.setTranslateX(300);
        parent.setTranslateY(300);
        parent.getTransforms().add(new Rotate(-45, new Point3D(1,0,0)));

        _jfxCa = new JfxCA(_ca);
        final Plane p = _ca.createPlane();
        _jfxCa.addPlane(p);
        parent.getChildren().add(_jfxCa);
        _root.getChildren().add(parent);

        RotateTransition t = new RotateTransition(Duration.millis(36000), _jfxCa);
        t.setByAngle(360);
        t.setCycleCount(t.INDEFINITE);
        t.play();

        stage.setTitle("CAViewer");
        stage.setScene(s);
        stage.show();

        final Iterator<Plane> frames = _ca.getRule().frameIterator(p, _pool, new GOptions(true, 1, 1, 1f));
        _sched.scheduleAtFixedRate(()->{
                final Plane next = frames.next();
                Platform.runLater(()->{_jfxCa.addPlane(next);});
            }, 500, 500, TimeUnit.MILLISECONDS);
    }
}
