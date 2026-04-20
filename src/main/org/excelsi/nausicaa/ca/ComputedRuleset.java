package org.excelsi.nausicaa.ca;


public class ComputedRuleset extends AbstractComputedRuleset {
    public ComputedRuleset(final Archetype a) {
        this(a, Languages.universal());
    }

    public ComputedRuleset(final Archetype a, final Language lang) {
        this(a, lang, 0f);
    }

    public ComputedRuleset(final Archetype a, final Language lang, final float transition) {
        super(a, lang, transition);
    }
}
