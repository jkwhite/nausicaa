package org.excelsi.ca;


public class Transpose extends AbstractMutator {
    public String name() { return "Transpose"; }
    public String description() { return "Transposes a result color from another rule"; }

    public Rule mutate(Rule r) {
        return null;
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        //System.err.println("mutating "+r);
        Rule[] rules = r.rules();
        if(rules.length<2) {
            //System.err.println("transpose failed: only one rule");
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
                    h.setResultOnly(true);
                    nr[i] = h.mutate(rules[i]);
                }
                catch(IllegalArgumentException e) {
                    //System.err.println("transpose failed: not enough colors");
                    //nr[i] = rules[i];
                    throw new MutationFailedException("not enough colors");
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
