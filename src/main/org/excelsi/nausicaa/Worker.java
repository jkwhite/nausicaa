package org.excelsi.nausicaa;


import java.util.*;


public class Worker extends Thread {
    private LinkedList<Runnable> _works = new LinkedList<Runnable>();
    private static Worker _w;


    public static synchronized Worker instance() {
        if(_w==null) {
            _w = new Worker();
            _w.setDaemon(true);
            _w.setName("Worker");
            _w.start();
        }
        return _w;
    }

    public void push(Runnable work) {
        synchronized(_works) {
            _works.add(work);
            _works.notify();
        }
    }

    public void run() {
        while(true) {
            Runnable r = null;
            synchronized(_works) {
                if(_works.isEmpty()) {
                    try {
                        _works.wait();
                    }
                    catch(InterruptedException e) {
                    }
                }
                if(!_works.isEmpty()) {
                    r = _works.remove(0);
                }
            }
            if(r!=null) {
                try {
                    r.run();
                }
                catch(Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
