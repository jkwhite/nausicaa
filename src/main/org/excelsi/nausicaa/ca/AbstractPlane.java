package org.excelsi.nausicaa.ca;


import com.google.gson.*;
import java.io.*;
import java.util.zip.*;


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

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.add("ca", creator().toJson());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(DataOutputStream dos = new DataOutputStream(bos)) {
            if(getDepth()==0) {
                for(int i=0;i<getWidth();i++) {
                    for(int j=0;j<getHeight();j++) {
                        dos.writeInt(getCell(i,j));
                    }
                }
            }
            else {
                for(int i=0;i<getWidth();i++) {
                    for(int j=0;j<getHeight();j++) {
                        for(int k=0;k<getDepth();k++) {
                            dos.writeInt(getCell(i,j,k));
                        }
                    }
                }
            }
            String data = Base64.encodeObject(bos.toByteArray(), Base64.GZIP | Base64.DONT_BREAK_LINES);
            o.addProperty("data", data);
            return o;
        }
        catch(IOException e) {
            throw new IllegalStateException("somehow got io error", e);
        }
    }

    @Override public void export(PrintWriter w) throws IOException {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
        gson.toJson(toJson(), w);
    }
}
