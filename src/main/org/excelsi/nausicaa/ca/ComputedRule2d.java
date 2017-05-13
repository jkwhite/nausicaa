package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Random;


public class ComputedRule2d extends AbstractRule implements Mutatable {
    private final Ruleset _origin;
    private final Pattern _p;
    private final Rule _meta;
    //private final ComputedRule2d _meta;


    public ComputedRule2d(Pattern p) {
        this(p, null);
    }

    public ComputedRule2d(Pattern p, Ruleset origin) {
        this(p, origin, null, null);
    }

    public ComputedRule2d(Pattern p, Ruleset origin, Rule metarule, Rule hyper) {
        //super(p, hyper);
        super(1,1);
        _p = p;
        _origin = origin!=null?origin:new ComputedRuleset(p.archetype());
        _meta = null;
    }

    //@Override public IndexedRule getMetarule() {
        //return _meta;
        //throw new UnsupportedOperationException();
    //}
//
    //@Override public IndexedRule withMetarule(IndexedRule meta) {
        //return new IndexedRule2d(pattern(), _origin, (IndexedRule2d) meta, getHyperrule());
        //throw new UnsupportedOperationException();
    //}
//
    //@Override public IndexedRule withHyperrule(IndexedRule hyper) {
        //return new IndexedRule2d(pattern(), _origin, _meta, hyper);
        //throw new UnsupportedOperationException();
    //}

    public ComputedRule2d derive(Pattern pattern) {
        //return new ComputedRule2d(pattern, _origin, _meta, getHyperrule());
        return new ComputedRule2d(pattern, _origin, _meta, null);
    }

    //@Override public ComputedRule2d derive(Pattern.Transform transform) {
        //return new ComputedRule2d(pattern().transform(transform), _origin, _meta!=null?_meta.derive(transform):null, getHyperrule());
    //}

    @Override public Archetype archetype() {
        return _p.archetype();
    }

    @Override public int dimensions() {
        return archetype().dims();
    }

    @Override public int background() {
        return 0;
    }

    @Override public int length() {
        return _p.archetype().sourceLength();
    }

    @Override public int[] colors() {
        int[] cols = new int[_p.archetype().colors()];
        for(int i=0;i<cols.length;i++) {
            cols[i] = i;
        }
        return cols;
    }

    @Override public int[][] toPattern() {
        throw new UnsupportedOperationException();
    }

    @Override public Ruleset origin() {
        return _origin;
    }

    public Iterator<Plane> frameIterator(final Plane c, final ExecutorService pool, final boolean doubleBuffer) {
        if(c==null) {
            throw new IllegalArgumentException("null plane");
        }
        final Iterator<Plane> metarator = _meta!=null?_meta.frameIterator(c,pool, doubleBuffer):null;
        final int block = 8000;
        int nworkers = c.getHeight()/block + (c.getHeight()%block>0?1:0);
        final Worker[] workers = new Worker[nworkers];
        final Pattern p = createPattern(pool);
        for(int i=0;i<workers.length;i++) {
            workers[i] = new Worker(p, 0, i*block, c.getWidth(), Math.min(c.getHeight(), (i+1)*block));
        }
        final Future[] futures = new Future[workers.length];
        return new Iterator<Plane>() {
            Plane p1 = c;
            Plane p2 = c.copy();
            Plane tmp;

            @Override public Plane next() {
                //if(metarator!=null) {
                    //Plane meta = metarator.next();
                    //w.frame(p1, p2, meta);
                //}
                //else {
                    //w.frame(p1, p2);
                //}
                final Plane frameP1 = p1;
                final Plane frameP2 = p2;
                for(int i=0;i<workers.length;i++) {
                    final int w = i;
                    futures[i] = pool.submit(()->workers[w].frame(frameP1, frameP2));
                }
                try {
                    for(int i=0;i<futures.length;i++) {
                        futures[i].get();
                    }
                }
                catch(InterruptedException e) {
                }
                catch(ExecutionException e) {
                    e.printStackTrace();
                }
                tmp = p1;
                p1 = p2;
                if(doubleBuffer) {
                    p2 = tmp;
                }
                else {
                    p2 = p1.copy();
                }
                p.tick();
                return p2;
            }

            @Override public boolean hasNext() {
                return true;
            }

            @Override public void remove() {
            }
        };
    }

    @Override public float generate(final Plane c, final int start, final int end, final ExecutorService pool, final boolean stopOnSame, final boolean overwrite, final Updater u) {
        Plane p1 = c;
        Plane p2 = c.copy();
        Plane tmp;
        final Pattern p = createPattern(pool);
        Worker w = new Worker(p, 0, 0, c.getWidth(), c.getHeight());
        for(int frames=start;frames<end;frames++) {
            w.frame(p1, p2);
            tmp = p1;
            p1 = p2;
            p2 = tmp;
            p.tick();
        }
        return 0f;
    }

    @Override public Mutatable mutate(Random r) {
        return derive(((ComputedPattern)_p).mutate(r));
    }

    @Override public String humanize() {
        StringBuilder b = new StringBuilder(archetype().dims()+"d / "+_p);
        //b.append(super.humanize());
        return b.toString();
    }

    public String genome() {
        return _p.toString();
    }

    @Override public String toString() {
        return "ComputedRule2d::{pattern:"+_p+"}";
    }

    protected final Pattern createPattern(final ExecutorService pool) {
        return ((ComputedPattern)_p).copy();
    }
}
