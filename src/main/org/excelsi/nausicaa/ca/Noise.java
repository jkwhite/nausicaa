package org.excelsi.nausicaa.ca;


public class Noise extends AbstractMutator {
    public String name() { return "Noise"; }
    public String description() { return "Introduces random fluctuations in patterns"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r) throws MutationFailedException {
        //IndexedPattern p = r.getPattern();
        return r.derive((a, target)->{
            for(int i=0;i<target.length;i++) {
                if(target[i]>0 && chance()) {
                    target[i] = (byte) (_om.nextInt(a.colors()));
                }
            }
        });
    }

    public Rule mutate(Rule r) {
        int[][] pats = r.toPattern();
        int[] colors = r.colors();
        int max = Math.max(1, pats[0].length/6);
        //System.err.println("making "+max+" changes");
        for(int i=0;i<max;i++) {
            int idx = _om.nextInt(pats.length);
            int colidx;
            do {
                colidx = _om.nextInt(colors.length);
            } while(pats[idx][pats[idx].length-1]==colors[colidx]);
            pats[idx][pats[idx].length-1] = colors[colidx];
        }
        //return new Rule1D(colors, pats, r.background());
        return r.origin().create(colors, pats, r.background());
    }

    public Multirule mutate(Multirule r) {
        //System.err.println("mutating "+r);
        Rule[] rules = r.rules();
        Rule[] nr = new Rule[rules.length];
        boolean none = true;
        for(int i=0;i<rules.length;i++) {
            if(_om.nextBoolean()||(none&&i==rules.length-1)) {
                nr[i] = mutate(rules[i]);
                none = false;
            }
            else {
                nr[i] = rules[i];
                //nr[i] = rules[i].copy();
            }
        }
        //return new Multirule1D(nr);
        return (Multirule) r.origin().create((Object[])nr);
    }
}
