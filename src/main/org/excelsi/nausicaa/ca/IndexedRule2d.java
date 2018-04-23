package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class IndexedRule2d extends AbstractIndexedRule implements IndexedRule {
    private final IndexedRuleset2d _origin;
    private final IndexedRule2d _meta;


    public IndexedRule2d(IndexedPattern p) {
        this(p, null);
    }

    public IndexedRule2d(IndexedPattern p, IndexedRuleset2d origin) {
        this(p, origin, null, null);
    }

    public IndexedRule2d(IndexedPattern p, IndexedRuleset2d origin, IndexedRule2d metarule, IndexedRule hyper) {
        super(p, hyper);
        _origin = origin!=null?origin:new IndexedRuleset2d(p.archetype());
        _meta = null;
    }

    @Override public IndexedRule getMetarule() {
        return _meta;
    }

    @Override public IndexedRule withMetarule(IndexedRule meta) {
        return new IndexedRule2d(pattern(), _origin, (IndexedRule2d) meta, getHyperrule());
    }

    @Override public IndexedRule withHyperrule(IndexedRule hyper) {
        return new IndexedRule2d(pattern(), _origin, _meta, hyper);
    }

    @Override public IndexedRule2d derive(IndexedPattern pattern) {
        return new IndexedRule2d(pattern, _origin, _meta, getHyperrule());
    }

    @Override public IndexedRule2d derive(IndexedPattern.Transform transform) {
        return new IndexedRule2d(pattern().transform(transform), _origin, _meta!=null?_meta.derive(transform):null, getHyperrule());
    }

    @Override public int dimensions() {
        return 2;
    }

    @Override public int background() {
        return 0;
    }

    @Override public int[][] toPattern() {
        throw new UnsupportedOperationException();
    }

    @Override public IndexedRuleset origin() {
        return _origin;
    }

    public Iterator<Plane> frameIterator(final Plane c, final ExecutorService pool, final GOptions opt) {
        if(c==null) {
            throw new IllegalArgumentException("null plane");
        }
        final Iterator<Plane> metarator = _meta!=null?_meta.frameIterator(c,pool, opt):null;
        final int block = 800;
        int nworkers = c.getHeight()/block + (c.getHeight()%block>0?1:0);
        final Worker[] workers = new Worker[nworkers];
        final Pattern p = createPattern(pool);
        for(int i=0;i<workers.length;i++) {
            workers[i] = new Worker(p, 0, i*block, c.getWidth(), Math.min(c.getHeight(), (i+1)*block), opt.weight(), ComputeMode.combined);
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
                catch(InterruptedException|ExecutionException e) {
                }
                tmp = p1;
                p1 = p2;
                if(opt.doubleBuffer()) {
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

    @Override public Plane generate(final Plane c, final int start, final int end, final ExecutorService pool, final boolean stopOnSame, final boolean overwrite, final Updater u, final GOptions opt) {
        Plane p1 = c;
        Plane p2 = c.copy();
        Plane tmp;
        final Pattern p = createPattern(pool);
        Worker w = new Worker(p, 0, 0, c.getWidth(), c.getHeight(), opt.weight(), ComputeMode.combined);
        for(int frames=start;frames<end;frames++) {
            //System.err.println("frame "+frames);
            w.frame(p1, p2);
            tmp = p1;
            p1 = p2;
            p2 = tmp;
            p.tick();
        }
        return c;
    }

    @Override public String humanize() {
        StringBuilder b = new StringBuilder("2d / ");
        b.append(super.humanize());
        return b.toString();
    }

    @Override public String toString() {
        return "IndexedRule2d::{pattern:"+pattern()+"}";
    }
}
