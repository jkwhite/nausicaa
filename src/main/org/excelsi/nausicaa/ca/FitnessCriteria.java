package org.excelsi.nausicaa.ca;


public final class FitnessCriteria {
    public static Fitness neverending() {
        return (a, ps)->{
            if(a.dims()==1) {
                final Plane p = ps[0];
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
                final double maxdiff1 = row1.length*(a.colors()-1);
                final double maxdiff2 = row1.length*(a.colors()-1);
                final double d1 = delta1 / maxdiff1;
                final double d2 = delta2 / maxdiff2;
                System.err.println("d1: "+d1+", d2: "+d2);
                return (1f-Math.abs(1f-2*d1))
                    * (1f-Math.abs(1f-2*d2));
                //return (maxdiff1 - Math.abs(maxdiff1/2-d1)) + (maxdiff2 - Math.abs(maxdiff2/2-d2));
                //return (delta1 / (double) row1.length + delta2 / (double) row1.length) / 2f;
            }
            else {
                return 0f;
            }
        };
    }

    public static Fitness repeatGreatest() {
        return (a, ps)->{
            if(a.dims()==1) {
                final Plane p = ps[0];
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
                final double d1 = matches / (double) row0.length;
                return d1;
            }
            else {
                return 0f;
            }
        };
    }

    public static Fitness reverse(final int alen, final int length) {
        return (a, ps)->{
            if(a.dims()==1 || a.dims()==2) {
                final Plane p = ps[0];
                final Plane p2 = a.dims()==1?ps[0]:ps[ps.length-1];
                int[] row0 = new int[p.getWidth()];
                //int[] row0 = target;
                p.getRow(row0, 0, 0);
                int[] target = new int[p.getWidth()];
                //for(int i=p.getHeight()-1;i>0;i--) {
                //}
                int p2row = a.dims()==1?p.getHeight()-1:0;
                p2.getRow(target, p2row, 0);
                double matches = 0;
                for(int i=0;i<length;i++) {
                    final int nidx = length-i-1;
                    if(row0[i]==target[nidx]) {
                        matches += 1;
                    }
                    else {
                        int lidx = nidx==0?length-1:nidx-1;
                        int uidx = nidx==length-1?0:nidx+1;
                        if(row0[i]==target[lidx]) {
                            matches += 0.2;
                        }
                        if(row0[i]==target[uidx]) {
                            matches += 0.2;
                        }
                    }
                }
                final double d1 = matches / (double) length;
                final int da = alen - a.colors();
                return da>0 ? d1/da : d1;
            }
            else {
                return 0f;
            }
        };
    }

    public static Fitness findTarget(final byte[] target) {
        return (a, ps)->{
            if(a.dims()==1) {
                final Plane p = ps[0];
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
                final double d1 = matches / (double) target.length;
                return d1;
            }
            else {
                return 0f;
            }
        };
    }

    public static Fitness findTarget(final Plane target, final double[] weights) {
        final int[][] t = new int[target.getHeight()][target.getWidth()];
        final double maxMatches = target.getHeight() * target.getWidth();
        for(int i=0;i<t.length;i++) {
            target.getRow(t[i], i, 0);
        }
        return (a, ps)->{
            if(a.dims()==1||a.dims()==2) {
                final Plane p = ps[ps.length-1];
                final int[] row = new int[p.getWidth()];
                double matches = 0;
                for(int i=0;i<t.length;i++) {
                    p.getRow(row, i, 0);
                    for(int j=0;j<row.length;j++) {
                        if(t[i][j]==row[j]) {
                            double w = row[j] < weights.length ? weights[row[j]] : 1;
                            matches += w;
                        }
                    }
                }
                //System.err.println("matches: "+matches+", max: "+maxMatches);
                final double d1 = matches / maxMatches;
                return d1;
            }
            else {
                return 0f;
            }
        };
    }

    public static Fitness nothingLostNothingGained() {
        return (a, ps)->{
            final int[][] ccounts = new int[ps.length][a.colors()];
            final int[] row = new int[ps[0].getWidth()];
            for(int i=0;i<ps.length;i++) {
                final Plane p = ps[i];
                for(int y=0;y<p.getHeight();y++) {
                    p.getRow(row, y, 0);
                    for(int j=0;j<row.length;j++) {
                        ccounts[i][row[j]]++;
                    }
                }
            }
            //double maxdelta = a.colors() * ps.length * a.colors() * row.length * ps[0].getHeight();
            int totaldelta = 0;
            double maxdelta = 0;
            for(int i=0;i<ccounts.length;i++) {
                for(int j=1;j<ccounts[i].length;j++) {
                    totaldelta += Math.abs(ccounts[i][j] - ccounts[i][j-1]);
                    maxdelta += row.length * ps[0].getHeight();
                }
            }
            return 1d - totaldelta / maxdelta;
        };
    }

    private FitnessCriteria() {}
}
