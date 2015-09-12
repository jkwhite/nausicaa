package org.excelsi.ca;


public class Life extends AbstractMutator {
    public String name() { return "Life"; }
    public String description() { return "Conway's Life"; }

    public Rule mutate(Rule r) throws MutationFailedException {
        int[][] pats = r.toPattern();
        int[] colors = r.colors();
        int bgr = r.background();
        for(int[] pat:pats) {
            pat[pat.length-1] = thresh(pat, colors, bgr, 2, 3);
        }
        return r.origin().create(colors, pats, bgr);
    }

    public int thresh(int[] pat, int[] colors, int bgr, int lb, int ub) {
        int total = 0;
        for(int i=0;i<pat.length-1;i++) {
            if(i==4) continue;
            if(pat[i]!=bgr) total++;
        }
        if(pat[4]==bgr) {
            return total==3?nonbgr(colors,bgr):bgr;
        }
        else {
            return total<lb?bgr
                  :total>ub?bgr
                  :nonbgr(colors,bgr);
        }
    }

    public int nonbgr(int[] colors, int bgr) {
        int x = _om.nextInt(colors.length);
        for(int i=0;i<colors.length;i++) {
            if(colors[x]!=bgr) {
                return colors[x];
            }
            if(++x==colors.length) {
                x = 0;
            }
        }
        throw new Error();
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
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
