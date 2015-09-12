package org.excelsi.ca;


public class Thin extends AbstractMutator {
    public String name() { return "Thin"; }
    public String description() { return "Adds more background to patterns"; }

    public Rule mutate(Rule r) {
        int[][] pats = r.toPattern();
        int[] colors = r.colors();
        int bgr = r.background();
        int count = 0;
        do {
            int idx = _om.nextInt(pats.length);
            if(pats[idx][pats[idx].length-1]!=bgr) {
                pats[idx][pats[idx].length-1] = bgr;
                count += 200;
            }
        } while(++count<1000*pats[0].length/3);
        return r.origin().create(colors, pats, bgr);
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
            }
        }
        //return new Multirule1D(nr);
        return (Multirule) r.origin().create((Object[])nr);
    }
}
