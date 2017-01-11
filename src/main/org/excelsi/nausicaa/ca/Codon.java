package org.excelsi.nausicaa.ca;


import java.util.Random;


public interface Codon {
    void op(byte[] p, Tape t);
    String code();
    default String generate(Random r) {
        return code();
    }
    default void tick() {
    }
}
