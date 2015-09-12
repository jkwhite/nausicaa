package org.excelsi.ca;


import java.util.*;


public class Expand extends AbstractMutator {
    public Rule mutate(Rule r) {
        if(r.dimensions()>1) {
            return null;
        }
        int[][] pats = r.toPattern();
        int[] colors = r.colors();
        int len = r.length();
        len += 2;
        if(len==9) {
            len = 3;
        }

        return r.origin().derive(colors, len).random(_om).next();
    }

    public String name() { return "Expand"; }
    public String description() { return "Increases pattern space"; }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        if(r.getClass() != Multirule1D.class) {
            throw new MutationFailedException("expand is too dangerous for this rule");
        }
        Rule[] rules = r.rules();
        int i = _om.nextInt(rules.length);
        int[][] pats = rules[i].toPattern();
        int[] colors = rules[i].colors();
        int len = rules[i].length();
        len += 2;
        if(len==9) {
            len = 3;
        }
        //int[][] pats2 = Rule1D.archetype(colors, len);

        Rule[] nr = new Rule[rules.length];
        for(int j=0;j<rules.length;j++) {
            if(j!=i) {
                nr[j] = rules[j];
            }
            else {
                //nr[j] = rules[j].origin().create(colors, pats2, r.background());
                //nr[j] = new Ruleset1D(colors, len).random().next();
                nr[j] = rules[j].origin().derive(colors, len).random().next();
            }
        }
        return (Multirule) r.origin().create((Object[])nr);
    }
}
