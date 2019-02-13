package org.excelsi.nausicaa.ca;


public interface Rogers {
    int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int k, int offset);
    float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int k, int offset);
    int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int offset);
    float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int offset);

    public static Rogers forPattern(final Pattern p) {
        final Archetype a = p.archetype();
        final int size = a.size();
        final int d = size*2+1;
        if(!p.usesSource()) {
            return new Rogers() {
                @Override public int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int k, int offset) {
                    return into;
                }

                @Override public float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int k, int offset) {
                    return into;
                }

                @Override public int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int offset) {
                    return into;
                }

                @Override public float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int offset) {
                    return into;
                }
            };
        }
        switch(a.neighborhood()) {
            default:
            case moore:
                return new Rogers() {
                    @Override public int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int k, int offset) {
                        return p.getBlock(into, i-size, j-size, k-size, /*dx*/ d, /*dy*/ d, /*dz*/ d, offset);
                    }

                    @Override public float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int k, int offset) {
                        return p.getBlock(into, i-size, j-size, k-size, /*dx*/ d, /*dy*/ d, /*dz*/ d, offset);
                    }

                    @Override public int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int offset) {
                        return p.getBlock(into, i-size, j-size, /*dx*/ d, /*dy*/ d, offset);
                    }

                    @Override public float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int offset) {
                        return p.getBlock(into, i-size, j-size, /*dx*/ d, /*dy*/ d, offset);
                    }
                };
            case vonneumann:
                return new Rogers() {
                    @Override public int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int k, int offset) {
                        return p.getCardinal(into, i, j, k, size, size, size, offset);
                    }

                    @Override public float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int k, int offset) {
                        return p.getCardinal(into, i, j, k, size, size, size, offset);
                    }

                    @Override public int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int offset) {
                        return p.getCardinal(into, i, j, size, size, offset);
                    }

                    @Override public float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int offset) {
                        return p.getCardinal(into, i, j, size, size, offset);
                    }
                };
            case circular:
                return new Rogers() {
                    final int[][] coords = Archetype.circularCoords(a.dims(), a.size());
                    @Override public int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int k, int offset) {
                        // TODO
                        return p.getCoords(into, i, j, coords, offset);
                    }

                    @Override public float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int k, int offset) {
                        return p.getCoords(into, i, j, coords, offset);
                    }

                    @Override public int[] getNeighborhood(IntPlane p, int[] into, int i, int j, int offset) {
                        return p.getCoords(into, i, j, coords, offset);
                    }

                    @Override public float[] getNeighborhood(FloatPlane p, float[] into, int i, int j, int offset) {
                        // TODO
                        return p.getCoords(into, i, j, coords, offset);
                    }
                };
        }
    }
}
