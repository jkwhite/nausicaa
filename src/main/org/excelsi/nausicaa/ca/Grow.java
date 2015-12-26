package org.excelsi.nausicaa.ca;


import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;


public class Grow extends AbstractMutator {
    public String name() { return _full?"Overgrow":"Grow"; }
    public String description() { return "All colors bloom"; }


    private final boolean _full;

    public Grow() {
        this(false);
    }

    public Grow(boolean full) {
        _full = full;
    }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        final long max = a.totalPatterns();
        final byte[] base = new byte[a.sourceLength()];
        final int[] zero = new int[a.colors()];
        final int[] counts = new int[a.colors()];
        return r.derive(r.getPattern().transform((arch, target)->{
            for(int i=0;i<max;i++) {
                if(target[i]!=-1) {
                    System.arraycopy(zero, 0, counts, 0, counts.length);
                    Patterns.expandSourceIndex(arch, i, base);
                    byte last = -1;
                    boolean mixed = false;
                    for(int j=0;j<base.length;j++) {
                        final byte t = base[j];
                        if(t>-1) {
                            //if(last==-1) {
                                //last = t;
                            //}
                            //else if(last!=t) {
                                //mixed = true;
                                //break;
                            //}
                            counts[base[j]]++;
                        }
                    }
                    int midx = -1;
                    int mx = -1;
                    for(int j=0;j<counts.length;j++) {
                        if(mx==-1||counts[j]>mx) {
                            midx = j;
                            mx = counts[j];
                        }
                    }
                    if(target[i]!=(byte) midx && (_full||chance())) {
                        target[i] = (byte) midx;
                    }
                    //if(mixed) {
                        //int sidx = _rand.nextInt(counts.length);
                        //while(counts[sidx]==0) {
                            //sidx = (sidx+1) % counts.length;
                        //}
                        //target[i] = sidx;
                        //target[i] = 0;
                    //}
                }
            }
        }));
    }
}
