package org.excelsi.nausicaa.ca;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class Pools {
    private static final Logger LOG = LoggerFactory.getLogger(Pools.class);
    private static final Map<String,ExecutorService> _pools = new HashMap<>();

    private static final ExecutorService ADHOC = Executors.newFixedThreadPool(4, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            LOG.debug("spawning new thread for "+r);
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    public static ExecutorService adhoc() {
        return ADHOC;
    }

    public static int adhocSize() {
        return 1;
    }

    public static synchronized ExecutorService shared(final String name, final int defaultSize) {
        ExecutorService e = _pools.get(name);
        if(e==null) {
            e = named(name, defaultSize);
            _pools.put(name, e);
        }
        return e;
    }

    public static ExecutorService named(final String name, final int size) {
        return Executors.newFixedThreadPool(size, new ThreadFactory() {
            long n = 0;
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName(name+"-"+n);
                n++;
                return t;
            }
        });
    }
}
