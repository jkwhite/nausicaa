package org.excelsi.ca;


import java.util.*;


public class Order extends AbstractMutator {
    public String name() { return "Order"; }
    public String description() { return "Shuffles rule execution order"; }

    public Rule mutate(Rule r) {
        return null;
    }

    public Multirule mutate(Multirule r) {
        Rule[] rs = r.rules();
        //for(int i=0;i<rs.length;i++) {
            //rs[i] = rs[i].copy();
        //}
        List<Rule> rules = new LinkedList<Rule>(Arrays.asList(rs));
        Collections.shuffle(rules, _om);
        return (Multirule) r.origin().create((Rule[])rules.toArray(new Rule[0]));
    }
}
