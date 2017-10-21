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
        return new Iterator<Rule>() {
            @Override public boolean hasNext() {
                return true;
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override public Rule next() {
                //return new ComputedRule2d(new ComputedPattern(_a, ComputedPattern.random(_a, r)));
                //return new MutatingRule(new ComputedRule2d(new ComputedPattern(_a, ComputedPattern.random(_a, r))), r);
                /*
                return new SequenceRule(
                    new SequenceRule.Sequence()
                        .s(r.nextInt(40)+30, new ComputedRule2d(new ComputedPattern(_a, ComputedPattern.random(_a, r))))
                        .s(r.nextInt(40)+30, new ComputedRule2d(new ComputedPattern(_a, ComputedPattern.random(_a, r))))
                        .s(r.nextInt(40)+30, new ComputedRule2d(new ComputedPattern(_a, ComputedPattern.random(_a, r))))
                    );
                */
                return new ComputedRule2d(
                    new SequencePattern(
                        new SequencePattern.Sequence()
                            .s(r.nextInt(70)+70, new ComputedPattern(_a, ComputedPattern.random(_a, r)))
                            //.s(r.nextInt(70)+70, new ComputedPattern(_a, ComputedPattern.random(_a, r)))
                            //.s(r.nextInt(70)+70, new ComputedPattern(_a, ComputedPattern.random(_a, r)))
                            //.s(r.nextInt(70)+70, new ComputedPattern(_a, ComputedPattern.random(_a, r)))
                            //.s(r.nextInt(70)+70, new ComputedPattern(_a, ComputedPattern.random(_a, r)))
                            //.s(r.nextInt(70)+70, new ComputedPattern(_a, ComputedPattern.random(_a, r)))
                        )
                    );
            }
        };
    }

    @Override public Rule create(Object... args) {
        final String[] gs = args[0].toString().split(",");
        SequencePattern.Sequence s = new SequencePattern.Sequence();
        for(String g:gs) {
            int c = 100;
            if(Character.isDigit(g.charAt(0))) {
                String[] cg = g.split(":");
                c = Integer.parseInt(cg[0]);
                g = cg[1];
            }
            System.err.println("time: "+c+", rule: "+g);
            s.s(c, new ComputedPattern(_a,
                new ComputedPattern.MachineElf(new Machine(_a, new Genome(g)))));
        }
        return new ComputedRule2d(new SequencePattern(s));
    }

    @Override public Ruleset derive(int[] colors, int len) {
        throw new UnsupportedOperationException();
    }
}
