package org.excelsi.nausicaa.ca;


public interface Variables {
    Float weight();

    public static Variables constant(final Float weight) {
        return new Variables() {
            @Override public Float weight() {
                return weight;
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
        };
    }
}