package org.excelsi.nausicaa.ca;


public class ComputedRuleset extends AbstractComputedRuleset {
    public ComputedRuleset(final Archetype a) {
        super(a, Languages.universal());
    }

    public ComputedRuleset(final Archetype a, final Language lang) {
        super(a, lang);
    }
}
