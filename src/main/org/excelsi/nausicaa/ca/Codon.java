package org.excelsi.nausicaa.ca;


import java.util.Random;


public interface Codon {
    void op(int[] p, Tape t);
    Codon copy();
    String code();
    boolean usesPattern();
    default boolean reversible() { return true; }
    default String generate(Random r) {
        return code();
    }
    default void tick() {
    }
}
