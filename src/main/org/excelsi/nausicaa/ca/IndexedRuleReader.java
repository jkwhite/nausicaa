package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.IOException;


public class IndexedRuleReader {
    private final DataInputStream _in;


    public IndexedRuleReader(DataInputStream dis) {
        _in = dis;
    }

    public IndexedRule read() throws IOException {
        IndexedPattern p = IndexedPattern.read(_in);
        switch(p.archetype().dims()) {
            case 1:
                return new IndexedRule1d(p);
            case 2:
                return new IndexedRule2d(p);
            default:
                throw new IllegalStateException("unsupported dimensionality "+p.archetype().dims());
        }
    }
}
