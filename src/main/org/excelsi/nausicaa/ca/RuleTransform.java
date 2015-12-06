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
            IndexedRule ir = (IndexedRule) root;
            System.err.println(_m.name()+" mutating rule "+root.humanize());
            IndexedRule mut = _m.mutateIndexedRule(ir);
            if(ir.getMetarule()!=null) {
                mut = mut.withMetarule(_m.mutateIndexedRule(ir.getMetarule()));
            }
            CA after = c.mutate(mut, _rand);
            System.err.println(_m.name()+" produced rule "+after.getRule().humanize());
            return after;
        }
        else {
            return c.mutate(_m.mutate(root), _rand);
        }
    }
}
