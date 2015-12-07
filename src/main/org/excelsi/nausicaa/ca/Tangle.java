package org.excelsi.nausicaa.ca;


public class Tangle extends AbstractMutator {
    public String name() { return "Tangle"; }
    public String description() { return "Entangles the color spaces of multiple rules"; }


    @Override public IndexedRule mutateIndexedRule(IndexedRule r) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        if(a.colors()<3) {
            throw new MutationFailedException("need at least 3 colors");
        }
        final long max = a.totalPatterns();
        final byte[] base = new byte[a.sourceLength()];
        final int[] zero = new int[a.colors()];
        final int[] counts = new int[a.colors()];
        return r.derive(r.getPattern().transform((arch, target)->{
            for(int i=0;i<max;i++) {
                if(target[i]==0) {
                    System.arraycopy(zero, 0, counts, 0, counts.length);
                    Patterns.expandSourceIndex(arch, i, base);
                    byte last = -1;
                    boolean mixed = false;
                    for(int j=0;j<base.length;j++) {
                        final byte t = base[j];
                        if(t>0) {
                            if(last==-1) {
                                last = t;
                            }
                            else if(last!=t) {
                                mixed = true;
                            }
                            counts[base[j]]++;
                        }
                    }
                    if(mixed && _om.nextInt(100)<ALPHA) {
                        int sidx = _om.nextInt(counts.length);
                        while(counts[sidx]==0) {
                            sidx = (sidx+1) % counts.length;
                        }
                        target[i] = (byte) sidx;
                    }
                }
            }
        }));
    }

    public Rule mutate(Rule r) {
        return null;
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        //System.err.println("mutating "+r);
        Rule[] rules = r.rules();
        if(rules.length<2) {
            //System.err.println("tangle failed: only one rule");
            //return r;
            throw new MutationFailedException("only one rule");
        }
        Rule[] nr = new Rule[rules.length];
        int si = _om.nextInt(rules.length);
        int di = si+1;
        if(di==rules.length) {
            di = 0;
        }
        for(int i=0;i<rules.length;i++) {
            if(i==di) {
                int[] sc = rules[si].colors();
                try {
                    Hue h = new Hue(false, sc[_om.nextInt(sc.length)]);
                    nr[i] = h.mutate(rules[i]);
                }
                catch(IllegalArgumentException e) {
                    throw new MutationFailedException("not enough colors");
                    //System.err.println("tangle failed: not enough colors");
                    //nr[i] = rules[i];
                }
            }
            else {
                nr[i] = rules[i];
            }
        }
        //return new Multirule1D(nr);
        return (Multirule) r.origin().create((Object[])nr);
    }
}
