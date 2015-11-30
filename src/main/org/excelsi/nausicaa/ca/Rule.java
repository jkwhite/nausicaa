package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;


public interface Rule extends java.io.Serializable, Humanizable {
    Ruleset origin();
    int[][] toPattern();
    int[] colors();
    int background();
    int length();
    int dimensions();
    int colorCount();
    String id();
    //String toIncantation();
    //void init(CA c, Initialization i);
    //int getSuggestedInterval(CA c);
    void write(DataOutputStream dos) throws IOException;
    float generate(Plane c, int start, int end, boolean stopOnSame, boolean overwrite, Updater u);
    Iterator<Plane> frameIterator(Plane initial);

    interface Updater {
        void update(Rule r, int start, int current, int end);
        long interval();
    }
}
