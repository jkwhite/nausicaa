package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public interface Rule extends java.io.Serializable, Humanizable {
    Archetype archetype();
    Ruleset origin();
    IndexedRule getHyperrule();
    int[][] toPattern();
    int[] colors();
    int background();
    int length();
    int dimensions();
    int colorCount();
    int width();
    int height();
    String id();
    void copy(Plane p);
    void tick();
    //String toIncantation();
    //void init(CA c, Initialization i);
    //int getSuggestedInterval(CA c);
    void write(DataOutputStream dos) throws IOException;
    void write(PrintWriter w);
    float generate(Plane c, int start, int end, ExecutorService pool, boolean stopOnSame, boolean overwrite, Updater u);
    Iterator<Plane> frameIterator(Plane initial, ExecutorService pool, boolean doubleBuffer, int parallel);

    default Stream<Plane> stream(Plane initial, ExecutorService pool, boolean doubleBuffer, int parallel) {
        Iterable<Plane> it = ()->frameIterator(initial, pool, doubleBuffer, parallel);
        return StreamSupport.stream(it.spliterator(), false);
    }

    interface Updater {
        void update(Rule r, int start, int current, int end);
        long interval();
    }
}
