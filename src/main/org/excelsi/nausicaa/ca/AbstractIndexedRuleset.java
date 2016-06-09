package org.excelsi.nausicaa.ca;


import java.util.Random;


public abstract class AbstractIndexedRuleset implements IndexedRuleset {
    private final Archetype _a;
    private final IndexedRuleset _hyper;


    public AbstractIndexedRuleset(Archetype a) {
        this(a, null);
    }

    public AbstractIndexedRuleset(Archetype a, IndexedRuleset hyper) {
        _a = a;
        _hyper = hyper;
    }

    @Override public IndexedRuleset getHyperrules() {
        return _hyper;
    }

    @Override public IndexedRuleset derive(Archetype a, IndexedRuleset hyper) {
        switch(a.dims()) {
            case 1:
                return new IndexedRuleset1d(a, hyper);
            case 2:
                return new IndexedRuleset2d(a, hyper);
            default:
                throw new IllegalArgumentException("unsupported dimensionality "+a.dims());
        }
    }

    @Override public IndexedRuleset derive(Archetype a) {
        switch(a.dims()) {
            case 1:
                return new IndexedRuleset1d(a, _hyper);
            case 2:
                return new IndexedRuleset2d(a, _hyper);
            default:
                throw new IllegalArgumentException("unsupported dimensionality "+a.dims());
        }
    }

    @Override public IndexedRule derive(Archetype a, IndexedRule sourceRule) {
        IndexedRuleset rs = derive(a);
        if(a.colors()!=_a.colors()) {
            IndexedRule ir = rs.custom(sourceRule, (sa, source, ta, target)->{
                //System.err.println("mismatch: copying "+sa+" => "+ta);
                //System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
                final byte[] base = new byte[sa.sourceLength()];
                final int[] coefficients = ta.sourceCoefficients();
                for(int i=0;i<source.length;i++) {
                    Patterns.expandSourceIndex(sa, i, base);
                    int nidx = Patterns.indexForSource(coefficients, base);
                    if(nidx<target.length) {
                        if(source[i] < a.colors()) {
                            target[nidx] = source[i];
                        }
                        else {
                            System.err.println("wrapping large source "+source[i]+" at "+a.colors());
                            target[nidx] = (byte) (source[i] % a.colors());
                        }
                    }
                }
            });
            return ir;
        }
        else {
            return rs.custom(sourceRule, (sa, source, ta, target)->{
                //System.err.println("match: copying "+sa+" => "+ta);
                System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
            });
            //return (IndexedRule) rs.iterator().next();
        }
    }

    @Override public IndexedRule merge(IndexedRule rule1, IndexedRule rule2) {
        final IndexedRule meta = rule1.getMetarule()!=null?rule1.getMetarule():rule2.getMetarule();
        final IndexedRule hyper = rule1.getHyperrule()!=null?rule1.getHyperrule():rule2.getHyperrule();
        Archetype a1 = rule1.getPattern().archetype();
        Archetype a2 = rule2.getPattern().archetype();
        Archetype a = a1.asColors(a1.colors()+a2.colors()-1);
        return derive(a).custom((arch, target)->{
            rule1.getPattern().inspect((arch1, source1)->{
                rule2.getPattern().inspect((arch2, source2)->{
                    final int[] coefficients = arch.sourceCoefficients();
                    final byte[] base1 = new byte[arch1.sourceLength()];
                    for(int i=0;i<source1.length;i++) {
                        Patterns.expandSourceIndex(arch1, i, base1);
                        int nidx = Patterns.indexForSource(coefficients, base1);
                        if(nidx<target.length) {
                            if(source1[i]>=a.colors()) throw new MutationFailedException(source1[i]+" >= "+a.colors());
                            target[nidx] = source1[i];
                        }
                    }
                    final byte[] base2 = new byte[arch2.sourceLength()];
                    for(int i=0;i<source2.length;i++) {
                        Patterns.expandSourceIndex(arch2, i, base2);
                        for(int j=0;j<base2.length;j++) {
                            if(base2[j]!=0) {
                                base2[j] = (byte) (base2[j] + arch1.colors()-1);
                            }
                        }
                        int nidx = Patterns.indexForSource(coefficients, base2);
                        if(nidx<target.length) {
                            byte src = source2[i];
                            if(src!=0) {
                                src += (byte) (arch1.colors()-1);
                            }
                            if(src>=a.colors()) throw new MutationFailedException(src+" >= "+a.colors());
                            target[nidx] = src;
                            //if(i%20==0) {
                                //System.err.println(i+" => "+nidx+": "+target[nidx]+": "+Patterns.formatPattern(base2));
                            //}
                        }
                        else {
                            System.err.println("out of range: "+nidx+" > "+target.length);
                        }
                    }
                });
            });
        }).withMetarule(meta).withHyperrule(hyper);
    }

    protected final Archetype archetype() {
        return _a;
    }

    protected final IndexedRule hyperRandom(final Random r) {
        return _hyper!=null?(IndexedRule)_hyper.random(r).next():null;
    }
}
