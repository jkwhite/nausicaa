package org.excelsi.nausicaa.ca;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class Pools {
    private static final ExecutorService ADHOC = Executors.newFixedThreadPool(4, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    private static final ExecutorService PRELUDE = Executors.newFixedThreadPool(4, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });
    /*
    private static final ExecutorService BGR = Executors.newFixedThreadPool(20, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    private static final ExecutorService CORES = Executors.newFixedThreadPool(4, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });
    */

    public static ExecutorService adhoc() {
        return ADHOC;
    }

    public static int adhocSize() {
        return 1;
    }

    public static ExecutorService prelude() {
        return PRELUDE;
    }

    public static int preludeSize() {
        return 4;
    }

    /*
    public static ExecutorService bgr() {
        return BGR;
    }

    public static ExecutorService core() {
        return CORES;
    }
    */

    public static ExecutorService named(final String name, final int size) {
        return Executors.newFixedThreadPool(size, new ThreadFactory() {
            long n = 0;
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(name+"-"+n);
                n++;
                return t;
            }
        });
    }
}
