package org.excelsi.nausicaa.ca;


public class Noise extends AbstractMutator {
    public String name() { return "Noise"; }
    public String description() { return "Introduces random fluctuations in patterns"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        return r.derive((a, target)->{
            for(int i=0;i<target.length;i++) {
                if(chance(f)) {
                    target[i] = (byte) (_om.nextInt(a.colors()));
                }
            }
        });
    }

    @Override public Rule mutate(Rule r) {
        return new ComputedRule2d(new ComputedPattern(r.archetype(), ComputedPattern.random(r.archetype(), new Datamap(), _om)));
    }

    @Override public boolean supports(Rule r) {
        return true;
    }
}
