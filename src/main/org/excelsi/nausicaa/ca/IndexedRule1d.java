package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.Iterator;


public class IndexedRule1d extends AbstractIndexedRule {
    //private final IndexedPattern _p;
    private final IndexedRuleset1d _origin;
    private final IndexedRule1d _meta;


    public IndexedRule1d(IndexedPattern p) {
        this(p, null);
    }

    public IndexedRule1d(IndexedPattern p, IndexedRuleset1d origin) {
        this(p, origin, null);
    }

    public IndexedRule1d(IndexedPattern p, IndexedRuleset1d origin, IndexedRule1d metarule) {
        this(p, origin, metarule, null);
    }

    public IndexedRule1d(IndexedPattern p, IndexedRuleset1d origin, IndexedRule1d metarule, IndexedRule hyperrule) {
        super(p, hyperrule);
        //_p = p;
        _origin = origin!=null?origin:new IndexedRuleset1d(p.archetype());
        _meta = null;
    }

    @Override public IndexedRule getMetarule() {
        return _meta;
    }

    @Override public IndexedRule withMetarule(IndexedRule meta) {
        return new IndexedRule1d(pattern(), _origin, (IndexedRule1d) meta, getHyperrule());
    }

    @Override public IndexedRule withHyperrule(IndexedRule hyper) {
        return new IndexedRule1d(pattern(), _origin, _meta, hyper);
    }

    @Override public IndexedRule1d derive(IndexedPattern pattern) {
        return new IndexedRule1d(pattern, _origin, _meta, getHyperrule());
    }

    @Override public IndexedRule1d derive(IndexedPattern.Transform transform) {
        return new IndexedRule1d(pattern().transform(transform), _origin, _meta, getHyperrule());
    }

    @Override public int dimensions() {
        return 1;
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

    @Override public Plane generate(final Plane c, final int start, final int end, final ExecutorService pool, final boolean stopOnSame, final boolean overwrite, final Updater u, final GOptions opt) {
        final int w = c.getWidth();
        final int h = c.getHeight();
        final int size = pattern().archetype().size();
        final int colors = pattern().archetype().colors();

        int[] prev = new int[2*size+1];
        byte[] pattern = new byte[prev.length];

        int[] pow = new int[pattern().length()];
        for(int i=0;i<pow.length;i++) {
            pow[pow.length-1-i] = (int) Math.pow(colors, i);
        }

        final Pattern p = createPattern(pool);
        //System.err.println("created pattern: "+p);
        for(int i=start;i<end;i++) {
            for(int j=0;j<w;j++) {
                c.getBlock(prev, j-size, i-1, prev.length, 1, 0);
                int idx = 0;
                for(int k=0;k<prev.length;k++) {
                    pattern[k] = (byte) (prev[k]);
                    idx += prev[k] * pow[k];
                }
                c.setCell(j, i, p.next(idx, pattern));
                //System.err.print(".");
            }
            //mutateRule(p);
            p.tick();
        }
        return c;
    }

    @Override public Iterator<Plane> frameIterator(final Plane initial, final ExecutorService pool, final GOptions opt) {
        generate(initial, 1, initial.getHeight(), pool, false, false, null, opt);

        return new Iterator<Plane>() {
            Plane p1 = initial;
            final int[] cp = new int[p1.getWidth()];
            int crow = 0;


            @Override public boolean hasNext() {
                return true;
            }

            @Override public void remove() {
            }

            @Override public Plane next() {
                if(++crow==p1.getHeight()) {
                    //final Plane p2 = p1.copy();
                    //p2.setRow(p1.getRow(cp, p1.getHeight()-1, 0), 0);
                    p1.setRow(p1.getRow(cp, p1.getHeight()-1, 0), 0);
                    //p1 = p2;
                    crow = 1;
                    generate(p1, 1, p1.getHeight(), pool, false, false, null, opt);
                }
                return p1.subplane(0, crow, p1.getWidth(), crow+1);
            }
        };
    }

    @Override public String humanize() {
        StringBuilder b = new StringBuilder("1d / ");
        b.append(super.humanize());
        return b.toString();
    }
}
