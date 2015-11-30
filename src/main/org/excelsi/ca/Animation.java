package org.excelsi.ca;


import java.util.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;


public class Animation extends Thread implements ViewerListener {
    private Futures _f;
    private Branch<World> _w;
    private Viewer.CAGraph _g;
    private Viewer _v;
    private int _steps;


    public Animation(Futures f, Viewer v, int steps) {
        _f = f;
        v.addViewerListener(this);
        _v = v;
        _steps = steps;
    }

    public Animation(Branch<World> w, Viewer.CAGraph g, int steps) {
        _w = w;
        _g = g;
        _steps = steps;
    }

    public void futureChanged(Futures f) {
        if(f==_f) {
            _v.removeViewerListener(this);
            _v.animate(_f, _steps);
        }
    }

    public void futureChanging(Futures f) {
        if(f==_f) {
            interrupt();
            while(isAlive()) {
                try {
                    Thread.sleep(25);
                }
                catch(InterruptedException e) {
                    break;
                }
            }
        }
    }

    public void run() {
        if(_f!=null) {
            runFutures();
            return;
        }
        Rule r;
        CA root;

        if(_f!=null) {
            r = _f.getMainDisplay().getRule();
            root = _f.getMainDisplay().getCA();
        }
        else {
            r = _w.data().getRule();
            root = _w.data().getCA();
        }

        try {
            for(Iterator<CA> f=((Multirule2D)r).frames(root);f.hasNext();) {
                long start = System.currentTimeMillis();
                final CA frame = f.next();
                long end = System.currentTimeMillis();
                if(isInterrupted()) {
                    break;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(_f!=null) {
                            _f.getMainDisplay().setCA(frame);
                        }
                        else {
                            _w.data().setCA(frame);
                            _g.graphDidChange();
                        }
                    }
                });
                if(_steps>0&&--_steps==0) {
                    break;
                }
                long sleeps = start+50-end;
                if(sleeps>0) {
                    //System.err.println("sleep for "+sleeps);
                    sleep(sleeps);
                }
            }
        }
        catch(InterruptedException e) {
        }
    }

    public void runFutures() {
        Display[] ds = _f.getDisplays();
        DisplayAnimator[] da = new DisplayAnimator[ds.length];
        for(int i=0;i<ds.length;i++) {
            da[i] = new DisplayAnimator(ds[i]);
        }
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(4,ds.length));
        try {
            while(!isInterrupted()) {
                long start = System.currentTimeMillis();
                for(int i=0;i<da.length;i++) {
                    if(isInterrupted()) {
                        break;
                    }
                    pool.submit(da[i]);
                }
                long end = System.currentTimeMillis();
                if(_steps>0&&--_steps==0) {
                    break;
                }
                long sleeps = start+50-end;
                if(sleeps>0) {
                    sleep(sleeps);
                }
            }
        }
        catch(InterruptedException e) {
        }
        finally {
            pool.shutdownNow();
        }
    }

    private class DisplayAnimator implements Runnable {
        private Iterator<CA> _frames;
        private Display _d;


        public DisplayAnimator(Display d) {
            _frames = ((Multirule2D)d.getRule()).frames(d.getCA());
            _d = d;
        }

        public void run() {
            step();
        }

        public void step() {
            if(!_frames.hasNext()) {
                return;
            }
            final CA frame = _frames.next();
            if(isInterrupted()) {
                return;
            }
            final Thread calling = Thread.currentThread();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(calling.isInterrupted()) {
                        return;
                    }
                    _d.setCA(frame);
                }
            });
        }
    }
}
