package org.excelsi.ca;


import java.util.*;


public class Gray {
    public static Rule rule(int i) {
        if(i>255) throw new IllegalArgumentException(i+" > 255");
        Ruleset1D rs = new Ruleset1D(new int[]{CA.pack(255, 255, 255, 255), CA.pack(0, 0, 0, 255)}, 3);
        Rule r = null;
        for(Iterator<Rule> ir=rs.iterator();i>=0;i--) {
            r = ir.next();
        }
        return r;
    }
}
