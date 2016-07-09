package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class IndexedRule2d extends AbstractIndexedRule implements IndexedRule {
    //private final IndexedPattern _p;
    private final IndexedRuleset2d _origin;
    private final IndexedRule2d _meta;


    public IndexedRule2d(IndexedPattern p) {
        this(p, null);
    }

    public IndexedRule2d(IndexedPattern p, IndexedRuleset2d origin) {
        this(p, origin, null, null);
    }

    public IndexedRule2d(IndexedPattern p, IndexedRuleset2d origin, IndexedRule2d metarule, IndexedRule hyper) {
        //super(p.archetype().patternLength(), 1);
        super(p, hyper);
        //_p = p;
        _origin = origin!=null?origin:new IndexedRuleset2d(p.archetype());
        //_meta = metarule;
        //if(_meta!=null) {
            //System.err.println("META: "+_meta.humanize());
        //}
        //else {
            //System.err.println("META: null");
        //}
        _meta = null;
    }

    //@Override public Archetype archetype() {
        //return _p.archetype();
    //}

    @Override public IndexedRule getMetarule() {
        return _meta;
    }

    //@Override public IndexedRule getHyperrule() {
        //throw new UnsupportedOperationException();
    //}

    @Override public IndexedRule withMetarule(IndexedRule meta) {
        return new IndexedRule2d(pattern(), _origin, (IndexedRule2d) meta, getHyperrule());
    }

    @Override public IndexedRule withHyperrule(IndexedRule hyper) {
        return new IndexedRule2d(pattern(), _origin, _meta, hyper);
        //throw new UnsupportedOperationException();
    }

    @Override public IndexedRule2d derive(IndexedPattern pattern) {
        return new IndexedRule2d(pattern, _origin, _meta, getHyperrule());
    }

    @Override public IndexedRule2d derive(IndexedPattern.Transform transform) {
        return new IndexedRule2d(pattern().transform(transform), _origin, _meta!=null?_meta.derive(transform):null, getHyperrule());
    }

    //@Override public IndexedPattern getPattern() {
        //return _p;
    //}

    @Override public int dimensions() {
        return 2;
    }

    //@Override public int length() {
        //return _p.archetype().sourceLength();
    //}

    @Override public int background() {
        return 0;
    }

    //@Override public int colorCount() {
        //return _p.archetype().colors();
    //}

    @Override public int[][] toPattern() {
        throw new UnsupportedOperationException();
    }

    @Override public IndexedRuleset origin() {
        return _origin;
    }

    //@Override public int[] colors() {
        //int[] cols = new int[_p.archetype().colors()];
        //for(int i=0;i<cols.length;i++) {
            //cols[i] = i;
        //}
        //return cols;
    //}

    public class Worker {
        private final int _x1;
        private final int _y1;
        private final int _x2;
        private final int _y2;
        private final Pattern _wp;
        private final int _size;
        private final int[] _prev;
        private final byte[] _pattern;
        private final int[] _pow;

        public Worker(Pattern p, int x1, int y1, int x2, int y2) {
            _x1 = x1;
            _y1 = y1;
            _x2 = x2;
            _y2 = y2;
            _wp = p;
            _size = _wp.archetype().size();
            final int colors = _wp.archetype().colors();
            _prev = new int[(int)Math.pow(2*_size+1, _wp.archetype().dims())];
            _pattern = new byte[_prev.length];

            //_pow = new int[_wp.length()];
            _pow = new int[_wp.archetype().sourceLength()];
            for(int i=0;i<_pow.length;i++) {
                _pow[_pow.length-1-i] = (int) Math.pow(colors, i);
            }
            //System.err.println(String.format("worker dims: %dx%d+%dx%d", _x1, _y1, _x2, _y2));
            //System.err.println(String.format("prev array size: %d, pow array size: %d", _prev.length, _pow.length));
        }

        private void validate(Plane p) {
            for(int i=0;i<p.getWidth();i++) {
                for(int j=0;j<p.getHeight();j++) {
                    p.getCell(i, j);
                }
            }
        }

        public void frame(final Plane p1, final Plane p2) {
            validate(p1);
            int counts = 0;
            //System.err.println(String.format("dims: %d,%d,%d,%d", _y1, _y2, _x1, _x2));
            int mw = p1.getWidth()/2;
            int mh = p1.getHeight()/2;
            int tw = p1.getWidth();
            int th = p1.getHeight();
            for(int i=_y1;i<_y2;i++) {
                for(int j=_x1;j<_x2;j++) {
                    counts++;
                    //System.err.println(String.format("working: %d,%d", j, i));
                    p1.getBlock(_prev, j-_size, i-1, /*dx*/ 3, /*dy*/ 3, 0);
                    int idx = 0;
                    for(int k=0;k<_prev.length;k++) {
                        _pattern[k] = (byte) (_prev[k]);
                        idx += _prev[k] * _pow[k];
                        //System.err.println(String.format("prev[%d]=%d, pow[%d]=%d", k, _prev[k], k, _pow[k]));
                    }
                    //System.err.println(idx+" ");
                    //p2.setCell(j, i, _wp.next(idx));
                    //p2.setCell(j, i, _wp.next(idx, distance(tw, th, mw, mh, i, j)));
                    p2.setCell(j, i, _wp.next(idx));
                }
            }
            mutateRule();
            //System.err.println("set "+counts+" cells");
        }

        public void frame(final Plane p1, final Plane p2, final Plane meta) {
            int counts = 0;
            //System.err.println(String.format("dims: %d,%d,%d,%d", _y1, _y2, _x1, _x2));
            int mw = p1.getWidth()/2;
            int mh = p1.getHeight()/2;
            int tw = p1.getWidth();
            int th = p1.getHeight();
            int toff = 0;
            for(int i=_y1;i<_y2;i++) {
                for(int j=_x1;j<_x2;j++) {
                    counts++;
                    //System.err.println(String.format("working: %d,%d", j, i));
                    p1.getBlock(_prev, j-_size, i-1, /*dx*/ 3, /*dy*/ 3, 0);
                    int idx = 0;
                    for(int k=0;k<_prev.length;k++) {
                        _pattern[k] = (byte) (_prev[k]);
                        idx += _prev[k] * _pow[k];
                        //System.err.println(String.format("prev[%d]=%d, pow[%d]=%d", k, _prev[k], k, _pow[k]));
                    }
                    //System.err.println(idx+" ");
                    //p2.setCell(j, i, _wp.next(idx));
                    //TODO HERE
                    //p2.setCell(j, i, _wp.next(idx, distance(tw, th, mw, mh, i, j)));
                    //int off = meta.getCell(j, i);
                    //toff += off;
                    //p2.setCell(j, i, _wp.next(idx, off));
                }
            }
            mutateRule();
            //System.err.println("set "+counts+" cells");
            //System.err.println("total offsets: "+toff);
        }

        private final int distance(int tw, int th, int mw, int mh, int i, int j) {
            return (int) (Math.sqrt((mw-i)*(mw-i) + (mh-j)*(mh-j))/10);
        }

        private void mutateRule() {
            //if(mutagen()!=null) {
                //_wp.mutate(mutagen());
            //}
        }
    }

    public Iterator<Plane> frameIterator(final Plane c, final ExecutorService pool, final boolean doubleBuffer) {
        if(c==null) {
            throw new IllegalArgumentException("null plane");
        }
        final Iterator<Plane> metarator = _meta!=null?_meta.frameIterator(c,pool, doubleBuffer):null;
        final int block = 500;
        int nworkers = c.getHeight()/block + (c.getHeight()%block>0?1:0);
        final Worker[] workers = new Worker[nworkers];
        final Pattern p = createPattern(pool);
        for(int i=0;i<workers.length;i++) {
            //workers[i] = new Worker(_p, 0, i*block, c.getWidth(), Math.min(c.getHeight(), (i+1)*block));
            workers[i] = new Worker(p, 0, i*block, c.getWidth(), Math.min(c.getHeight(), (i+1)*block));
        }
        final Future[] futures = new Future[workers.length];
        System.err.println("frame workers: "+workers.length);
        return new Iterator<Plane>() {
            Plane p1 = c;
            Plane p2 = c.copy();
            Plane tmp;
            //final Worker w = new Worker(_p.copy(), 0, 0, c.getWidth(), c.getHeight());
            //final Worker w = new Worker(_p, 0, 0, c.getWidth(), c.getHeight());

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
                //w.frame(p1, p2);
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
            System.err.println("frame "+frames);
            w.frame(p1, p2);
            tmp = p1;
            p1 = p2;
            p2 = tmp;
            p.tick();
        }
        return 0f;
    }

    //@Override public void write(DataOutputStream dos) throws IOException {
        //_p.write(dos);
    //}

    //@Override public String humanize() {
        //return _p.summarize();
    //}

    //@Override public String id() {
        //return _p.formatTarget();
    //}

    @Override public String humanize() {
        StringBuilder b = new StringBuilder("2d / ");
        b.append(super.humanize());
        return b.toString();
    }

    @Override public String toString() {
        return "IndexedRule2d::{pattern:"+pattern()+"}";
    }
}
