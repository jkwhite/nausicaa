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

    @Override public Rule mutate(Rule r) {
        if(r.archetype().colors()<3) {
            throw new MutationFailedException("only 2 colors");
        }
        final Archetype a = r.archetype().asColors(r.archetype().colors()-1);
        return new ComputedRule2d(new ComputedPattern(a, ComputedPattern.random(a, new Datamap(), _om)));
    }

    @Override public boolean supports(Rule r) {
        return true;
    }
}
