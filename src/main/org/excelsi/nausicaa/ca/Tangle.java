package org.excelsi.nausicaa.ca;


public class Tangle extends AbstractMutator {
    public String name() { return "Tangle"; }
    public String description() { return "Entangles the color spaces of multiple rules"; }


    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
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
                    if(mixed && chance(f)) {
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
}
