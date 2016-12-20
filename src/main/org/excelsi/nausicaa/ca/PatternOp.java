package org.excelsi.nausicaa.ca;


@FunctionalInterface
public interface PatternOp {
    int op(int b1, int b2);

    public static PatternOp plus() {
        return (b1,b2)->(int)(b1+b2);
    }

    public static PatternOp minus() {
        return (b1,b2)->(int)(b1-b2);
    }

    public static PatternOp multiply() {
        return (b1,b2)->(int)(b1*b2);
    }

    public static PatternOp divide() {
        return (b1,b2)->(int)(b1/b2);
    }

    public static PatternOp mod() {
        return (b1,b2)->(int)(b1%b2);
    }

    public static int sum(final byte[] p) {
        int t=0;
        for(int i=0;i<p.length;i++) {
            t+=p[i];
        }
        return t;
    }

    public static int sumo(final byte[] p) {
        int t=0;
        for(int i=0;i<p.length;i++) {
            t+=p[i];
        }
        t-=p[(p.length-1)/2];
        return t;
    }
}
