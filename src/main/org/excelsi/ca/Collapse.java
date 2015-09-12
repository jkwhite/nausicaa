package org.excelsi.ca;


import java.util.*;


public class Collapse extends AbstractMutator {
    public Rule mutate(Rule r) throws MutationFailedException {
        int[] allcolors = r.colors();
        int bgr = r.background();
        if(allcolors.length<3) {
            throw new MutationFailedException("not enough colors");
        }
        int oi = allcolors[_om.nextInt(allcolors.length)];
        int ni;
        do {
            ni = allcolors[_om.nextInt(allcolors.length)];
        } while(oi==ni);
        if(bgr==oi) {
            bgr = ni;
        }
        return Hue.replace(r, oi, ni);
    }

    public String name() { return "Collapse"; }
    public String description() { return "Reduces color spectrum"; }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        Rule[] rs = r.rules();
        int[] allcolors = r.colors();
        int bgr = r.background();
        if(allcolors.length<3) {
            throw new MutationFailedException("not enough colors");
        }
        int oi = allcolors[_om.nextInt(allcolors.length)];
        int ni;
        do {
            ni = allcolors[_om.nextInt(allcolors.length)];
        } while(oi==ni);
        if(bgr==oi) {
            bgr = ni;
        }
        //Rule[] nr = new Rule[rs.length];
        List<Rule> nr = new ArrayList<Rule>(rs.length);
        for(int i=0;i<rs.length;i++) {
            try {
                nr.add(Hue.replace(rs[i], oi, ni));
            }
            catch(IllegalArgumentException e) {
                //System.err.println("collapse failed: not enough colors");
                throw new MutationFailedException("not enough colors");
            }
        }
        return (Multirule) r.origin().create((Rule[])nr.toArray(new Rule[0]));
    }
}
