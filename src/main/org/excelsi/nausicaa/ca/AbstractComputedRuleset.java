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
        final int version = args.length>1?((Integer)args[1]).intValue():2;
        final String[] gs = args[0].toString().replace('\n',',').split(",");
        SequencePattern.Sequence s = new SequencePattern.Sequence();
        for(final String gr:gs) {
            String g = gr.trim();
            int c = 100;
            if(g.indexOf(':')>=0) {
                String[] cg = g.split(":");
                c = Integer.parseInt(cg[0].trim());
                g = cg[1];
            }
            //g = g.replace(' ','-');
            System.err.println("time: "+c+", rule: "+g);
            s.s(c, new ComputedPattern(_a,
                new ComputedPattern.MachineElf(new Machine(_a, new Genome(g, version)))));
        }

        SequencePattern sp;
        if(args.length>1 && args[1] instanceof MutationFactor) {
            sp = new SequencePattern(s, ((MutationFactor)args[1]).transition());
        }
        else {
            sp = new SequencePattern(s);
        }
        return new ComputedRule2d(sp);
    }

    @Override public Ruleset derive(int[] colors, int len) {
        throw new UnsupportedOperationException();
    }
}
