package org.excelsi.nausicaa.ca;


import com.google.gson.*;
import java.io.*;
import java.util.zip.*;


public abstract class AbstractIntPlane extends AbstractPlane implements IntPlane {
    @Override public Values type() { return Values.discrete; }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.add("ca", creator().toJson());
        o.addProperty("type", "int");
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

    @Override public Pen pen() {
        return new Pen() {
            @Override public void setCell(int x, int y, int v) {
                AbstractIntPlane.this.setCell(x, y, v);
            }

            @Override public void setCell(int x, int y, int z, int v) {
                AbstractIntPlane.this.setCell(x, y, z, v);
            }

            @Override public void setCell(int x, int y, int z, double v) {
                AbstractIntPlane.this.setCell(x, y, z, (int) v);
            }

            @Override public void setRGBCell(int x, int y, int v) {
                AbstractIntPlane.this.setRGBCell(x, y, v);
            }
        };
    }

    private Probe _probe;
    @Override public Probe probe() {
        if(_probe==null) {
            _probe = new Probe() {
                final int max = creator().archetype().colors();

                @Override public double probe(int x, int y, int z) {
                    return AbstractIntPlane.this.getCell(x,y,z);
                }

                @Override public double probeNorm(int x, int y, int z) {
                    int val = AbstractIntPlane.this.getCell(x,y,z);
                    return (double)val/(double)max;
                }
            };
        }
        return _probe;
    }
}
