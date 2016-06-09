package org.excelsi.nausicaa.ca;


import java.util.Random;


public class RuleTransform implements Transform {
    private final Mutator _m;
    private final Random _rand;
    private final MutationFactor _f;


    public RuleTransform(Random rand, Mutator m, MutationFactor f) {
        _rand = rand;
        _m = m;
        _f = f;
    }

    public String name() {
        return "Rule";
    }

    public CA transform(final CA c) throws MutationFailedException {
        Rule root = c.getRule();
        //Rule m = root.origin().random(c.getRandom()).next();
        if(root instanceof IndexedRule) {
            IndexedRule ir = (IndexedRule) root;
            //System.err.println(_m.name()+" mutating rule "+root.humanize());
            //log("before", ir);
            IndexedRule mut = _m.mutateIndexedRule(ir, _f);
            //log("after", mut);
            if(ir.getMetarule()!=null) {
                mut = mut.withMetarule(_m.mutateIndexedRule(ir.getMetarule(), _f));
            }
            if(ir.getHyperrule()!=null) {
                try {
                    mut = mut.withHyperrule(_m.mutateIndexedRule(ir.getHyperrule(), _f));
                }
                catch(MutationFailedException e) {
                    mut = mut.withHyperrule(null);
                }
            }
            CA after = c.mutate(mut, _rand);
            //System.err.println(_m.name()+" produced rule "+after.getRule().humanize());
            return after;
        }
        else {
            return c.mutate(_m.mutate(root), _rand);
        }
    }

    private void log(final String pre, final IndexedRule rule) {
        try {
            System.err.println(pre+" histo for "+_m+": "+rule.getPattern().histogram());
        }
        catch(Exception e) {
            System.err.println("failed for "+pre+": "+e);
            e.printStackTrace();
        }
    }
}
