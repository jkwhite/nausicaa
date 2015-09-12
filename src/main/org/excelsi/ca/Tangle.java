package org.excelsi.ca;


public class Tangle extends AbstractMutator {
    public String name() { return "Tangle"; }
    public String description() { return "Entangles the color spaces of multiple rules"; }

    public Rule mutate(Rule r) {
        return null;
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        //System.err.println("mutating "+r);
        Rule[] rules = r.rules();
        if(rules.length<2) {
            //System.err.println("tangle failed: only one rule");
            //return r;
            throw new MutationFailedException("only one rule");
        }
        Rule[] nr = new Rule[rules.length];
        int si = _om.nextInt(rules.length);
        int di = si+1;
        if(di==rules.length) {
            di = 0;
        }
        for(int i=0;i<rules.length;i++) {
            if(i==di) {
                int[] sc = rules[si].colors();
                try {
                    Hue h = new Hue(false, sc[_om.nextInt(sc.length)]);
                    nr[i] = h.mutate(rules[i]);
                }
                catch(IllegalArgumentException e) {
                    throw new MutationFailedException("not enough colors");
                    //System.err.println("tangle failed: not enough colors");
                    //nr[i] = rules[i];
                }
            }
            else {
                nr[i] = rules[i];
            }
        }
        //return new Multirule1D(nr);
        return (Multirule) r.origin().create((Object[])nr);
    }
}
