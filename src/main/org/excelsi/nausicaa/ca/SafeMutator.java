package org.excelsi.nausicaa.ca;


public class SafeMutator implements Mutator {
    private final Mutator _m;


    public SafeMutator(Mutator m) {
        _m = m;
    }

    public String name() {
        return _m.name();
    }

    public String description() {
        return _m.description();
    }

    @Override public boolean supports(Rule r) {
        return _m.supports(r);
    }

    @Override public void setRandom(java.util.Random r) {
        _m.setRandom(r);
    }

    @Override public Rule mutate(Rule r) throws MutationFailedException {
        return _m.mutate(r);
    }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        try {
            return _m.mutateIndexedRule(r, f);
        }
        catch(MutationFailedException|IllegalStateException e) {
            System.err.println(e.toString());
            return r;
        }
    }
}
