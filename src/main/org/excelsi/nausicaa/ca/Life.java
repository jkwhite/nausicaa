package org.excelsi.nausicaa.ca;


public class Life extends AbstractMutator {
    private final int _ub;
    private final int _lb;


    public Life() {
        this(2, 3);
    }

    public Life(int cols) {
        this((int)Math.cbrt(cols), (int)Math.sqrt(1+cols));
    }

    public Life(int lb, int ub) {
        _lb = lb;
        _ub = ub;
    }

    public String name() { return "Life"; }
    public String description() { return "Conway's Life"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r) throws MutationFailedException {
        if(r.getPattern().archetype().dims()!=2) {
            throw new MutationFailedException("life must be 2d");
        }
        final byte[] base = new byte[r.getPattern().archetype().sourceLength()];
        final int max = (r.getPattern().archetype().colors()-1)*8;
        //final int ub = (int) Math.sqrt(1+max);
        //final int lb = (int) Math.cbrt(max);
        final int ub = _ub;
        final int lb = _lb;
        return r.derive(r.getPattern().transform((arch, target)->{
            for(int i=0;i<target.length;i++) {
                Patterns.expandSourceIndex(arch, i, base);
                int sum = 0;
                byte t;
                for(int j=0;j<base.length;j++) {
                    if(j==4) {
                        continue;
                    }
                    //sum += base[j];
                    sum += base[j]==0?0:1;
                }
                if(base[4]==0) {
                    t = sum>lb&&sum<=ub?nonbgr(arch):0;
                }
                else {
                    t = sum<lb ? 0
                        :sum>ub ? 0
                        :nonbgr(arch);
                }
                target[i] = t;
            }
        }));
    }

    private byte nonbgr(Archetype arch) {
        if(arch.colors()==2) {
            return 1;
        }
        else {
            return (byte) (_om.nextInt(arch.colors()-1)+1);
        }
    }
}
