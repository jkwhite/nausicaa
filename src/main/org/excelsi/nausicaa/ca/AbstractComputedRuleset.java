package org.excelsi.nausicaa.ca;


import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;


public class AbstractComputedRuleset implements Ruleset {
    private final Archetype _a;
    private final Language _lang;


    //public AbstractComputedRuleset(Archetype a) {
        //this(a, Languages.universal());
    //}

    public AbstractComputedRuleset(Archetype a, Language lang) {
        _a = a;
        _lang = lang;
    }

    public Language language() {
        return _lang;
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
                return new ComputedRule2d(
                    new SequencePattern(
                        new SequencePattern.Sequence()
                            .s(
                                r.nextInt(70)+70,
                                null,
                                null,
                                new ComputedPattern(
                                    _a,
                                    ComputedPattern.random(new Implicate(_a, new Datamap(), _lang), r)))
                        ),
                    AbstractComputedRuleset.this
                    );
            }
        };
    }

    @Override public Iterator<Rule> random(final Random r, final Implicate i) {
        return new Iterator<Rule>() {
            @Override public boolean hasNext() {
                return true;
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override public Rule next() {
                return new ComputedRule2d(
                    new SequencePattern(
                        new SequencePattern.Sequence()
                            .s(r.nextInt(70)+70, null, null, new ComputedPattern(_a, /*i.language(),*/ ComputedPattern.random(i, r)))
                        ),
                    AbstractComputedRuleset.this
                    );
            }
        };
    }

    @Override public Rule create(Object... args) {
        //System.err.println("################ CREATING WITH LANG: "+_lang);
        final String genome = args[0].toString();
        final GenomeParser gp = new GenomeParser(_a, _lang);
        if(args.length>1 && args[1] instanceof MutationFactor) {
            gp.mutationFactor((MutationFactor)args[1]);
        }
        return gp.parse(genome, this);
    }

    @Override public Ruleset derive(int[] colors, int len) {
        throw new UnsupportedOperationException();
    }
}
