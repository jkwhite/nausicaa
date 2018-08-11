package org.excelsi.nausicaa.ca;


import com.google.gson.*;
import java.io.*;


public abstract class AbstractPlane implements Plane {
    private final Object LOCK = new Object();
    private final Object UNLOCK = new Object();

    private volatile short _readLock = 0;
    private volatile short _writeLock = 0;


    @Override public void lockRead() {
        do {
            while(_writeLock>0);
            synchronized(LOCK) {
                if(_writeLock==0) {
                    _readLock++;
                }
            }
        } while(_writeLock>0);
    }

    @Override public void unlockRead() {
        synchronized(LOCK) {
            _readLock--;
        }
    }

    @Override public void lockWrite() {
        do {
            while(_readLock>0);
            synchronized(LOCK) {
                if(_readLock==0) {
                    _writeLock++;
                }
            }
        } while(_readLock>0);
    }

    @Override public void unlockWrite() {
        synchronized(LOCK) {
            _writeLock--;
        }
    }

    @Override public void export(PrintWriter w) throws IOException {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
        gson.toJson(toJson(), w);
    }
}
