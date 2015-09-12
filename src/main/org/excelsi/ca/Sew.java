package org.excelsi.ca;


public class Sew extends AbstractMutator {
    public String name() { return "Sew"; }
    public String description() { return "Modifies a rule so that it produces another rule's color"; }

    public Rule mutate(Rule r) {
        return null;
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        //System.err.println("mutating "+r);
        Rule[] rules = r.rules();
        if(rules.length<2) {
            throw new MutationFailedException("sew failed: only one rule");
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
                    int[][] pats = rules[si].toPattern();
                    int[] dc = rules[di].colors();
                    int idx;
                    do {
                        idx = dc[_om.nextInt(dc.length)];
                    } while(idx==rules[si].background());
                    for(int[] pat:pats) {
                        if(_om.nextInt(100)<33) {
                            pat[pat.length-1] = idx;
                        }
                    }
                    //nr[i] = h.mutate(rules[i]);
                    nr[i] = rules[si].origin().create(sc, pats, rules[si].background());
                }
                catch(IllegalArgumentException e) {
                    System.err.println("sew failed: not enough colors");
                    nr[i] = rules[i];
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
