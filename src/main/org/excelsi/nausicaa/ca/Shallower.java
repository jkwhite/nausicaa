package org.excelsi.nausicaa.ca;


public class Shallower extends AbstractMutator {
    public String name() { return "Shallower"; }
    public String description() { return "Decreases hyperdepth"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        if(r.getHyperrule()!=null) {
            return r.getHyperrule();
        }
        else {
            throw new MutationFailedException("already at shallowest depth");
        }
    }
}
