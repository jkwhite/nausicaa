package org.excelsi.ca;


import java.util.ArrayList;
import java.util.List;
import javassist.*;
import java.util.Arrays;
import java.sql.Array;
import java.math.BigInteger;
import java.util.Iterator;


public class RuleSquare extends Rule1D {
    private CA _b;
    private boolean _wrap;


    public RuleSquare(Ruleset origin, int[] colors, int[][] patterns, int background) {
        super(origin, colors, patterns, background);
        _wrap = ((RulesetSquare)origin).getWrap();
    }

    public RuleSquare copy() {
        return new RuleSquare(origin(), colors(), toPattern(), background());
    }

    public int length() {
        return (int) Math.sqrt(super.length());
    }

    public boolean getWrap() {
        return _wrap;
    }

    public void init(CA c, Initialization in) {
        switch(in) {
            case single:
                for(int i=0;i<c.getWidth();i++) {
                    c.set(i, 0, _colors[0]);
                }
                break;
            case random:
                c.set(c.getWidth()/2, c.getHeight()/2, _colors[Rand.om.nextInt(_colors.length)]);
                break;
        }
    }

    public int getSuggestedInterval(CA c) {
        return Math.max(c.getHeight(), c.getWidth());
    }

    private void calc(CA b1, CA b2, int[] sur, int x, int y, int offset) {
        int idx = 0;
        int dx = x - offset;
        int dy = y - offset;
        for(int i=x-1;i<=x+1;i++) {
            for(int j=y-1;j<=y+1;j++) {
                sur[idx++] = get(b1, i, j);
            }
        }
        int n = selector().next(sur, 0);
        int existing = b2.get(x,y);
        if(n!=-1&&(existing==0||existing==_bgr)) {
            //b2.set(x, y, _colors[n]);
            set(b2, x, y, _colors[n]);
        }
        else {
            if(existing==0) {
                //b2.set(x, y, _bgr);
                set(b2, x, y, _bgr);
            }
        }
    }

    public void setOrigin(int ox, int oy) {
        _ox = ox;
        _oy = oy;
    }

    public int[] getLastDim() {
        return _lastDim;
    }

    /*
    private static int wrapx(CA c, int x) {
        while(x<0) x += b2.getWidth();
        x = x % c.getWidth();
        return x;
    }

    private static int wrapy(CA c, int y) {
        while(y<0) y += b2.getHeight();
        y = y % c.getHeight();
        return y;
    }
    */

    private int _ox = -1, _oy = -1;
    private int[] _lastDim = new int[4];
    //private CA _last;
    public float generate(CA b1, CA b2, int start, boolean stopOnSame, boolean over, Updater u) {
        //if(_last!=c) {
            //_ox = c.getWidth()/2;
            //_oy = c.getHeight()/2;
            //_ox = Rand.om.nextInt(c.getWidth());
            //_oy = Rand.om.nextInt(c.getHeight());
            //_last = c;
        //}
        int times = 1;
        int[] sur = new int[_len*_len];
        int dim = _len;
        int dist = start;
        int offset = (dim-1)/2;
        _lastDim[0] = _ox-dist;
        _lastDim[1] = _oy-dist;
        _lastDim[2] = _ox+dist;
        _lastDim[3] = _oy+dist;
        if(_wrap) {
            for(int x=_ox-dist;x<=_ox+dist;x++) {
                calc(b1, b2, sur, x, _oy-dist, offset);
                calc(b1, b2, sur, x, _oy+dist, offset);
            }
            for(int y=_oy-dist;y<=_oy+dist;y++) {
                calc(b1, b2, sur, _ox-dist, y, offset);
                calc(b1, b2, sur, _ox+dist, y, offset);
            }
        }
        else {
            for(int x=_ox-dist;x<=_ox+dist;x++) {
                if(x>=0&&x<b1.getWidth()) {
                    if(_oy-dist>=0) {
                        calc(b1, b2, sur, x, _oy-dist, offset);
                    }
                    if(_oy+start<b1.getHeight()) {
                        calc(b1, b2, sur, x, _oy+dist, offset);
                    }
                }
            }
            for(int y=_oy-dist;y<=_oy+dist;y++) {
                if(y>=0&&y<b1.getHeight()) {
                    if(_ox-dist>=0) {
                        calc(b1, b2, sur, _ox-dist, y, offset);
                    }
                    if(_ox+dist<b1.getWidth()) {
                        calc(b1, b2, sur, _ox+dist, y, offset);
                    }
                }
            }
        }
        return 0f;
    }
}
