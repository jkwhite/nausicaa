package org.excelsi.ca;


import java.util.*;


public class Hue extends AbstractMutator {
    private boolean _background = true;
    private int _color = -1;
    private boolean _resultOnly;


    public String name() { return "Hue"; }
    public String description() { return "Modifies colors"; }

    public Hue() {
    }

    public Hue(boolean useBackground, int color) {
        _background = useBackground;
        _color = color;
    }

    public void setResultOnly(boolean r) {
        _resultOnly = r;
    }

    public Rule mutate(Rule r) throws MutationFailedException {
        int[][] pats = r.toPattern();
        int[] colors = r.colors();
        int bgr = r.background();
        int colidx;
        do {
            colidx = _om.nextInt(colors.length);
        } while(!_background&&colors[colidx]==bgr);
        int oc = colors[colidx];
        int nc = _color!=-1?_color:/*CA.pack(Rand.om.nextInt(256), Rand.om.nextInt(256),
                Rand.om.nextInt(256), 255)*/ CA.randomColor(_om);
        //System.err.println( "replacing "+oc+" with "+nc);
        if(bgr==oc) {
            bgr = nc;
        }
        return replace(r, oc, nc, _resultOnly);
        //return new Rule1D(colors, pats, bgr);
        //return r.origin().create(colors, pats, bgr);
    }

    public static Rule replace(Rule r, int oc, int nc) throws MutationFailedException {
        return replace(r, oc, nc, false);
    }

    public static Rule replace(Rule r, int oc, int nc, boolean resultOnly) throws MutationFailedException {
        int[][] pats = r.toPattern();
        int[] colors = r.colors();
        int bgr = r.background();
        //System.err.println( "in replace: "+oc+" -> "+nc);
        if(bgr==oc) {
            bgr = nc;
            //System.err.println( "set bgr");
        }
        for(int i=0;i<pats.length;i++) {
            for(int j=0;j<pats[i].length;j++) {
                if(pats[i][j]==oc) {
                    if(!resultOnly || j==pats[i].length-1) {
                        pats[i][j] = nc;
                    }
                }
            }
        }
        List<Integer> elim = new ArrayList<Integer>();
        for(int i=0;i<colors.length;i++) {
            if(colors[i]==oc) {
                colors[i] = nc;
            }
            Integer x = new Integer(colors[i]);
            if(!elim.contains(x)) {
                elim.add(x);
            }
        }
        if(elim.size()<2) {
            throw new MutationFailedException("need at least 2 colors");
        }
        //System.err.println( "elim: "+elim);
        //System.err.println( "elim size: "+elim.size());
        //System.err.println( "colors size: "+colors.length);
        if(elim.size()!=colors.length) {
            //System.err.println( "compacting");
            // duplicate color, need to recreate
            colors = new int[elim.size()];
            for(int i=0;i<colors.length;i++) {
                colors[i] = elim.get(i).intValue();
            }
            //System.err.println( "len: "+(pats[0].length-1));
            //pats = Rule1D.archetype(colors, pats[0].length-1);
            pats = new Ruleset1D(colors, pats[0].length-1).random().next().toPattern();
        }
        return r.origin().create(colors, pats, bgr);
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        Rule[] rules = r.rules();
        Rule[] nr = new Rule[rules.length];

        int[] allcolors = r.colors();
        int bgr = r.background();
        int colidx;
        do {
            colidx = _om.nextInt(allcolors.length);
        } while(!_background&&allcolors[colidx]==bgr);
        int oc = allcolors[colidx];
        //int nc = _color!=-1?_color:CA.pack(Rand.om.nextInt(256), Rand.om.nextInt(256),
                //Rand.om.nextInt(256), 255);
        int nc = _color!=-1?_color:CA.randomColor(_om);
        if(bgr==oc) {
            bgr = nc;
        }
        for(int i=0;i<rules.length;i++) {
            int[][] pats = rules[i].toPattern();
            int[] colors = rules[i].colors();
            nr[i] = replace(rules[i], oc, nc);
        }
        return (Multirule) r.origin().create((Object[])nr);
    }
}
