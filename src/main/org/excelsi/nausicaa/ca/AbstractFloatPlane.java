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
            private final int[] _rgb = new int[4];
            private final float _pal = creator().getPalette().getColorCount()-1f;
            private final float _mul = (creator().archetype().colors() - 1)/255f;


            @Override public void setCell(int x, int y, int v) {
                AbstractFloatPlane.this.setCell(x, y, v/_pal);
            }

            @Override public void setCell(int x, int y, int z, int v) {
                AbstractFloatPlane.this.setCell(x, y, z, v/_pal);
            }

            @Override public void setCell(int x, int y, int z, float v) {
                AbstractFloatPlane.this.setCell(x, y, z, v);
            }

            @Override public void setRGBCell(int x, int y, int v) {
                Colors.unpack(v, _rgb);
                //Colors.dump(v, "with mx "+mx);
                if(getDepth()==3) {
                    AbstractFloatPlane.this.setCell(x, y, 0, _rgb[0]*_mul);
                    AbstractFloatPlane.this.setCell(x, y, 1, _rgb[1]*_mul);
                    AbstractFloatPlane.this.setCell(x, y, 2, _rgb[2]*_mul);
                }
                else if(getDepth()==4) {
                    AbstractFloatPlane.this.setCell(x, y, 0, _rgb[0]*_mul);
                    AbstractFloatPlane.this.setCell(x, y, 1, _rgb[1]*_mul);
                    AbstractFloatPlane.this.setCell(x, y, 2, _rgb[2]*_mul);
                    AbstractFloatPlane.this.setCell(x, y, 3, _rgb[3]*_mul);
                }
                //else {
                    //throw new IllegalStateException("unsupported depth "+getDepth());
                //}
            }
        };
    }

    private Probe _probe;
    @Override public Probe probe() {
        if(_probe==null) {
            _probe = new Probe() {
                final float max = creator().archetype().colors();

                @Override public float probe(int x, int y, int z) {
                    return AbstractFloatPlane.this.getCell(x,y,z);
                }

                @Override public float probeNorm(int x, int y, int z) {
                    float val = AbstractFloatPlane.this.getCell(x,y,z);
                    return val/max;
                }
            };
        }
        return _probe;
    }
}
