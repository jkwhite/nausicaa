package org.excelsi.nausicaa.ca;


public interface Worker {
    void frame(Plane p);
    void frame(Plane p1, Plane p2);
    Stats getStats();


    static class Stats {
        public long timeMsec;
        public long frames;


        @Override public String toString() {
            return String.format(
                "total msec: %d, total frames: %d, avg: %f",
                timeMsec,
                frames,
                (float)timeMsec/(float)frames
            );
        }
    }
}
