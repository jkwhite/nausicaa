package org.excelsi.ca;


public interface Mutator {
    String name();
    String description();
    Rule mutate(Rule r) throws MutationFailedException;
    Multirule mutate(Multirule r) throws MutationFailedException;
    void setRandom(java.util.Random r);
}
