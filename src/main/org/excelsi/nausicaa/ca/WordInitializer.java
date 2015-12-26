package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.IOException;


public class WordInitializer implements Initializer {
    private final String _alphabet;
    public static String WORD = "GATTACA";
    public static String ALPAHBET = "GATC";
    public static String TARGET = "GATC";


    public WordInitializer() {
        this(ALPAHBET);
    }

    public WordInitializer(String alphabet) {
        _alphabet = alphabet;
    }

    public WordInitializer(String alphabet, String input) {
        _alphabet = alphabet;
    }

    @Override public void init(Plane plane, Rule rule, Random random) {
        String input = WORD;
        byte[] encodedInput = encodeWord(input);
        int idx = 0;
        final int colors[] = rule.colors();
        switch(rule.dimensions()) {
            case 1:
                for(int i=0;i<plane.getWidth();i++) {
                    plane.setCell(i, 0, encodedInput[idx]);
                    idx = (idx+1) % encodedInput.length;
                }
                break;
            case 2:
            default:
                for(int j=0;j<plane.getHeight();j++) {
                    for(int i=0;i<plane.getWidth();i++) {
                        plane.setCell(i, j, encodedInput[idx]);
                        idx = (idx+1) % encodedInput.length;
                    }
                }
                break;
        }
    }

    public static byte[] encodeWord(String alphabet, String w) {
        byte[] e = new byte[w.length()];
        for(int i=0;i<w.length();i++) {
            final char c = w.charAt(i);
            e[i] = (byte) alphabet.indexOf(c);
        }
        return e;
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

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.word.getId());
    }
}
