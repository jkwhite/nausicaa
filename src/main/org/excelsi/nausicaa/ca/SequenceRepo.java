package org.excelsi.nausicaa.ca;


import java.io.IOException;
import java.util.concurrent.ExecutorService;


public interface SequenceRepo {
    Plane readPlane(Sequence.Segment segment, int ord, int gen, ExecutorService pool, GOptions opt) throws IOException;
}
