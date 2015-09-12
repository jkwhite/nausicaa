package org.excelsi.ca;


import javax.swing.JFrame;
import java.io.IOException;


public class TestRule extends junit.framework.TestCase {
    public void testCompile() throws Exception {
        final int w = pack(255,255,255,255);
        final int b = pack(0,0,0,255);

        Rule r = new Rule(new int[]{b,w}, new int[][]{
            new int[]{w,w,w,b},
            new int[]{w,w,b,w},
            new int[]{b,w,w,w},
            new int[]{b,w,b,w},
            new int[]{b,b,w,w},
            new int[]{w,b,b,w},
            new int[]{b,b,b,b},
            new int[]{w,b,w,w}
        }, w);

        int wid = 1500;
        CA c = new CA(wid, 1000);
        c.initPlain(w);
        c.set(wid/2, 0, b);
        //c.initRandom(new int[]{b,w});
        r.generate(c, 999);
        //c.save("/tmp/ca.png");
        //c.display();
        //System.in.read();
    }

    public void testEquals() {
        final int w = pack(255,255,255,255);
        final int b = pack(0,0,0,255);

        Rule r = new Rule(new int[]{b,w}, new int[][]{
            new int[]{w,w,w,b},
            new int[]{w,w,b,w},
            new int[]{b,w,w,w},
            new int[]{b,w,b,w},
            new int[]{b,b,w,w},
            new int[]{w,b,b,w},
            new int[]{b,b,b,b},
            new int[]{w,b,w,w}
        }, w);

        String s1 = r.toString();
        Rule r2 = Rule.fromString(new int[]{b,w}, b, 3, s1);
        assertEquals("lost in translation", s1, r2.toString());
    }

    private static int pack(int r, int g, int b, int a) {
        return a+(r<<8)+(g<<16)+(b<<24);
    }
}
