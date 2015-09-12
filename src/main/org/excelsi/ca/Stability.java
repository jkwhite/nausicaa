package org.excelsi.ca;


import java.util.*;


public class Stability extends AbstractMutator {
    private int _factor;


    public Stability(int factor) {
        _factor = factor;
    }

    public String name() { return _factor>0?"Stabilize":"Destabilize"; }
    public String description() { return "Alters rule stability"; }


    public Rule mutate(Rule r) throws MutationFailedException {
        int[][] pats = r.toPattern();
        int[] colors = r.colors();
        int n = _om.nextInt(colors.length);
        int a = CA.alpha(colors[n]);
        a += _factor;
        if(a>255) {
            a = 255;
        }
        if(a<128) {
            a = 128;
        }
        int oc = colors[n];
        int nc = CA.setAlpha(colors[n], a);
        int bgr = r.background();
        if(oc==bgr) {
            bgr = nc;
        }
        for(int i=0;i<pats.length;i++) {
            for(int j=0;j<pats[i].length;j++) {
                if(pats[i][j]==oc) {
                    pats[i][j] = nc;
                }
            }
        }
        colors[n] = nc;
        return r.origin().create(colors, pats, bgr);
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        Rule[] rules = r.rules();
        Rule[] nr = new Rule[rules.length];
        int n = _om.nextInt(rules.length);
        for(int i=0;i<rules.length;i++) {
            if(n==i) {
                nr[i] = mutate(rules[i]);
            }
            else {
                nr[i] = rules[i];
            }
        }
        return (Multirule) r.origin().create((Object[])nr);
    }
}
