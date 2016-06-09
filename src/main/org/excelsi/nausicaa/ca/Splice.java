package org.excelsi.nausicaa.ca;


import java.util.*;


public class Splice extends AbstractMutator {
    public String name() { return "Splice"; }
    public String description() { return "Splices in a new rule"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        if(1+a.colors()>Color.MAX_COLORS) {
            throw new MutationFailedException("at max "+Color.MAX_COLORS+" colors");
        }
        else {
            a.asColors(1+a.colors()).validate(f.validator());
            //IndexedRule nr = r.origin().derive(a.asColors(2), r);
            IndexedRule nr = (IndexedRule) r.origin().derive(a.asColors(2)).random(_om).next();
            return nr.origin().merge(r, nr);
        }
    }
}
