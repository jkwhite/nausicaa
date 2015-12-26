package org.excelsi.nausicaa.ca;


public final class FitnessCriteria {
    public static Fitness neverending() {
        return (a, p)->{
            if(a.dims()==1) {
                if(p.getHeight()<2) {
                    return 0f;
                }
                int[] row1 = new int[p.getWidth()];
                int[] row2 = new int[p.getWidth()];
                p.getRow(row1, p.getHeight()-2, 0);
                p.getRow(row2, p.getHeight()-1, 0);
                int delta1 = 0;
                int delta2 = 0;
                for(int i=0;i<row1.length;i++) {
                    delta1 += Math.abs(row2[i]-row1[i]);
                    delta2 += Math.abs(row1[i]-row1[(i+1)%row1.length]);
                    delta2 += Math.abs(row2[i]-row2[(i+1)%row2.length]);
                }
                //System.err.println("delta1: "+delta1+", delta2: "+delta2);
                final float maxdiff1 = row1.length*(a.colors()-1);
                final float maxdiff2 = row1.length*(a.colors()-1);
                final float d1 = delta1 / maxdiff1;
                final float d2 = delta2 / maxdiff2;
                System.err.println("d1: "+d1+", d2: "+d2);
                return (1f-Math.abs(1f-2*d1))
                    * (1f-Math.abs(1f-2*d2));
                //return (maxdiff1 - Math.abs(maxdiff1/2-d1)) + (maxdiff2 - Math.abs(maxdiff2/2-d2));
                //return (delta1 / (float) row1.length + delta2 / (float) row1.length) / 2f;
            }
            else {
                return 0f;
            }
        };
    }

    public static Fitness repeatGreatest() {
        return (a, p)->{
            if(a.dims()==1) {
                int[] row0 = new int[p.getWidth()];
                p.getRow(row0, 0, 0);
                int[] rowi = new int[p.getWidth()];
                //for(int i=p.getHeight()-1;i>0;i--) {
                //}
                p.getRow(rowi, p.getHeight()-1, 0);
                int matches = 0;
                for(int i=0;i<rowi.length;i++) {
                    if(row0[i]==rowi[i]) {
                        matches++;
                    }
                }
                final float d1 = matches / (float) row0.length;
                return d1;
            }
            else {
                return 0f;
            }
        };
    }

    public static Fitness findTarget(final byte[] target) {
        return (a, p)->{
            if(a.dims()==1) {
                //int[] row0 = new int[p.getWidth()];
                //int[] row0 = target;
                //p.getRow(row0, 0, 0);
                int[] rowi = new int[p.getWidth()];
                //for(int i=p.getHeight()-1;i>0;i--) {
                //}
                p.getRow(rowi, p.getHeight()-1, 0);
                int matches = 0;
                for(int i=0;i<target.length;i++) {
                    if(target[i]==rowi[i]) {
                        matches++;
                    }
                }
                final float d1 = matches / (float) target.length;
                return d1;
            }
            else {
                return 0f;
            }
        };
    }

    private FitnessCriteria() {}
}
