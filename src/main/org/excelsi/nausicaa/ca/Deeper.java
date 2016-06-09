package org.excelsi.nausicaa.ca;


public class Deeper extends AbstractMutator {
    public String name() { return "Deeper"; }
    public String description() { return "Increases hyperdepth"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        return ((IndexedRule)r.origin().derive(r.getPattern().archetype(), r.origin()).random(_om).next()).withHyperrule(r);
    }
}
