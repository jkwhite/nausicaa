package org.excelsi.nausicaa.ca;


import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class AbstractComputedRuleset implements Ruleset {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractComputedRuleset.class);

    private final Archetype _a;
    private final Language _lang;
    private final float _transition;


    public AbstractComputedRuleset(Archetype a, Language lang, float transition) {
        _a = a;
        _lang = lang;
        _transition = transition;
    }

    public Archetype archetype() {
        return _a;
    }

    public Language language() {
        return _lang;
    }

    public float transition() {
        return _transition;
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
                                    ComputedPattern.random(new Implicate(_a, new Datamap(), _lang), r))),
                            0f
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
        final String genome = args[0].toString();
        final GenomeParser gp = new GenomeParser(_a, _lang, _transition);
        Parameters ps = args.length>1 && args[1] instanceof Parameters?(Parameters)args[1]:null;
        gp.parameters(ps);
        return gp.parse(genome, derive(this, ps));
    }

    @Override public Ruleset derive(int[] colors, int len) {
        throw new UnsupportedOperationException();
    }

    private static AbstractComputedRuleset derive(AbstractComputedRuleset r, Parameters p) {
        return new ComputedRuleset(
            r.archetype(),
            r.language(),
            p!=null?p.transition():r.transition());
    }
}
