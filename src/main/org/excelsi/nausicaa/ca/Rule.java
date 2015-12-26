package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public interface Rule extends java.io.Serializable, Humanizable {
    Archetype archetype();
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
    Iterator<Plane> frameIterator(Plane initial, ExecutorService pool);
    default Stream<Plane> stream(Plane initial, ExecutorService pool) {
        Iterable<Plane> it = ()->frameIterator(initial, pool);
        return StreamSupport.stream(it.spliterator(), false);
    }

    interface Updater {
        void update(Rule r, int start, int current, int end);
        long interval();
    }
}
