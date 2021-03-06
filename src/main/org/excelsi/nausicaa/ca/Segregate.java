package org.excelsi.nausicaa.ca;


import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;


public class Segregate extends AbstractMutator {
    public String name() { return "Segregate"; }
    public String description() { return "Produces disjoint subrules from a multicolor rule"; }

    private boolean _all = false;


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
                if(target[i]!=0) {
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
                                break;
                            }
                            counts[base[j]]++;
                        }
                    }
                    if(mixed) {
                        //int sidx = _rand.nextInt(counts.length);
                        //while(counts[sidx]==0) {
                            //sidx = (sidx+1) % counts.length;
                        //}
                        //target[i] = sidx;
                        target[i] = 0;
                    }
                }
            }
        }));
    }
}
