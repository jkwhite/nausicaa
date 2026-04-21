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
    /** whether this codon uses values from the context */
    default boolean usesContext() { return false; }
    /** whether this codon uses location values only from the context */
    default boolean usesLocation() { return false; }
    /** whether this codon accesses tape values via pop/peek */
    default boolean usesTape() { return false; }
    /** purpose unclear */
    default boolean reversible() { return false; }
    /** whether this codon is rotationally symmetric with respect to the pattern */
    default boolean symmetric() { return false; }
    /** whether this codon will always produce the same value given the same pattern and context */
    default boolean deterministic() { return true; }
    /** whether this codon moves the tape head directly, outside of push/pop */
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
