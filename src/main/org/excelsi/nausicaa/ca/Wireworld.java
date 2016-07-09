package org.excelsi.nausicaa.ca;


public class Wireworld extends AbstractMutator {
    private static final int EMPTY = 0;
    private static final int HEAD = 1;
    private static final int TAIL = 2;
    private static final int CONDUCTOR = 3;
    private final int SELF = 4;


    public Wireworld() {
    }

    public String name() { return "Wireworld"; }
    public String description() { return "Wireworld"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        if(r.getPattern().archetype().dims()!=2) {
            throw new MutationFailedException("wireworld must be 2d");
        }
        final Archetype na = r.archetype().asColors(4);
        final byte[] base = new byte[na.sourceLength()];
        final int[] histo = new int[na.colors()];
        //final int max = (r.getPattern().archetype().colors()-1)*8;
        //final int ub = (int) Math.sqrt(1+max);
        //final int lb = (int) Math.cbrt(max);
        //final int ub = _ub;
        //final int lb = _lb;
        return r.origin().derive(na).custom((arch, target)->{
            for(int i=0;i<target.length;i++) {
                Patterns.expandSourceIndex(arch, i, base);
                switch(base[SELF]) {
                    case HEAD:
                        target[i] = TAIL;
                        break;
                    case TAIL:
                        target[i] = CONDUCTOR;
                        break;
                    case CONDUCTOR:
                        for(int j=0;j<histo.length;j++) {
                            histo[j] = 0;
                        }
                        for(int j=0;j<base.length;j++) {
                            histo[base[j]]++;
                        }
                        if(histo[HEAD]==1||histo[HEAD]==2) {
                            target[i] = HEAD;
                        }
                        else {
                            target[i] = CONDUCTOR;
                        }
                        break;
                    case EMPTY:
                    default:
                        target[i] = EMPTY;
                        break;
                }
            }
        });
    }
}
