package org.excelsi.nausicaa.ca;


public class WordDecoder implements Decoder<CharSequence> {
    private final String _alphabet;


    public WordDecoder(String alphabet) {
        _alphabet = alphabet;
    }

    @Override public CharSequence decode(Plane p) {
        StringBuilder b = new StringBuilder();
        int[] e = new int[p.getWidth()];
        ((IntPlane)p).getRow(e, p.getHeight()-1, 0);
        for(int i=0;i<e.length;i++) {
            final char c = (char) e[i];
            final char d = (char) _alphabet.indexOf(c);
            b.append(d);
        }
        return b.toString();
    }
}
