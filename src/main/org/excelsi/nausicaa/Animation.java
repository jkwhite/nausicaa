package org.excelsi.nausicaa;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.excelsi.nausicaa.ca.Plane;
import org.excelsi.nausicaa.ca.Pools;
import org.excelsi.nausicaa.ca.GOptions;
import org.excelsi.nausicaa.ca.ComputeMode;


public class Animation extends Thread implements TimelineListener, ConfigListener {
    enum State { animate, pause, reinit, die };
    private final Config _config;
    private final PlanescapeProvider _f;
    private final Timeline _timeline;
    private final int _origSteps;
    private int _steps;
    private State _state = State.animate;
    private long _delay;
    private int _version;


    public Animation(Config config, PlanescapeProvider f, Timeline timeline, int steps) {
        super("Animation");
        _config = config;
        _f = f;
        _timeline = timeline;
        _origSteps = steps;
        _steps = steps;
        _timeline.addTimelineListener(this);
        _config.addListener(this);
        _delay = _config.getAnimationDelay();
        System.err.println("new animator");
    }

    @Override public void timelineChanged(TimelineEvent e) {
        switch(e.getType()) {
            case "tick":
                _state = State.pause;
                _steps = _origSteps;
                break;
            case "futures":
                _state = State.reinit;
                _steps = _origSteps;
                break;
            case "tock":
                _state = State.reinit;
                _steps = _origSteps;
                break;
        }
    }

    @Override public void configChanged(Config c, String property) {
        if("animationDelay".equals(property)) {
            _delay = c.getAnimationDelay();
            _state = State.reinit;
            _steps = _origSteps;
        }
    }

    public void stopAnimation() {
        System.err.println("stopping animator");
        _state = State.die;
        interrupt();
        while(isAlive()) {
            try {
                _state = State.die;
                Thread.sleep(25);
            }
            catch(InterruptedException e) {
                //break;
            }
        }
        System.err.println("stopped animator");
    }

    public void run() {
        try {
            while(!isInterrupted()&&_state!=State.die) {
                if(_state==State.animate) {
                    runFutures();
                }
                Thread.sleep(50);
                if(_state==State.reinit) {
                    _state = State.animate;
                }
            }
        }
        catch(InterruptedException e) {
        }
        finally {
            _timeline.removeTimelineListener(this);
            _config.removeListener(this);
        }
    }

    public void runFutures() {
        final int version = _version;
        Planescape[] ds = _f.getPlanescapes();
        DisplayAnimator[] da = new DisplayAnimator[ds.length];
        //ExecutorService compute = Executors.newFixedThreadPool(Math.min(4,ds.length));
        //ExecutorService render = Executors.newFixedThreadPool(Math.min(4,ds.length));

        //ExecutorService compute = Pools.named("compute", Math.min(4,ds.length));
        //ExecutorService render = Pools.named("render", Math.min(4,ds.length));
        int ccores = _config.getIntVariable("animation_computeCores", 2);
        int rcores = Math.min(ds.length, _config.getIntVariable("animation_renderCores", 2));
        float weight = _config.getFloatVariable("weight", 1f);
        System.err.println("using "+ccores+" compute cores and "+rcores+" render cores");
        ExecutorService compute = Pools.named("compute", ccores);
        ExecutorService render = Pools.named("render", rcores);
        for(int i=0;i<ds.length;i++) {
            da[i] = new DisplayAnimator(ds[i], compute, ds.length==1?ccores:1, weight);
        }
        try {
top:        while(_state==State.animate) {
                long start = System.currentTimeMillis();
                for(int i=0;i<da.length;i++) {
                    if(_state!=State.animate||isInterrupted()) {
                        break;
                    }
                    render.submit(da[i]);
                }
                long end = System.currentTimeMillis();
                if(_steps>0&&--_steps==0) {
                    _state = State.die;
                    try {
                        render.shutdown();
                        System.err.print("awaiting render... ");
                        render.awaitTermination(5, TimeUnit.SECONDS);
                        System.err.println("done");
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                long sleeps = start+_delay-end;
                if(sleeps>0) {
                    sleep(sleeps);
                }
            }
        }
        catch(InterruptedException e) {
        }
        finally {
            compute.shutdownNow();
            render.shutdownNow();
        }
    }

    private class DisplayAnimator implements Runnable {
        private Iterator<Plane> _frames;
        private Planescape _d;
        private int _parallel;


        public DisplayAnimator(Planescape d, ExecutorService pool, int parallel, float weight) {
            //_frames = ((Multirule2D)d.getRule()).frames(d.getCA());
            Plane p = d.getPlane();
            while(p==null) {
                try {
                    Thread.sleep(10);
                }
                catch(InterruptedException e) {
                }
                p = d.getPlane();
            }
            _parallel = parallel;
            _frames = d.compileRule().frameIterator(p, pool,
                new GOptions(true, _parallel, 0, weight)
                    .computeMode(ComputeMode.from(_config.<String>getVariable("rgb_computemode","combined")))
                );
            _d = d;
        }

        public void run() {
            step();
        }

        public void step() {
            //System.err.print(".");
            if(!_frames.hasNext()) {
                System.err.println("done");
                return;
            }
            final Plane frame = _frames.next();
            frame.lockRead();
            if(isInterrupted()) {
                if(!_d.delegateUnlock()) {
                    frame.unlockRead();
                }
                System.err.println("interrupted done");
                return;
            }
            final Thread calling = Thread.currentThread();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        //if(calling.isInterrupted() || _state!=State.animate) {
                            //return;
                        //}
                        //_d.setPlane(frame);
                        if(!calling.isInterrupted() && (_state==State.animate||_state==State.die)) {
                            _d.setPlane(frame);
                        }
                        else {
                            System.err.println("not rendering: int: "+calling.isInterrupted()+", st: "+_state);
                        }
                    }
                    finally {
                        if(!_d.delegateUnlock()) {
                            frame.unlockRead();
                        }
                    }
                }
            });
        }
    }
}
