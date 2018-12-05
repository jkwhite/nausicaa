package org.excelsi.nausicaa.ca;


public interface Variables {
    Float weight();
    default boolean weightVaries() { return false; }
    default Float weight(Plane p, int x, int y, int z) { return weight(); }

    public static Variables constant(final Float weight) {
        return new Variables() {
            @Override public Float weight() {
                return weight;
            }
        };
    }

    public static Variables byPlane(Plane p) {
        final Probe probe = p.probe();
        return new Variables() {
            @Override public Float weight() { return null; }
            @Override public boolean weightVaries() { return true; }
            @Override public Float weight(Plane p, int x, int y, int z) {
                return probe.probeNorm(x,y,z);
            }
        };
    }

    public static Variables cascade(Variables... vars) {
        return new Variables() {
            @Override public Float weight() {
                for(Variables v:vars) {
                    if(v.weight()!=null) {
                        return v.weight();
                    }
                }
                return 1f;
            }

            @Override public boolean weightVaries() { return vars[0].weightVaries(); }

            @Override public Float weight(Plane p, int x, int y, int z) {
                for(Variables v:vars) {
                    Float w = v.weight(p, x, y, z);
                    if(w!=null) {
                        return w;
                    }
                }
                return 1f;
            }
        };
    }
}
