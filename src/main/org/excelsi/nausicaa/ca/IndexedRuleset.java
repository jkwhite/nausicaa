package org.excelsi.nausicaa.ca;


public interface IndexedRuleset extends Ruleset {
    IndexedRule custom(IndexedPattern.Transform transform);
    IndexedRule custom(IndexedRule source, IndexedPattern.BinaryTransform transform);
    IndexedRule merge(IndexedRule rule1, IndexedRule rule2);
    IndexedRuleset derive(Archetype a);
    IndexedRule derive(Archetype a, IndexedRule source);
}
