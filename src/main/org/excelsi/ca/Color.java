package org.excelsi.ca;


import java.util.*;


public class Color extends AbstractMutator {
    public String name() { return "Color"; }
    public String description() { return "Increases color spectrum for one rule"; }


    public Rule mutate(Rule r) {
        int[] colors = r.colors();
        int nc = CA.randomColor(_om);
        int[] ac = new int[colors.length+1];
        System.arraycopy(colors, 0, ac, 0, colors.length);
        ac[ac.length-1] = nc;
        return r.origin().derive(ac, r.length()).random(_om).next();
    }

    public Multirule mutate(Multirule r) {
        Rule[] rules = r.rules();
        int i = _om.nextInt(rules.length);
        int[] allcolors = r.colors();
        int[] colors = rules[i].colors();
        //System.err.println("r bg: "+r.background());

        int[] pal = disjoint(allcolors, colors);
        int nc;
        if(pal.length==0) {
            nc = CA.randomColor(_om);
        }
        else {
            nc = pal[_om.nextInt(pal.length)];
        }
        int[] ac = new int[colors.length+1];
        System.arraycopy(colors, 0, ac, 0, colors.length);
        ac[ac.length-1] = nc;
        Rule[] nr = new Rule[rules.length];
        for(int j=0;j<rules.length;j++) {
            if(j!=i) {
                nr[j] = rules[j];
            }
            else {
                //nr[j] = rules[j].origin().create(ac, Rule1D.archetype(ac, rules[j].toPattern()[0].length-1), r.background());
                nr[j] = rules[j].origin().derive(ac, rules[j].length()).random().next();
                //nr[j] = new Ruleset1D(ac, rules[j].toPattern()[0].length-1).random().next();
            }
        }
        return (Multirule) r.origin().create((Object[])nr);
    }

    static public int[] disjoint(int[] set, int[] remove) {
        Set<Integer> s = new HashSet<Integer>();
        for(int i:set) {
            s.add(i);
        }
        for(int i:remove) {
            s.remove(i);
        }
        int[] ret = new int[s.size()];
        int j = 0;
        for(Integer i:s) {
            ret[j++] = i.intValue();
        }
        return ret;
    }
}
