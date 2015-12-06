package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public interface Rule extends java.io.Serializable, Humanizable {
    Ruleset origin();
    int[][] toPattern();
    int[] colors();
    int background();
    int length();
    int dimensions();
    int colorCount();
    String id();
    void tick();
    //String toIncantation();
    //void init(CA c, Initialization i);
    //int getSuggestedInterval(CA c);
    void write(DataOutputStream dos) throws IOException;
    float generate(Plane c, int start, int end, boolean stopOnSame, boolean overwrite, Updater u);
    Iterator<Plane> frameIterator(Plane initial);
    default Stream<Plane> stream(Plane initial) {
        Iterable<Plane> it = ()->frameIterator(initial);
        return StreamSupport.stream(it.spliterator(), false);
    }

    interface Updater {
        void update(Rule r, int start, int current, int end);
        long interval();
    }
}
