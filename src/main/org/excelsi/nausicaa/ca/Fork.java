package org.excelsi.nausicaa.ca;


import java.util.*;


public class Fork extends AbstractMutator {
    public String name() { return "Fork"; }
    public String description() { return "Adds a new rule similar to current rules"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        if(2*a.colors()>Color.MAX_COLORS) {
            throw new MutationFailedException("at max "+Color.MAX_COLORS+" colors");
        }
        else {
            a.asColors(2*a.colors()).validate(f.validator());
            IndexedRule nr = r.origin().derive(a, r);
            return nr.origin().merge(r, nr);
        }
    }

    //private Rule compatibilitize(Rule model, Rule r) throws MutationFailedException {
        //if(model.background()==r.background()) {
            //return r;
        //}
        //else {
            //return Hue.replace(r, r.background(), model.background());
        //}
        //Hue.replace(pats, colors, bgr, model.background());
        //return model.origin().create(colors, pats, model.background());
    //}
}
