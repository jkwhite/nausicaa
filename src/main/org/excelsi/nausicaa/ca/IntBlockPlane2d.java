package org.excelsi.nausicaa.ca;


public class IntBlockPlane2d extends IntBlockPlane {
    public IntBlockPlane2d(CA ca, int w, int h, int d, Palette p, Integer oob) {
        this(ca, w, h, d, p, oob, new int[w*h*d]);
    }

    public IntBlockPlane2d(CA ca, int w, int h, int d, Palette p, Integer oob, int[] s) {
        super(ca, w, h, d, p, oob, s);
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

    @Override public int[] getCardinal(int[] into, int x, int y, int dx, int dy, int offset) {
        into[offset++] = getCell(x+1,y,0);
        into[offset++] = getCell(x,y-1,0);
        into[offset++] = getCell(x,y,0);
        into[offset++] = getCell(x,y+1,0);
        into[offset++] = getCell(x-1,y,0);
        return into;
    }

    @Override public int[] getCardinal(int[] into, int x, int y, int z, int dx, int dy, int dz, int offset) {
        into[offset++] = getCell(x+1,y,0);
        into[offset++] = getCell(x,y-1,0);
        into[offset++] = getCell(x,y,0);
        into[offset++] = getCell(x,y+1,0);
        into[offset++] = getCell(x-1,y,0);
        return into;
    }

    @Override public Plane copy() {
        int[] s = getBuffer();
        int[] sc = new int[s.length];
        System.arraycopy(s, 0, sc, 0, s.length);
        return new IntBlockPlane2d(creator(), getWidth(), getHeight(), getDepth(), getPalette(), oob(), sc);
    }

    @Override public Plane withDepth(int d) {
        IntBlockPlane p = new IntBlockPlane(creator(), getWidth(), getHeight(), d, getPalette(), oob());
        for(int i=0;i<getWidth();i++) {
            for(int j=0;j<getHeight();j++) {
                p.setCell(i, j, 0, getCell(i, j, 0));
            }
        }
        return p;
    }
}
