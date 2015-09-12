package org.excelsi.ca;


import java.util.*;


public class Chaos {
    public Ruleset space1d() {
        ArrayList<Ruleset> rules = new ArrayList<Ruleset>();
        rules.add(new Ruleset1D(new int[]{
                    CA.randomColor(),
                    CA.randomColor()}));
        return new Rulespace1D((Ruleset1D[]) rules.toArray(new Ruleset1D[0]));
    }
}
