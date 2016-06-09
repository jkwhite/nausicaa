package org.excelsi.nausicaa.ca;


public class WordEncoder implements Encoder<CharSequence> {
    private final String _alphabet;


    public WordEncoder(String alphabet) {
        _alphabet = alphabet;
    }

    @Override public byte[] encode(CharSequence in) {
        byte[] e = new byte[in.length()];
        for(int i=0;i<in.length();i++) {
            final char c = in.charAt(i);
            e[i] = (byte) _alphabet.indexOf(c);
        }
        return e;
    }
}
