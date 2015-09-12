package org.excelsi.ca;


public interface Rule extends java.io.Serializable {
    public enum Initialization { single, random, arabesque, image };
    Ruleset origin();
    int[][] toPattern();
    int[] colors();
    int background();
    int length();
    int dimensions();
    String toIncantation();
    void init(CA c, Initialization i);
    int getSuggestedInterval(CA c);
    float generate(CA c, int start, int end, boolean stopOnSame, boolean over, Updater u);
    Rule copy();
    Rule mutate(Mutator m) throws MutationFailedException;
    void setInterceptor(Interceptor i);
    void setFabric(Fabric f);
    void setRandom(java.util.Random r);
    //void setEvaporate(boolean evaporate);
    //void setPhaseTransition(boolean phase);
    //void setStabilityReset(boolean sr);
    void setFlag(Options o);
    void setFlag(Options o, boolean state);
    boolean getFlag(Options o);
    void setMask(java.util.BitSet mask);
    java.util.BitSet getMask();
    long toSeed();

    interface Updater {
        void update(Rule r, int start, int current, int end);
        long interval();
    }
}
