package org.excelsi.nausicaa.ca;


public class IntBlockPlane2d extends IntBlockPlane {
    public IntBlockPlane2d(CA ca, int w, int h, int d, Palette p) {
        this(ca, w, h, d, p, new int[w*h*d]);
    }

    public IntBlockPlane2d(CA ca, int w, int h, int d, Palette p, int[] s) {
        super(ca, w, h, d, p, s);
    }

    public int[] getBlock(int[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        //System.err.println("x="+x+", y="+y+", z="+z+", dx="+dx+", dy="+dy+", dz="+dz);
        int idx=offset;
        for(int i=x;i<x+dx;i++) {
            for(int j=y;j<y+dy;j++) {
                //for(int k=z;k<z+dz;k++) {
                    //System.err.println(i+", "+j+", "+k+" @ "+idx);
                    into[idx++] = getCell(i,j,0);
                //}
            }
        }
        return into;
    }

    @Override public Plane copy() {
        int[] s = getBuffer();
        int[] sc = new int[s.length];
        System.arraycopy(s, 0, sc, 0, s.length);
        return new IntBlockPlane2d(creator(), getWidth(), getHeight(), getDepth(), getPalette(), sc);
    }
}
