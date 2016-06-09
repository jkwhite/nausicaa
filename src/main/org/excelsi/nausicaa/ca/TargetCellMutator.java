package org.excelsi.nausicaa.ca;


public abstract class TargetCellMutator extends AbstractMutator {
    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        //IndexedPattern p = r.getPattern();
        return r.derive((a, target)->{
            for(int i=0;i<target.length;i++) {
                final byte orig = target[i];
                final byte nt = mutate(a, f, orig);
                if(nt!=orig) {
                    target[i] = nt;
                }
            }
        });
    }

    public Rule mutate(Rule r) {
        throw new UnsupportedOperationException();
    }

    public Multirule mutate(Multirule r) {
        throw new UnsupportedOperationException();
    }

    protected abstract byte mutate(Archetype a, MutationFactor f, byte t);
}
