package org.excelsi.nausicaa.ca;


import java.io.PrintWriter;
import java.io.IOException;


public class ComputedRuleWriter {
    private final PrintWriter _w;


    public ComputedRuleWriter(PrintWriter w) {
        _w = w;
    }

    public void writeRule(Rule r) throws IOException {
        ComputedRule2d cr = (ComputedRule2d) r;
        _w.println("computed");
        cr.archetype().write(_w);
        cr.write(_w);
    }
}
