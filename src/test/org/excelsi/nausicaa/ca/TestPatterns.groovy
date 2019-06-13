package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.excelsi.nausicaa.ca.Codons.*;


public class TestPatterns extends GroovyTestCase {
    private static final double T = 0.001d;


    public void testPatterns() {
        Reader r = new FileReader("src/test/org/excelsi/nausicaa/ca/patterns.txt")
        def tests = []
        r.eachLine { p ->
            def st = p.split("\t")
            def t = new Tst(type:st[0], pat:st[1].split(" "), genome:st[2], res:st[3])
            tests << t
        }
        System.out.println("read tests: ${tests}")
        tests.each { t ->
            t.run()
        }
    }

    class Tst {
        String type
        List pat
        String genome
        String res

        public String toString() { return "type: ${type}, pat:${pat}, genome:${genome}, res:${res}" }

        public void run() {
            def t
            def p
            def io
            def a
            switch(type) {
                case "I":
                    a = new Archetype(2, 1, 1000)
                    t = new IntTape(32768)
                    io = new IO(Values.discrete)
                    p = new int[pat.size()]
                    for(int i=0;i<p.length;i++) { p[i] = pat[i] as Integer }
                    io.ii = p
                    break;
                case "D":
                    a = new Archetype(2, 1, 1000, Archetype.Neighborhood.moore, Values.continuous)
                    t = new FloatTape(32768)
                    io = new IO(Values.continuous)
                    p = new double[pat.size()]
                    for(int i=0;i<p.length;i++) { p[i] = pat[i] as Double }
                    io.fi = p
                    break;
                default:
                    throw new IllegalArgumentException("unknown tape type: '${type}'")
            }
            io.ctx = new Pattern.Ctx()
            io.ctx.r = new Random()
            def g = new Genome(genome)
            def im = new Implicate(a, new Datamap(), Languages.universal())
            def codons = g.codons(im)
            def machine = new Machine(im, g, true)
            machine.compute(io)
            switch(type) {
                case "I":
                    assertEquals("failed test '${toString()}'", res as Integer, io.io)
                    break;
                case "D":
                default:
                    assertEquals("failed test '${toString()}'", res as Double, io.fo, T)
                    break;
            }
        }
    }
}
