package org.excelsi.nausicaa.ca;


public class Self extends AbstractMutator {
    //private static final int EMPTY = 0;
    //private static final int HEAD = 1;
    //private static final int TAIL = 2;
    //private static final int CONDUCTOR = 3;
    private final int SELF = 4;


    public Self() {
    }

    public String name() { return "Self"; }
    public String description() { return "Self"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        if(r.getPattern().archetype().dims()!=2) {
            throw new MutationFailedException("Self must be 2d");
        }
        if(r.getPattern().archetype().colors()<4) {
            throw new MutationFailedException("Self must have 4+ colors");
        }
        //final Archetype na = r.archetype().asColors(4);
        final Archetype a = r.archetype();
        final byte[] base = new byte[r.archetype().sourceLength()];
        final int[] histo = new int[r.archetype().colors()];
        return r.derive((arch, target)->{
            for(int i=0;i<target.length;i++) {
                Patterns.expandSourceIndex(arch, i, base);
                int self = base[SELF];
                int res = 0;
                if(self==0) {
                    // empty
                    res = 0;
                }
                else if(self==a.colors()-2) {
                    // tail
                    res = a.colors()-1; // conductor
                }
                else if(self==a.colors()-1) {
                    // conductor
                    for(int j=0;j<histo.length;j++) {
                        histo[j] = 0;
                    }
                    for(int j=0;j<base.length;j++) {
                        histo[base[j]]++;
                    }
                    if(histo[1]==1||histo[1]==2) {
                        res = 1; // head
                    }
                    else {
                        res = a.colors()-1; // conductor
                    }
                }
                else {
                    // head
                    res = self + 1; // to tail
                }
                target[i] = (byte) res;
            }
        });
    }
}
