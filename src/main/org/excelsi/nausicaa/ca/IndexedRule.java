package org.excelsi.nausicaa.ca;


public interface IndexedRule extends Rule {
    IndexedPattern getPattern();
    IndexedRule derive(IndexedPattern pattern);
    IndexedRuleset origin();
}
