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


    public static ExecutorService bgr() {
        return _bgr;
    }
}
