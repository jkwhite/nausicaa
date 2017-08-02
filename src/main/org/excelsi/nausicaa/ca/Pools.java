package org.excelsi.nausicaa.ca;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class Pools {
    private static final ExecutorService _bgr = Executors.newFixedThreadPool(20, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    private static final ExecutorService _cores = Executors.newFixedThreadPool(4, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });


    public static ExecutorService bgr() {
        return _bgr;
    }

    public static ExecutorService core() {
        return _cores;
    }

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
