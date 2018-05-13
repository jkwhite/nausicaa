package org.excelsi.nausicaa.ca;


import java.io.BufferedReader;
import java.io.IOException;


public class ComputedRuleReader {
    private final BufferedReader _r;
    private final int _version;


    public ComputedRuleReader(BufferedReader r, int version) {
        _r = r;
        _version = version;
    }

    public Rule readRule() throws IOException {
        String line = _r.readLine();
        if(!"computed".equals(line)) {
            throw new IOException("not a computed rule: "+line);
        }
        Archetype a = Archetype.read(_r, _version);
        String genome = _r.readLine();
        return new ComputedRuleset(a).create(genome, _version);
    }
}
