package org.excelsi.nausicaa.ca;


public class RandomMutator extends AbstractMutator {
    public String name() { return "Random"; }
    public String description() { return "Randomly creates a new rule"; }

    //public Rule mutate(Rule r) {
        //return r.origin().random(_om).next();
    //}
//
    //public Multirule mutate(Multirule r) {
        //return (Multirule) r.origin().random(_om).next();
    //}
}
