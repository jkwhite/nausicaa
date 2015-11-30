package org.excelsi.nausicaa.ca;


import java.util.*;


public class Cull extends AbstractMutator {
    public String name() { return "Cull"; }
    public String description() { return "Removes a rule"; }

    public Rule mutate(Rule r) {
        return null;
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        Rule[] rs = r.rules();
        if(rs.length>1) {
            List<Rule> rules = new LinkedList<Rule>(Arrays.asList(rs));
            rules.remove(_om.nextInt(rules.size()));
            return (Multirule) r.origin().create((Rule[])rules.toArray(new Rule[0]));
        }
        else {
            throw new MutationFailedException("only one rule");
            //System.err.println("cull failed: only one rule");
            //return r;
            //Clone c = new Clone();
            //return c.mutate(r);
        }
    }
}
