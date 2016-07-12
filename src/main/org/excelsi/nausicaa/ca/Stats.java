package org.excelsi.nausicaa.ca;


import java.util.Arrays;


public class Stats implements Humanizable {
    private final Plane _p;
    private final int[] _histo;
    private final double[] _nhisto;
    private final int[] _maxruns;
    private final double[] _nmaxruns;


    public int[] getHisto() {
        return _histo;
    }

    public double[] getNhisto() {
        return _nhisto;
    }

    public int[] getMaxruns() {
        return _maxruns;
    }

    public double getHmean() {
        return mean(_histo);
    }

    public double getRmean() {
        return mean(_maxruns);
    }

    public double getNrmean() {
        return mean(_nmaxruns);
    }

    public double getHsdev() {
        return sdev(_histo);
    }

    public double getRsdev() {
        return sdev(_maxruns);
    }

    public double getNrsdev() {
        return sdev(_nmaxruns);
    }

    public int[] histo() {
        return _histo;
    }

    public double[] nhisto() {
        return _nhisto;
    }

    public int[] maxruns() {
        return _maxruns;
    }

    public Plane plane() {
        return _p;
    }

    public Multistats compareWith(final Stats o) {
        return new Multistats(this, o);
    }

    @Override public String humanize() {
        return String.format("h %s nh %s r %s hmean %.2g hsdev %.2g nhmean %.2g nhsdev %.2g rmean %.2g rsdev %.2g nrmean %.2g nrsdev %.2g",
            Arrays.toString(_histo),
            format(_nhisto),
            Arrays.toString(_maxruns),
            mean(_histo),
            sdev(_histo),
            mean(_nhisto),
            sdev(_nhisto),
            mean(_maxruns),
            sdev(_maxruns),
            mean(_nmaxruns),
            sdev(_nmaxruns)
        );
    }

    public static String format(double[] v) {
        StringBuilder b = new StringBuilder("[");
        for(int i=0;i<v.length;i++) {
            b.append(String.format("%.2g", v[i]));
            if(i<v.length-1) {
                b.append(", ");
            }
        }
        return b.append("]").toString();
    }

    public static Stats forPlane(final Plane p) {
        return forPlane(p.creator(), p);
    }

    public static Stats forPlane(final CA c, final Plane p) {
        return new Stats(c, p);
    }

    public static double ideal(double i, double v) {
        return Math.abs(i-v);
    }

    public static double mean(int[] v) {
        double m = 0;
        for(int i=0;i<v.length;i++) {
            m += v[i];
        }
        return m/v.length;
    }

    public static double mean(double[] v) {
        double m = 0;
        for(int i=0;i<v.length;i++) {
            m += v[i];
        }
        return m/v.length;
    }

    public static int p100(int[] v) {
        int m = v[0];
        for(int i=0;i<v.length;i++) {
            if(v[i] > m) {
                m = v[i];
            }
        }
        return m;
    }

    public static int p0(int[] v) {
        int m = v[0];
        for(int i=0;i<v.length;i++) {
            if(v[i] < m) {
                m = v[i];
            }
        }
        return m;
    }

    public static double sdev(int[] v) {
        return Math.sqrt(var(v));
    }

    public static double sdev(double[] v) {
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

    public static double var(double[] v) {
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

    public static double similarity(Plane p1, Plane p2) {
        return 1d-diff(p1, p2);
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

    private Stats(final CA c, final Plane p) {
        final Archetype a = c.getRule().archetype();
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

        double[] nhisto = new double[histo.length];
        for(int i=0;i<nhisto.length;i++) {
            nhisto[i] = ((double)histo[i]) / (p.getWidth() * p.getHeight());
        }
        double[] nmaxruns = new double[maxruns.length];
        for(int i=0;i<nmaxruns.length;i++) {
            nmaxruns[i] = ((double)maxruns[i]) / p.getWidth();
        }
        _histo = histo;
        _nhisto = nhisto;
        _maxruns = maxruns;
        _nmaxruns = nmaxruns;
        _p = p;
    }
}
