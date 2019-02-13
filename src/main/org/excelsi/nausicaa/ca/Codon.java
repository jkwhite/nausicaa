package org.excelsi.nausicaa.ca;


import java.util.Random;


public interface Codon {
    void op(int[] p, IntTape t);
    default void op(int[] p, IntTape t, Pattern.Ctx ctx) {
        op(p, t);
    }
    default void op(float[] p, FloatTape t) { }
    default void op(float[] p, FloatTape t, Pattern.Ctx ctx) {
        op(p, t);
    }
    default boolean supports(Values v) { return v==Values.discrete; }
    Codon copy();
    String code();
    boolean usesPattern();
    default boolean usesContext() { return false; }
    default boolean usesTape() { return false; }
    default boolean reversible() { return true; }
    default boolean symmetric() { return true; }
    default boolean deterministic() { return true; }
    default String generate(Random r) {
        return code();
    }
    default void tick() {
    }
    default Codon chain(Codon c) {
        return new Codons.Chain(this, c);
    }
}
