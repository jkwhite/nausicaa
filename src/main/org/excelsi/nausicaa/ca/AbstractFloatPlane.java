package org.excelsi.nausicaa.ca;


import com.google.gson.*;
import java.io.*;
import java.util.zip.*;


public abstract class AbstractFloatPlane extends AbstractPlane implements FloatPlane {
    @Override public Values type() { return Values.continuous; }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.add("ca", creator().toJson());
        o.addProperty("type", "float");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(DataOutputStream dos = new DataOutputStream(bos)) {
            if(getDepth()==0) {
                for(int i=0;i<getWidth();i++) {
                    for(int j=0;j<getHeight();j++) {
                        dos.writeFloat(getCell(i,j));
                    }
                }
            }
            else {
                for(int i=0;i<getWidth();i++) {
                    for(int j=0;j<getHeight();j++) {
                        for(int k=0;k<getDepth();k++) {
                            dos.writeFloat(getCell(i,j,k));
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

    @Override public Pen pen() {
        return new Pen() {
            @Override public void setCell(int x, int y, int v) {
                setCell(x, y, v);
            }

            @Override public void setCell(int x, int y, int z, int v) {
                setCell(x, y, z, v);
            }

            @Override public void setCell(int x, int y, int z, float v) {
                setCell(x, y, z, v);
            }
        };
    }
}
