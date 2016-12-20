package org.excelsi.nausicaa.ca;


import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;


public class AbstractComputedRuleset implements Ruleset {
    private final Archetype _a;


    public AbstractComputedRuleset(Archetype a) {
        _a = a;
    }

    @Override public Iterator<Rule> iterator() {
        return Collections.<Rule>emptyList().iterator();
    }

    @Override public Iterator<Rule> random(final Random r) {
        //return Collections.<Rule>emptyList().iterator();
        /*
        return Arrays.<Rule>asList(
            new ComputedRule2d(
                //new ComputedPattern(_a, ComputedPattern.cyclic(_a))
                new ComputedPattern(_a, ComputedPattern.random(_a, r))
            )
        ).iterator();
        */
        return new Iterator<Rule>() {
            @Override public boolean hasNext() {
                return true;
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override public Rule next() {
                return new ComputedRule2d(new ComputedPattern(_a, ComputedPattern.random(_a, r)));
            }
        };
    }

    @Override public Rule create(Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override public Ruleset derive(int[] colors, int len) {
        throw new UnsupportedOperationException();
    }
}
