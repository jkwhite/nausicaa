package org.excelsi.nausicaa.ca;


public class Multistats implements Humanizable {
    private final Stats[] _s;
    private final double[] _diffs;


    public Multistats(final Stats... s) {
        _s = s;
        _diffs = new double[s.length-1];
        for(int i=1;i<s.length;i++) {
            _diffs[i-1] = Stats.similarity(s[i-1].plane(), s[i].plane());
        }
    }

    public double[] getPsim() {
        return _diffs;
    }

    public double getPsimmean() {
        return Stats.mean(_diffs);
    }

    public double getPsimsdev() {
        return Stats.sdev(_diffs);
    }

    public double[] psim() {
        return _diffs;
    }

    @Override public String humanize() {
        StringBuilder b = new StringBuilder();
        for(int i=0;i<_s.length;i++) {
            b.append(_s[i].humanize()).append("\n");
        }
        b.append("psim ").append(Stats.format(_diffs));
        return b.toString();
    }
}
