package org.excelsi.nausicaa;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.swing.SwingUtilities;
import org.excelsi.nausicaa.ca.Plane;


public class Animation extends Thread implements TimelineListener, ConfigListener {
    enum State { animate, pause, reinit, die };
    private final Config _config;
    private final PlaneDisplayProvider _f;
    private final Timeline _timeline;
    private int _steps;
    private State _state = State.animate;
    private long _delay;


    public Animation(Config config, PlaneDisplayProvider f, Timeline timeline, int steps) {
        _config = config;
        _f = f;
        _timeline = timeline;
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
                break;
            case "futures":
                _state = State.reinit;
                break;
            case "tock":
                _state = State.reinit;
                break;
        }
    }

    @Override public void configChanged(Config c, String property) {
        if("animationDelay".equals(property)) {
            _delay = c.getAnimationDelay();
            _state = State.reinit;
        }
    }

    public void stopAnimation() {
        System.err.println("stopping animator");
        _state = State.die;
        interrupt();
        while(isAlive()) {
            try {
                Thread.sleep(25);
            }
            catch(InterruptedException e) {
                break;
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
        PlaneDisplay[] ds = _f.getDisplays();
        DisplayAnimator[] da = new DisplayAnimator[ds.length];
        ExecutorService compute = Executors.newFixedThreadPool(Math.min(4,ds.length));
        ExecutorService render = Executors.newFixedThreadPool(Math.min(4,ds.length));
        for(int i=0;i<ds.length;i++) {
            da[i] = new DisplayAnimator(ds[i], compute);
        }
        try {
            while(_state==State.animate) {
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
        private PlaneDisplay _d;


        public DisplayAnimator(PlaneDisplay d, ExecutorService pool) {
            //_frames = ((Multirule2D)d.getRule()).frames(d.getCA());
            _frames = d.getRule().frameIterator(d.getPlane(), pool, true);
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
            final Plane frame = _frames.next(); //.copy();
            if(isInterrupted()) {
                System.err.println("interrupted done");
                return;
            }
            final Thread calling = Thread.currentThread();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(calling.isInterrupted() || _state!=State.animate) {
                        return;
                    }
                    _d.setPlane(frame);
                }
            });
        }
    }
}
