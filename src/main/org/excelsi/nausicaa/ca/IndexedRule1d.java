package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;


public class IndexedRule1d extends AbstractRule implements IndexedRule {
    private final IndexedPattern _p;
    private final IndexedRuleset1d _origin;


    public IndexedRule1d(IndexedPattern p) {
        this(p, null);
    }

    public IndexedRule1d(IndexedPattern p, IndexedRuleset1d origin) {
        super(p.archetype().patternLength(), 1);
        _p = p;
        _origin = origin!=null?origin:new IndexedRuleset1d(p.archetype());
    }

    @Override public IndexedRule getMetarule() {
        throw new UnsupportedOperationException();
    }

    @Override public IndexedRule withMetarule(IndexedRule meta) {
        throw new UnsupportedOperationException();
    }

    @Override public IndexedRule1d derive(IndexedPattern pattern) {
        return new IndexedRule1d(pattern, _origin);
    }

    @Override public IndexedRule1d derive(IndexedPattern.Transform transform) {
        return new IndexedRule1d(_p.transform(transform), _origin);
    }

    @Override public IndexedPattern getPattern() {
        return _p;
    }

    @Override public int dimensions() {
        return 1;
    }

    @Override public int length() {
        return _p.archetype().sourceLength();
    }

    @Override public int colorCount() {
        return _p.archetype().colors();
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

    @Override public int[] colors() {
        int[] cols = new int[_p.archetype().colors()];
        for(int i=0;i<cols.length;i++) {
            cols[i] = i;
        }
        return cols;
    }

    @Override public float generate(Plane c, int start, int end, boolean stopOnSame, boolean overwrite, Updater u) {
        final int w = c.getWidth();
        final int h = c.getHeight();
        final int size = _p.archetype().size();
        final int colors = _p.archetype().colors();

        int[] prev = new int[2*size+1];
        byte[] pattern = new byte[prev.length];

        int[] pow = new int[_p.length()];
        for(int i=0;i<pow.length;i++) {
            pow[pow.length-1-i] = (int) Math.pow(colors, i);
        }

        for(int i=start;i<end;i++) {
            for(int j=0;j<w;j++) {
                c.getBlock(prev, j-size, i-1, prev.length, 1, 0);
                int idx = 0;
                for(int k=0;k<prev.length;k++) {
                    pattern[k] = (byte) (prev[k]);
                    idx += prev[k] * pow[k];
                }
                c.setCell(j, i, _p.next(idx));
                //System.err.print(".");
            }
        }
        return 0f;
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        _p.write(dos);
    }

    @Override public String humanize() {
        return _p.summarize();
    }

    @Override public String id() {
        return _p.formatTarget();
    }

    @Override public String toString() {
        return "IndexedRule1d::{pattern:"+_p+"}";
    }
}
