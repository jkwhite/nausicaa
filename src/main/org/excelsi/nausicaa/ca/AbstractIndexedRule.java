package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;


public abstract class AbstractIndexedRule extends AbstractRule implements IndexedRule {
    private final IndexedPattern _p;
    private final IndexedRule _hyper;


    public AbstractIndexedRule(IndexedPattern p) {
        this(p, null);
    }

    public AbstractIndexedRule(IndexedPattern p, IndexedRule hyper) {
        //super(p.archetype().patternLength(), 1);
        super(p.archetype().totalPatterns(), 1);
        _p = p;
        _hyper = hyper;
    }

    @Override public Archetype archetype() {
        return _p.archetype();
    }

    @Override public IndexedPattern getPattern() {
        return _p;
    }

    @Override public IndexedRule getHyperrule() {
        return _hyper;
    }

    @Override public int length() {
        return _p.archetype().sourceLength();
    }

    @Override public int colorCount() {
        return _p.archetype().colors();
    }

    @Override public int[] colors() {
        int[] cols = new int[_p.archetype().colors()];
        for(int i=0;i<cols.length;i++) {
            cols[i] = i;
        }
        return cols;
    }

    @Override public void copy(final Plane p) {
        _p.inspect((a,t)->{
            for(int i=0;i<t.length;i++) {
                p.setCell(i, 0, t[i]);
            }
            //p.setRow(t, 0);
        });
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        _p.write(dos);
    }

    @Override public String humanize() {
        //return _p.summarize();
        StringBuilder b = new StringBuilder(Info.b10Id(this, 50));
        if(getHyperrule()!=null) {
            b.append(" : ").append(getHyperrule().humanize());
        }
        return b.toString();
    }

    @Override public String id() {
        return _p.formatTarget();
    }

    @Override public String toString() {
        return "IndexedRule1d::{pattern:"+_p+"}";
    }

    protected final void mutateRule(final IndexedPattern p) {
        if(mutagen()!=null) {
            p.mutate(mutagen());
        }
    }

    protected final IndexedPattern pattern() {
        return _p;
    }

    protected final Pattern createPattern(final ExecutorService pool) {
        if(getHyperrule()==null) {
            return pattern().copy();
        }
        else {
            RulePattern rp = new RulePattern(pool, this, getHyperrule());
            return rp;
        }
    }
}
