package org.excelsi.nausicaa.ca;


import java.util.Random;


public interface Codon {
    //void op(int[] p, IntTape t);
    void op(int[] p, IntTape t, Pattern.Ctx ctx);
    //void op(double[] p, FloatTape t);
    void op(double[] p, FloatTape t, Pattern.Ctx ctx);
    boolean supports(Values v);
    Codon copy();
    String code();
    boolean usesPattern();
    default boolean usesContext() { return false; }
    default boolean usesTape() { return false; }
    default boolean reversible() { return true; }
    default boolean symmetric() { return true; }
    default boolean deterministic() { return true; }
    default boolean positioning() { return false; }
    default String generate(Random r) {
        return code();
    }
    default void tick() {
    }
    default Codon chain(Codon c) {
        return new Codons.Chain(this, c);
    }
    default void compile(Compiler c) { }
}
