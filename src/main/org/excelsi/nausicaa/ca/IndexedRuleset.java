package org.excelsi.nausicaa.ca;


public interface IndexedRuleset extends Ruleset {
    IndexedRuleset getHyperrules();
    IndexedRule custom(IndexedPattern.Transform transform);
    IndexedRule custom(IndexedRule source, IndexedPattern.BinaryTransform transform);
    IndexedRule merge(IndexedRule rule1, IndexedRule rule2);
    IndexedRuleset derive(Archetype a);
    IndexedRuleset derive(Archetype a, IndexedRuleset hyper);
    IndexedRule derive(Archetype a, IndexedRule source);
}
