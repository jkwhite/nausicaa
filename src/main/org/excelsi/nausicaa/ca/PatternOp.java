package org.excelsi.nausicaa.ca;


@FunctionalInterface
public interface PatternOp {
    int op(int b1, int b2);

    public static PatternOp eq() {
        return (b1,b2)->(b1==b2?1:0);
    }

    public static PatternOp neq() {
        return (b1,b2)->(b1==b2?0:1);
    }

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
        return (b1,b2)->(int)(b2==0?b1:b1/b2);
    }

    public static PatternOp mod() {
        return (b1,b2)->(int)(b2==0?b1:b1%b2);
    }

    public static PatternOp max() {
        return (b1,b2)->(int)(b1>b2?b1:b2);
    }

    public static PatternOp min() {
        return (b1,b2)->(int)(b1>b2?b2:b1);
    }

    public static PatternOp mino() {
        return (b1,b2)->(int)(b1>b2-1?b2-1:b1);
    }

    public static PatternOp pow() {
        return (b1,b2)->(int)(Math.pow(b1,b2));
    }

    public static PatternOp minusa() {
        return (b1,b2)->(int)(Math.abs(b1-b2));
    }

    public static PatternOp xor() {
        return (b1,b2)->(int)(b1^b2);
    }

    public static PatternOp and() {
        return (b1,b2)->(int)(b1&b2);
    }

    public static PatternOp or() {
        return (b1,b2)->(int)(b1|b2);
    }

    public static PatternOp rotr() {
        return (b1,b2)->(Integer.rotateRight(b1, b2));
    }

    public static PatternOp rotl() {
        return (b1,b2)->(Integer.rotateLeft(b1, b2));
    }

    public static PatternOp avg() {
        return (b1,b2)->(b1+b2)/2;
    }

    public static PatternOp first() {
        return (b1,b2)->(b1);
    }

    public static PatternOp last() {
        return (b1,b2)->(b2);
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
