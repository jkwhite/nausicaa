package org.excelsi.ca;


import java.util.*;


public class MultiruleSquare extends Multirule1D {
    public MultiruleSquare(Ruleset origin, Rule... rules) {
        super(origin, rules);
    }

    public void init(CA c, Initialization in) {
        int bgr = background();
        for(int i=0;i<c.getWidth();i++) {
            for(int j=0;j<c.getHeight();j++) {
                c.set(i, j, bgr);
            }
        }
            //switch(in) {
                //case single:
                    //for(int i=0;i<c.getWidth();i++) {
                        //for(int j=0;j<c.getHeight();j++) {
                            //c.set(i, j, bgr);
                        //}
                    //}
                    //break;
                //case random:
                    //for(int i=0;i<c.getWidth();i++) {
                        //for(int j=0;j<c.getHeight();j++) {
                            //c.set(i, j, _colors[Rand.om.nextInt(_colors.length)]);
                        //}
                    //}
                    //break;
            //}
        for(Rule r:rules()) {
            RuleSquare s = (RuleSquare) r;
            int ox = 1+Rand.om.nextInt(c.getWidth()-2);
            int oy = 1+Rand.om.nextInt(c.getHeight()-2);
            s.setOrigin(ox, oy);
            //if(in==Initialization.single) {
                c.set(ox, oy, s.colors()[1]);
            //}
            switch(in) {
                case single:
                    for(int i=ox-1;i<=ox+1;i++) {
                        for(int j=oy-1;j<=oy+1;j++) {
                            c.set(i, j, bgr);
                        }
                    }
                    break;
                case random:
                    int[] colors = r.colors();
                    for(int i=ox-1;i<=ox+1;i++) {
                        for(int j=oy-1;j<=oy+1;j++) {
                            c.set(i, j, colors[Rand.om.nextInt(colors.length)]);
                        }
                    }
                    //for(int i=0;i<c.getWidth();i++) {
                        //for(int j=0;j<c.getHeight();j++) {
                            //c.set(i, j, _colors[Rand.om.nextInt(_colors.length)]);
                        //}
                    //}
                    break;
            }
            if(in==Initialization.single) {
                c.set(ox, oy, s.colors()[1]);
            }
        }
    }

    private CA _b;
    public float generate(CA c, int start, int end, boolean stopOnSame, boolean over, Updater u) {
        if(_b==null||_b.getWidth()!=c.getWidth()||_b.getHeight()!=c.getHeight()) {
            _b = new CA(c.getWidth(), c.getHeight());
        }
        //if(true) return 0f;
        float diff = 0f;
        CA b1 = c, b2 = _b; // double buffer
        b2.setData(b1.getData());
        Rule[] rules = rules();
        for(int i=start;i<end;i++) {
            System.err.println( "starting "+i);
            //b2.setData(b1.getData());

            for(Rule r:rules) {
                RuleSquare r2 = (RuleSquare) r;
                diff += r2.generate(b1, b2, i, stopOnSame, true, null);
            }
            if(u!=null&&i%10==0) {
                u.update(this, start, i, end);
            }
            if(Thread.currentThread().isInterrupted()) {
                return diff;
            }
            //System.err.println();

            // swap
            for(Rule r:rules) {
                int[] d = ((RuleSquare)r).getLastDim();
                if(((RuleSquare)r).getWrap()) {
                    for(int x=d[0];x<=d[2];x++) {
                        b1.set(x, d[1], b2.get(x, d[1]));
                        b1.set(x, d[3], b2.get(x, d[3]));
                    }
                    for(int y=d[1];y<=d[3];y++) {
                        b1.set(d[0], y, b2.get(d[0], y));
                        b1.set(d[2], y, b2.get(d[2], y));
                    }
                }
                else {
                    for(int x=d[0];x<=d[2];x++) {
                        if(x>=0&&x<b2.getWidth()) {
                            if(d[1]>=0) {
                                b1.set(x, d[1], b2.get(x, d[1]));
                            }
                            if(d[3]<b2.getHeight()) {
                                b1.set(x, d[3], b2.get(x, d[3]));
                            }
                        }
                    }
                    for(int y=d[1];y<=d[3];y++) {
                        if(y>=0&&y<b2.getHeight()) {
                            if(d[0]>=0) {
                                b1.set(d[0], y, b2.get(d[0], y));
                            }
                            if(d[2]<b2.getWidth()) {
                                b1.set(d[2], y, b2.get(d[2], y));
                            }
                        }
                    }
                }
            }
            CA temp = b1;
            b1 = b2;
            b2 = temp;
            //for(int x=0;x<b2.getWidth();x++) {
                //for(int y=0;y<b2.getHeight();y++) {
                    //b2.set(x, y, _bgr);
                //}
            //}
        }
        c.setData(b1.getData());
        c.setGoodness(diff);
        return diff;
    }
}
