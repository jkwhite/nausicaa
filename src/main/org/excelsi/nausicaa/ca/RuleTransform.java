package org.excelsi.nausicaa.ca;


import java.util.Random;


public class RuleTransform implements Transform {
    private final Mutator _m;
    private final Random _rand;


    public RuleTransform(Random rand, Mutator m) {
        _rand = rand;
        _m = m;
    }

    public String name() {
        return "Rule";
    }

    public CA transform(CA c) throws MutationFailedException {
        Rule root = c.getRule();
        //Rule m = root.origin().random(c.getRandom()).next();
        if(root instanceof IndexedRule) {
            System.err.println(_m.name()+" mutating rule "+root.humanize());
            CA after = c.mutate(_m.mutateIndexedRule((IndexedRule)root), _rand);
            System.err.println(_m.name()+" produced rule "+after.getRule().humanize());
            return after;
        }
        else {
            return c.mutate(_m.mutate(root), _rand);
        }
    }
}
