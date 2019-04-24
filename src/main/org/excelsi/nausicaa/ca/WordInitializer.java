package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import com.google.gson.*;


public class WordInitializer implements Initializer {
    public static String WORD = "abcdef";
    public static String ALPAHBET = "abcdef";
    public static String TARGET = "abcdef";
    private final String _alphabet;
    private final String _input;


    public WordInitializer() {
        this(ALPAHBET, WORD);
    }

    //public WordInitializer(String alphabet) {
        //_alphabet = alphabet;
    //}

    public WordInitializer(String alphabet, String input) {
        _alphabet = alphabet;
        _input = input;
    }

    @Override public String humanize() {
        return "Word";
    }

    @Override public void init(Plane plane, Rule rule, Random random) {
        if(rule.archetype().isDiscrete()) {
            initDisc((IntPlane)plane, rule, random);
        }
        else {
            throw new UnsupportedOperationException("CONTINUOUS");
        }
    }

    private void initDisc(IntPlane plane, Rule rule, Random random) {
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

    public String formatString(Plane iplane, final String input) {
        IntPlane plane = (IntPlane) iplane;
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

    @Override public void write(PrintWriter w) {
        w.println(Initializers.word.name());
        w.println(_alphabet);
        w.println(_input);
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", "word");
        o.addProperty("alphabet", _alphabet);
        o.addProperty("input", _input);
        return o;
    }

    @Override public Mutatable mutate(MutationFactor m) {
        return new WordInitializer(_alphabet, _input);
    }

    @Override public boolean supportsMutation() {
        return false;
    }

    public static WordInitializer read(BufferedReader r, int version) throws IOException {
        return new WordInitializer(
            r.readLine(),
            r.readLine()
        );
    }

    public static WordInitializer fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        return new WordInitializer(
            Json.string(o, "alphabet"),
            Json.string(o, "input")
        );
    }
}
