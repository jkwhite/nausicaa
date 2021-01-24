package org.excelsi.nausicaa;


//import org.excelsi.nausicaa.ca.*;
import org.excelsi.nausicaa.ca.Varmap;
import org.excelsi.nausicaa.ifs.*;
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
import javafx.scene.transform.Affine;
import javafx.application.ConditionalFeature;
import javafx.geometry.Point3D;
import javafx.event.EventHandler;
import javafx.animation.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;

import org.fxyz.cameras.CameraTransformer;
import org.fxyz.cameras.AdvancedCamera;
import org.fxyz.cameras.controllers.FPSController;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class JfxIteratedFunction extends JfxWorld {
    private static final Logger LOG = LoggerFactory.getLogger(NViewer.class);

    private Canvas _c;
    private GraphicsContext _g;
    private IteratedFunction _ifs;
    private Animate _anim;

    public JfxIteratedFunction(int w, int h, int d, boolean useBorder) {
        super(w, h, d, useBorder);
    }

    @Override public void initScene() {
        super.initScene();
        _c = new Canvas(3*_w, 3*_h);
        _rotParent.getChildren().add(_c);
        _g = _c.getGraphicsContext2D();
        _g.setStroke(javafx.scene.paint.Color.WHITE);
        Random r = new Random();
        //ifs1();
        //ifs4();
        //ifs3(2);
        //_ifs = createIfs3();
        _ifs = new IteratedFunctionFactory("ifs3", "abcd")
            .createIfs(new Varmap());
        //new Animate().start();
    }

    /*
    class Save implements Op {
        @Override public void op(GraphicsContext g) {
            g.save();
        }
    }

    class Restore implements Op {
        @Override public void op(GraphicsContext g) {
            g.restore();
        }
    }
    */

    class Animate extends Thread {
        private final IteratedFunction _ifs;


        public Animate(IteratedFunction ifs) {
            _ifs = ifs;
            setDaemon(true);
        }

        @Override public void run() {
            Platform.runLater(()->{
                _ifs.initialize(_g, _w, _h);
                _ifs.render(_g, _w, _h);
            });
            int i=0;
            LOG.info("starting animation");
            while(!isInterrupted()) {
                if(++i>_ifs.getMaxIter()) {
                    break;
                }
                try {
                    Thread.sleep(5000);
                }
                catch(InterruptedException e) {
                    break;
                }
                _ifs.iterate();
                Platform.runLater(()->{
                    _g.clearRect(0, 0, _w, _h);
                    _ifs.render(_g, _w, _h);
                });
            }
            LOG.info("stopped animation");
        }
    }

    /*
    private IteratedFunction createIfs3() {
        return new IteratedFunction(
            new TreeTape.Op[]{new FillOval(0,0,2,2)},
            new TreeTape.Op[]{
                new Scale(-0.95,0.95).withFork(true),
                //new Scale(-1.11,1.11).withFork(true),
                new Translate(50,10),
                new Rotate(170,0.0)
            }, 17);
    }
    */

    //* = in-place
    //square    - 1 sqr - sxywh
    //translate*- 1 sqr - sxywh mzw* => szwwh (otherwise sxywh szwwh)
    //dupe      - 2 sqr - szwwh d => szwwh szwwh
    //nex       - 2 sqr - szwwh szwwh ne0 => [szwwh, s-zwwh]
    //nex       - 2 sqr - szwwh ne0 => [szwwh, s-zwwh]
    //rot-90    - 2 sqr - [szwwh, s-zwwh] => [szwwh r90, s-zwwh r90]

    //square    - 1 sqr - sxywh

    //translate*- 1 sqr - sxywh => mzw sxywh (otherwise sxywh szwwh)
    //sclx-1    - 2 sqr - szwwh scl => [szwwh, sclx-1 szwwh]
    //rot-90*   - 2 sqr - [szwwh, sclx-1 s-zwwh] => [r90 szwwh, r90 sclx-1 s-zwwh]

    //translate*- 1 sqr - [mzw r90 szwwh, mzw r90 sclx-1 s-zwwh]
    //sclx-1    - 2 sqr - [mzw r90 szwwh, sclx-1 mzw r90 szwwh, mzw r90 sclx-1 s-zwwh]
    //rot-90*   - 2 sqr - [szwwh, sclx-1 s-zwwh] => [szwwh r90, s-zwwh sclx-1 r90]
    private void ifs3(int it) {
        /*

        _g.setFill(javafx.scene.paint.Color.WHITE);
        List<Op> ops = new ArrayList<>();
        ops.add(new Scale(-0.95,0.95).withFork(true));
        //ops.add(new Scale(-1.11,1.11).withFork(true));
        ops.add(new Rotate(190));
        ops.add(new Translate(80,0));
        TreeTape tape = new TreeTape(new FillOval(0,0,1,1));

        LOG.info("bulding tape");
        for(int i=0;i<16;i++) {
            System.err.print(".");
            for(Op op:ops) {
                List<TreeTape.TreeNode> leaves = tape.getLeaves();
                LOG.info("leaves: "+leaves.size());
                for(TreeTape.TreeNode leaf:leaves) {
                    if(op.isFork()) {
                        op.op(leaf.fork());
                    }
                    else {
                        op.op(leaf);
                    }
                }
            }
        }
        //LOG.info("final tape: "+tape);
        Affine a = null;
        a = _g.getTransform(a);
        a.appendTranslation(_w/2, _h/2);
        _g.setTransform(a);
        for(TreeTape.TreeNode leaf:tape.getLeaves()) {
            _g.save();
            for(TreeTape.Op op:leaf.getTape().getOps()) {
                ((Op)op).op(_g);
            }
            _g.restore();
        }
        */
    }
    /*
    private void ifs3(int it) {
        State s = new State();
        _g.setFill(javafx.scene.paint.Color.WHITE);
        List<Op> ops = new ArrayList<>();
        //ops.add(new Save());
        ops.add(new Translate(100,10));
        //ops.add(new StrokeRect(350,350,100,100));
        ops.add(new FillOval(350,350,100,100));
        ops.add(new Rotate(25));
        ops.add(new Scale(0.92, 0.92));
        //ops.add(new Restore());
        //ops.add(new Save());
        ops.add(new Translate(-100,10));
        //ops.add(new StrokeRect(350,350,100,100));
        ops.add(new FillOval(350,350,100,100));
        ops.add(new Rotate(25));
        ops.add(new Scale(0.92, 0.92));
        //ops.add(new Restore());
        for(int i=0;i<it;i++) {
            for(Op o:ops) {
                o.op(_g);
            }
        }
    }
    */

    private void ifs4() {
        Affine a = null;
        a = _g.getTransform(a);
        a.appendTranslation(_w/2, _h/2);
        _g.setTransform(a);
        for(int i=0;i<100;i++) {
            _g.setFill(javafx.scene.paint.Color.WHITE);
            _g.fillRoundRect(200, 0, 30, 30, 20, 20);
            //a.appendTranslation(100, 0);
            //_g.setTransform(a);
            _g.scale(-1.0, 1.0);
            _g.setFill(javafx.scene.paint.Color.BLUE);
            _g.fillRoundRect(200, 0, 30, 30, 20, 20);
            _g.scale(0.95, 0.95);
            a = _g.getTransform(a);
            a.appendRotation(30, 10, 10);
            //a.appendRotation(70, 0, 0);
            _g.setTransform(a);
            //_g.rotate(90);
        }
    }

    private void ifs2() {
        _g.setFill(javafx.scene.paint.Color.WHITE);
        Affine a = null;
        for(int i=0;i<77;i++) {
            _g.fillRoundRect(_w/2, _h/2, 100, 100, 20, 20);
            a = _g.getTransform(a);
            a.appendRotation(10, _w/2, _h/2);
            _g.setTransform(a);
            _g.scale(-0.95, -0.95);
        }
    }

    private void ifs1() {
        Affine a = null;
        for(int i=0;i<77;i++) {
            _g.strokeLine(_w/2-300,_h/2-300,_w/2+300,_h/2+300);
            //_c.setRotate(Math.PI/8d);
            //_g.rotate(5);
            a = _g.getTransform(a);
            a.appendRotation(10, _w/2, _h/2);
            _g.setTransform(a);
            _g.scale(0.95, 0.95);
        }
    }

    @Override public void scaleUp() {
    }

    @Override public void scaleDown() {
    }

    @Override public synchronized void toggleAnimate() {
        if(_anim!=null&&_anim.isAlive()) {
            _anim.interrupt();
        }
        else {
            _anim = new Animate(_ifs);
            _anim.start();
        }
    }

    @Override public void setRender(Render render) {
    }
}
