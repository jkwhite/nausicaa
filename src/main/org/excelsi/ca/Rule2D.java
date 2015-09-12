package org.excelsi.ca;


import java.util.ArrayList;
import java.util.List;
import javassist.*;
import java.util.Arrays;
import java.sql.Array;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Random;
import java.util.HashSet;
import java.util.concurrent.*;


public class Rule2D extends Rule1D {
    private CA _b;
    private int[][] _p2;


    public Rule2D(Ruleset origin, int[] colors, int[][] patterns, int background) {
        super(origin, colors, patterns, background);
        _p2 = patterns;
        //testPattern(patterns);
    }

    public static void testPattern(int[][] patterns) {
        HashSet test = new HashSet();
        for(int[] pattern:patterns) {
            StringBuilder key = new StringBuilder();
            for(int p:pattern) {
                key.append(p).append(",");
            }
            String k = key.toString();
            if(test.contains(k)) {
                throw new Error("duplicate pattern: "+k);
            }
            test.add(k);
        }
    }

    public int dimensions() { return 2; }

    public int[][] toPattern() {
        return (int[][]) Rule1D.deepCopy(_p2);
    }

    public Rule2D copy() {
        return new Rule2D(origin(), colors(), toPattern(), background());
    }

    public int length() {
        return (int) Math.sqrt(super.length());
    }

    public void init(CA c, Initialization in) {
        Random r = new Random(Rand.seed());
        switch(in) {
            case single:
                for(int i=0;i<c.getWidth();i++) {
                    c.set(i, 0, _colors[0]);
                }
                break;
            case random:
                for(int i=0;i<c.getWidth();i++) {
                    for(int j=0;j<c.getHeight();j++) {
                        c.set(i, j, _colors[r.nextInt(_colors.length)]);
                    }
                }
                break;
            default:
        }
    }

    public static abstract class FrameIterator<E> implements Iterator<E> {
        private E _last;


        public FrameIterator(E root) {
            if(root==null) {
                throw new IllegalArgumentException();
            }
            _last = root;
        }

        public boolean hasNext() {
            return true;
        }

        public E next() {
            _last = frame(_last);
            return _last;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        abstract protected E frame(E last);
    }

    public Iterator<CA> frames(CA root) {
        return new FrameIterator<CA>(root) {
            protected CA frame(CA c) {
                //generate(c, 0, 1, false, null);
                return c;
            }
        };
    }

    public int getSuggestedInterval(CA c) {
        return 5;
    }


    //private int[] sur;
    private static final int WORKER_SIZE = 4;
    private transient RuleWorker[] _rw;
    private transient Future[] _frw;
    private static final ExecutorService _pool = Executors.newFixedThreadPool(3);
    public float generate(CA b1, CA b2, Updater u) {
        if(_frw==null) {
            _rw = new RuleWorker[WORKER_SIZE];
            _frw = new Future[_rw.length];
        }
        int workers = Math.max(1, Math.min(_rw.length, b1.getHeight()/200));
        //workers = 1;
        int x1 = 0, y1 = 0, step = b1.getHeight()/workers;
        for(int i=0;i<workers;i++) {
            int bot = y1+step;
            if(i==workers-1) {
                bot = b1.getHeight();
            }
            _rw[i] = new RuleWorker(b1, b2, x1, y1, b1.getWidth(), bot);
            _frw[i] = _pool.submit(_rw[i]);
            y1 = y1+step;
        }
        try {
            for(int i=0;i<workers;i++) {
                _frw[i].get();
            }
        }
        catch(ExecutionException e) {
            throw new Error(e);
        }
        catch(InterruptedException e) {
        }
        return 0f;
    }

