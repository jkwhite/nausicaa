package org.excelsi.nausicaa;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import org.excelsi.nausicaa.ca.Archetype;
import org.excelsi.nausicaa.ca.CA;
import org.excelsi.nausicaa.ca.Initializer;
import org.excelsi.nausicaa.ca.IndexedRule1d;
import org.excelsi.nausicaa.ca.IndexedRuleset1d;
import org.excelsi.nausicaa.ca.IndexedPattern;
import org.excelsi.nausicaa.ca.Palette;
import org.excelsi.nausicaa.ca.Patterns;
import org.excelsi.nausicaa.ca.Plane;
import org.excelsi.nausicaa.ca.Rule;
import org.excelsi.nausicaa.ca.Ruleset;


public class Words implements Initializer {
    private final String _alphabet;
    private final Archetype _a;
    private String _input;
    private byte[] _encodedInput;


    public Words(final String alphabet) {
        _alphabet = alphabet;
        final int colors = alphabet.length();
        _a = new Archetype(1, 1, colors);
    }

    public String run(final long sidx, long eidx, final String input, final int iterations) {
        _input = input;
        _encodedInput = encodeWord(input);
        Random rand = new Random();
        Palette pal = Palette.random(_a.colors(), rand);
        Ruleset rs = new IndexedRuleset1d(_a);
        //Rule rule = rs.random(rand).next();
        final int[] row = new int[input.length()];
        System.out.println(_a);
        if(eidx==0) {
            eidx = _a.totalRules();
        }
        Plane plane = null;
        for(long idx=sidx;idx<=eidx;idx++) {
            Rule rule = new IndexedRule1d(Patterns.forIndex(_a, idx));
            //System.err.println(rule.humanize());
            final CA ca = new CA(rule, pal, this, rand, 0, input.length(), iterations, 1);
            if(plane==null) {
                plane = ca.createPlane();
            }
            else {
                ca.pooledPlane(plane);
            }
            String dec;
            boolean match = false;
            //System.out.println(input);
            if(idx%50000==0) System.out.println(idx);
            for(int i=1;i<plane.getHeight();i++) {
                plane.getRow(row, i, 0);
                dec = decodeWord(row);
                if(dec.equals(input)) {
                    match = true;
                }
                //System.out.println(dec);
                if(match) {
                    System.out.println("match: "+idx);
                    System.out.println(formatString(plane, input));
                    //return dec;
                }
            }
        }
        return null;
    }

    public String formatString(Plane plane, final String input) {
        final int[] row = new int[plane.getWidth()];
        final StringBuilder b = new StringBuilder();
        for(int i=0;i<plane.getHeight();i++) {
            plane.getRow(row, i, 0);
            final String dec = decodeWord(row);
            if(input.equals(dec)) {
                b.append("* ");
            }
            else {
                b.append("  ");
            }
            b.append(dec).append('\n');
        }
        return b.toString();
    }

    public byte[] encodeWord(String w) {
        byte[] e = new byte[w.length()];
        for(int i=0;i<w.length();i++) {
            final char c = w.charAt(i);
            e[i] = (byte) _alphabet.indexOf(c);
        }
        return e;
    }

    public String decodeWord(int[] e) {
        StringBuilder d = new StringBuilder();
        for(int i=0;i<e.length;i++) {
            final char c = _alphabet.charAt(e[i]);
            d.append(c);
        }
        return d.toString();
    }

    @Override public void init(Plane plane, Rule rule, Random random) {
        int idx = 0;
        final int colors[] = rule.colors();
        switch(rule.dimensions()) {
            case 1:
                for(int i=0;i<plane.getWidth();i++) {
                    plane.setCell(i, 0, _encodedInput[idx]);
                    idx = (idx+1) % _encodedInput.length;
                }
                break;
            case 2:
            default:
                for(int j=0;j<plane.getHeight();j++) {
                    for(int i=0;i<plane.getWidth();i++) {
                        plane.setCell(i, j, _encodedInput[idx]);
                        idx = (idx+1) % _encodedInput.length;
                    }
                }
                break;
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        throw new UnsupportedOperationException();
    }


    public static void main(String[] args) {
        final String abc = args[0];
        final String input = args[1];
        final long sidx = Long.parseLong(args[2]);
        final long eidx = Long.parseLong(args[3]);
        final int iterations = Integer.parseInt(args[4]);
        Words w = new Words(abc);
        w.run(sidx, eidx, input, iterations);
    }
}
