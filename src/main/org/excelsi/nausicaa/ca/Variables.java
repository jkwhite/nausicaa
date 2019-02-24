package org.excelsi.nausicaa.ca;


public interface Variables {
    Double weight();
    default boolean weightVaries() { return false; }
    default Double weight(Plane p, int x, int y, int z) { return weight(); }
    default boolean update(Plane p, int x, int y, int z, float chance) { return true; }

    public static Variables constant(final Double weight) {
        return new Variables() {
            @Override public Double weight() {
                return weight;
            }
        };
    }

    public static Variables byPlane(Plane p) {
        final Probe probe = p.probe();
        return new Variables() {
            @Override public Double weight() { return null; }
            @Override public boolean weightVaries() { return true; }
            @Override public Double weight(Plane p, int x, int y, int z) {
                double w = probe.probeNorm(x,y,z);
                return w;
            }
        };
    }

    public static Variables cascade(Variables... vars) {
        return new Variables() {
            @Override public Double weight() {
                for(Variables v:vars) {
                    if(v.weight()!=null) {
                        return v.weight();
                    }
                }
                return 1d;
            }

            @Override public boolean weightVaries() { return vars[0].weightVaries(); }

            @Override public Double weight(Plane p, int x, int y, int z) {
                for(Variables v:vars) {
                    Double w = v.weight(p, x, y, z);
                    if(w!=null) {
                        return w;
                    }
                }
                return 1d;
            }

            @Override public boolean update(Plane p, int x, int y, int z, float chance) {
                return vars[0].update(p, x, y, z, chance);
            }
        };
    }
}
