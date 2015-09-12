package org.excelsi.ca;


import java.util.*;


public class Diverge extends AbstractMutator {
    public String name() { return "Diverge"; }
    public String description() { return "Replaces color in one rule"; }

    public Rule mutate(Rule r) {
        return null;
    }

    public Multirule mutate(Multirule r) {
        Rule[] rules = r.rules();
        int i = _om.nextInt(rules.length);
        int[] allcolors = r.colors();
        int[] colors = rules[i].colors();
        int bgr = rules[i].background();
        //System.err.println("r bg: "+r.background());

        int[] pal = disjoint(allcolors, colors);
        int nc = CA.randomColor();
        int ch;
        do {
            ch = _om.nextInt(colors.length);
        } while(colors[ch]==bgr);
        colors[ch] = nc;
        Rule[] nr = new Rule[rules.length];
        for(int j=0;j<rules.length;j++) {
            if(j!=i) {
                nr[j] = rules[j];
            }
            else {
                //nr[j] = rules[j].origin().create(ac, Rule1D.archetype(ac, rules[j].toPattern()[0].length-1), r.background());
                //nr[j] = new Ruleset1D(colors, rules[j].toPattern()[0].length-1).random().next();
                nr[j] = rules[j].origin().derive(colors, rules[j].length()).random().next();
            }
        }
        return (Multirule) r.origin().create((Object[])nr);
    }

    private int[] disjoint(int[] set, int[] remove) {
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
