package org.excelsi.nausicaa.ca;


import java.util.Arrays;


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

    public static Fitness interesting() {
        return (a, ps)->{
            final Plane p1 = ps[ps.length-3];
            final Plane p2 = ps[ps.length-1];
            final Stats s1 = Stats.forPlane(p1);
            final Stats s2 = Stats.forPlane(p2);
            //System.err.println("p1: "+System.identityHashCode(p1)+", p2: "+System.identityHashCode(p2)+", s1: "+s1.humanize()+", s2: "+s2.humanize());
            //System.err.println("histo: "+Arrays.toString(s2.histo)+", runs: "+Arrays.toString(s2.maxruns));

            double hs = Stats.sdev(s2.getHisto());
            //System.err.println("sdev: "+hs);
            double hval = Stats.ideal(10000, hs)/10000d;
            double runval = Stats.ideal(5, Stats.sdev(s2.getMaxruns()));
            double dval = Stats.ideal(0.002, Stats.diff(p1, p2));
            return hval + runval + dval;
        };
    }

    public static Fitness interesting2() {
        return (a, ps)->{
            final Plane p1 = ps[ps.length-3];
            final Plane p2 = ps[ps.length-2];
            final Stats s1 = Stats.forPlane(p1);
            final Stats s2 = Stats.forPlane(p2);
            final Multistats ms = s2.compareWith(s1);
            //System.err.println("p1: "+System.identityHashCode(p1)+", p2: "+System.identityHashCode(p2)+", ms: "+ms.humanize());
            //System.err.println("histo: "+Arrays.toString(s2.histo)+", runs: "+Arrays.toString(s2.maxruns));

            double pm = ms.getPsimmean();
            double v = 3d * Stats.ideal(0.90, pm);
            //System.err.println("psim: "+Stats.format(ms.getPsim())+", psimm: "+pm+", v: "+v);
            return v + Stats.ideal(0.20, s2.getNrsdev());
        };
    }

    /*
    public static double ideal(double i, double v) {
        return Math.abs(i-v);
    }

    public static double sdev(int[] v) {
        return Math.sqrt(var(v));
    }

    public static double var(int[] v) {
        double m = 0;
        for(int i=0;i<v.length;i++) {
            m += v[i];
        }
        m /= v.length;
        double var = 0;
        for(int i=0;i<v.length;i++) {
            double diff = v[i] - m;
            diff *= diff;
            var += diff;
        }
        return var;
    }

    public static int delta(int[] v1, int[] v2) {
        int d = 0;
        for(int i=0;i<v1.length;i++) {
            d += Math.abs(v2[i] - v1[i]);
        }
        return d;
    }

    public static double diff(Plane p1, Plane p2) {
        final int[] r1 = new int[p1.getWidth()];
        final int[] r2 = new int[p2.getWidth()];
        int diff = 0;
        for(int y=0;y<p1.getHeight();y++) {
            p1.getRow(r1, y, 0);
            p2.getRow(r2, y, 0);
            for(int j=0;j<r1.length;j++) {
                if(r1[j]!=r2[j]) {
                    diff++;
                }
                //diff += Math.abs(r2[j] - r1[j]);
            }
        }
        return ((double)diff)/(p1.getHeight()*p1.getWidth());
    }

    public static Stats stats(final Archetype a, final Plane p) {
        final int[] histo = new int[a.colors()];
        final int[] maxruns = new int[a.colors()];
        final int[] row = new int[p.getWidth()];
        int lastColor = 0;
        int maxrun = 0;
        for(int y=0;y<p.getHeight();y++) {
            p.getRow(row, y, 0);
            for(int i=0;i<row.length;i++) {
                histo[row[i]]++;
                if(row[i]==lastColor) {
                    maxrun++;
                }
                else {
                    if(maxruns[lastColor]<maxrun) {
                        maxruns[lastColor] = maxrun;
                    }
                    maxrun = 0;
                    lastColor = row[i];
                }
            }
        }
        return new Stats(histo, maxruns);
    }

    public static class Stats {
        public final int[] histo;
        public final int[] maxruns;

        public Stats(int[] histo, int[] maxruns) {
            this.histo = histo;
            this.maxruns = maxruns;
        }
    }
    */

    private FitnessCriteria() {}
}
