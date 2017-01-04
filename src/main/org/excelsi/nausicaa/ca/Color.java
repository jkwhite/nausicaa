package org.excelsi.nausicaa.ca;


import java.util.*;


public class Color extends AbstractMutator {
    public static final int MAX_COLORS = 12000000;
    public String name() { return "Expand"; }
    public String description() { return "Increases color spectrum for one rule"; }


    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        if(a.colors()>MAX_COLORS-1) {
            throw new MutationFailedException("at max "+MAX_COLORS+" colors");
        }
        else {
            IndexedRule nr = (IndexedRule) r.origin().derive(a.asColors(a.colors()+1).validate(f.validator()), r);
            nr = new ThickenOne((byte)(nr.getPattern().archetype().colors()-1)).mutateIndexedRule(nr, f);
            return nr;
        }
    }

    @Override public Rule mutate(Rule r) {
        final Archetype a = r.archetype().asColors(1+r.archetype().colors());
        return new ComputedRule2d(new ComputedPattern(a, ComputedPattern.random(a, _om)));
    }

    @Override public boolean supports(Rule r) {
        return true;
    }

    static private int[] disjoint(int[] set, int[] remove) {
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
