package org.excelsi.nausicaa.ca;


import java.util.*;


public class Fork extends AbstractMutator {
    public String name() { return "Fork"; }
    public String description() { return "Adds a new rule similar to current rules"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        if(2*a.colors()>8) {
            throw new MutationFailedException("at max 9 colors");
        }
        else {
            IndexedRule nr = r.origin().derive(a, r);
            return nr.origin().merge(r, nr);
        }
    }

    public Rule mutate(Rule r) {
        return null;
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        Rule[] rules = r.rules();
        int i = _om.nextInt(rules.length);
        int[][] pats = rules[i].toPattern();
        int[] colors = rules[i].colors();
        int bgr = rules[i].background();
        /*
        int i = Rand.om.nextInt(rules.length);
        int[][] pats = rules[i].toPattern();
        int[] colors = rules[i].colors();
        Rule n = new Rule1D(colors, pats, rules[i].background());
        int[] colset = disjoint(colors(), colors);
        int col;
        if(Rand.om.nextBoolean()&&colset.length>0) {
            col = colset[Rand.om.nextInt(colset.length)];
        }
        else {
            col = CA.pack(Rand.om.nextInt(256), Rand.om.nextInt(256),
                Rand.om.nextInt(256), 255);
        }
        n = n.mutate(new Hue(false, col));
        do {
            n = n.mutate(m);
        } while(Rand.om.nextBoolean());
        Rule[] nr;
        if(Rand.om.nextBoolean()) {
        }
        else {
        }
        nr = new Rule[rules.length+1];
        System.arraycopy(rules, 0, nr, 1, rules.length);
        nr[0] = n;
        return new Multirule1D(nr);
        */

        //Rule n = compatibilitize(rules[0], rules[0].origin().random().next());
        Rule n = rules[0].origin().create(colors, pats, bgr);
        n = new Hue(false, -1).mutate(n);
        do {
            n = new Noise().mutate(n);
        } while(_om.nextBoolean());
        //System.err.println("new is: "+n);
        Rule[] nr = new Rule[rules.length+1];
        System.arraycopy(rules, 0, nr, 1, rules.length);
        nr[0] = n;
        Multirule mr = (Multirule) r.origin().create((Rule[])nr);
        return new Tangle().mutate(mr);
    }

    private Rule compatibilitize(Rule model, Rule r) throws MutationFailedException {
        if(model.background()==r.background()) {
            return r;
        }
        else {
            return Hue.replace(r, r.background(), model.background());
        }
        //Hue.replace(pats, colors, bgr, model.background());
        //return model.origin().create(colors, pats, model.background());
    }
}
