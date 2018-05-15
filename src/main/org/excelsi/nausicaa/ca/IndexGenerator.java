package org.excelsi.nausicaa.ca;


import java.util.Random;


public class IndexGenerator {
    public Index build(Archetype a, Random r, String name) {
        if(a.neighborhood()!=Archetype.Neighborhood.vonneumann) {
            throw new UnsupportedOperationException();
        }
        int max = a.totalPatterns();
        final StringBuilder g = new StringBuilder()
            .append("1 rot4");
        for(int i=0;i<max;i++) {
            if(r.nextInt(100)<20) {
                String t = Integer.toString(i, a.colors());
                while(t.length()<a.sourceLength()) {
                    t = "0"+t;
                }
                g.append(" ").append(t);
                int tgt = r.nextInt(a.colors());
                g.append(tgt);
            }
        }
        return new Index(name, g.toString());
    }
}
