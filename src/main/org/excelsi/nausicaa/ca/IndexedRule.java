package org.excelsi.nausicaa.ca;


public interface IndexedRule extends Rule {
    IndexedPattern getPattern();
    IndexedRule derive(IndexedPattern pattern);
    IndexedRule derive(IndexedPattern.Transform transform);
    IndexedRule getMetarule();
    IndexedRule withMetarule(IndexedRule meta);
    IndexedRuleset origin();
}
