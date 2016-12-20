package org.excelsi.nausicaa.ca;


import java.util.*;


public class Collapse extends AbstractMutator {
    public String name() { return "Collapse"; }
    public String description() { return "Reduces color spectrum"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        if(a.colors()<3) {
            throw new MutationFailedException("only 2 colors");
        }
        else {
            return (IndexedRule) r.origin().derive(a.asColors(a.colors()-1)).random(_om).next();
        }
    }
}