    public float generate(CA c, int start, int end, boolean stopOnSame, boolean over, Updater u) {
        if(_b==null||_b.getWidth()!=c.getWidth()||_b.getHeight()!=c.getHeight()) {
            _b = new CA(c.getWidth(), c.getHeight());
        }
        int times = end-start;
        CA b1 = c, b2 = _b; // double buffer
        //int[] sur = new int[_len*_len];
        int[] sur = new int[_len-1];
        int dim = _len;
        int offset = (dim-1)/2;
        Selector sel = selector();
        for(int t=0;t<times;t++) {
            for(int x=0;x<b1.getWidth();x++) {
                for(int y=0;y<b1.getHeight();y++) {
                    int idx = 0;
                    int dx = x - offset;
                    int dy = y - offset;
                    if(false&&dx>=0&&dy>=0&&dx+dim<b1.getWidth()&&dy+dim<b1.getHeight()) {
                        b1.getBlock(sur, dx, dy, _len);
                    }
                    else {
                        for(int i=x-1;i<=x+1;i++) {
                            for(int j=y-1;j<=y+1;j++) {
                                sur[idx++] = get(b1, i, j);
                            }
                        }
                    }
                    int n = sel.next(sur, 0);
                    if(n!=-1) {
                        b2.set(x, y, _colors[n]);
                        if(_interceptor!=null) {
                            _interceptor.set(x, y, _colors[n]);
                        }
                        //System.err.print(" "+n);
                    }
                    /*
                    int dx = x - offset;
                    int dy = y - offset;
                    if(dx>=0&&dy>=0&&dx+dim<b1.getWidth()&&dy+dim<b1.getHeight()) {
                        b1.getBlock(sur, dx, dy, _len);
                    }
                    int n = sel.next(sur, 0);
                    if(n!=-1) {
                        b2.set(x, y, n);
                    }
                    */
                }
            }

            // swap
            CA temp = b1;
            b1 = b2;
            b2 = temp;
        }
        c.setData(b1.getData());
        //System.err.print(".");
        return 0f;
    }

    private class RuleWorker implements Runnable {
        private int _x1, _y1, _x2, _y2;
        private int[] sur;
        private int[][] rows = new int[3][];
        private CA b1, b2;


        public RuleWorker(CA b1, CA b2, int x1, int y1, int x2, int y2) {
            _x1 = x1;
            _y1 = y1;
            _x2 = x2;
            _y2 = y2;
            this.b1 = b1;
            this.b2 = b2;
        }

        public void run() {
            if(sur==null) {
                sur = new int[_len-1];
            }
            int dim = _len;
            int offset = (dim-1)/2;
            offset = 1;
            TroveSelector sel = (TroveSelector) selector();
            rows[0] = new int[b1.getWidth()];
            rows[1] = new int[b1.getWidth()];
            rows[2] = new int[b1.getWidth()];
            int ys = _y1-1;
            if(ys==-1) {
                ys = b1.getHeight()-1;
            }
            b1.getRow(rows[1], ys, 0);
            b1.getRow(rows[2], _y1, 0);
            for(int y=_y1;y<_y2;y++) {
                int[] tmp = rows[0];
                rows[0] = rows[1];
                rows[1] = rows[2];
                rows[2] = tmp;
                if(y+1!=b1.getHeight()) {
                    b1.getRow(rows[2], y+1, 0);
                }
                for(int x=_x1;x<_x2;x++) {
                    long lastKey = -1;
                    int lastN = -1;
                    int idx = 0;
                    int dx = x - offset;
                    int dy = y - offset;
                    long key;
                    if(dx>=0&&dy>=0&&x+1<b1.getWidth()&&y+1<b1.getHeight()) {
                        key = sel.inputKey(rows, x-1);
                    }
                    else {
                        for(int j=y-1;j<=y+1;j++) {
                            for(int i=x-1;i<=x+1;i++) {
                                sur[idx++] = get(b1, i, j);
                            }
                        }
                        key = sel.inputKey(sur,0);
                    }
                    int n;
                    if(key==lastKey) {
                        n = lastN;
                    }
                    else {
                        n = sel.next(key);
                    }
                    lastKey = key;
                    lastN = n;
                    int curColor = b2.get(x,y);
                    if(n!=-1) {
                        if(_colors[n]!=background()) {
                            //System.err.println("NONBGR FOR: "+Arrays.toString(sur));
                            //System.err.println("BGR: "+background());
                            //((TroveSelector)sel).longFind(sur);
                        }
                        b2.set(x, y, _colors[n]);
                        if(_interceptor!=null) {
                            _interceptor.set(x, y, _colors[n]);
                        }
                        //b2.set(x, y, n);
                        //System.err.print(" "+n);
                    }
                    else {
                        if(curColor==0) {
                            b2.set(x, y, background());
                        }
                        //System.err.println("no value for "+Arrays.toString(sur)+" for "+sel.toString());
                        //((TroveSelector)sel).longFind(sur);
                        //throw new Error();
                    }
                    /*
                    int dx = x - offset;
                    int dy = y - offset;
                    if(dx>=0&&dy>=0&&dx+dim<b1.getWidth()&&dy+dim<b1.getHeight()) {
                        b1.getBlock(sur, dx, dy, _len);
                    }
                    int n = sel.next(sur, 0);
                    if(n!=-1) {
                        b2.set(x, y, n);
                    }
                    */
                }
            }
        }
    }
}
