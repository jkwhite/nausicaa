package org.excelsi.nausicaa.particle;


public class Universe {
    private final ParticleFactory _f;
    private final Particle[] _ps;
    private final double[][] _n;


    public Universe(int size, ParticleFactory f) {
        _f = f;
        _ps = new Particle[size];
        _n = new double[size][];
        bang();
    }

    public void bang() {
        for(int i=0;i<_ps.length;i++) {
            _ps[i] = _f.createParticle();
            _n[i] = new double[2];
        }
    }

    public void advance() {
        for(int i=0;i<_ps.length;i++) {
            double[] cur = _n[i];
            cur[0] = 0;
            cur[1] = 0;
            for(int j=0;j<_ps.length;j++) {
                if(i!=j) {
                    _ps[i].interact(_ps[j], cur);
                }
            }
        }
        for(int i=0;i<_ps.length;i++) {
            _ps[i].commit(_n[i]);
        }
    }
}
