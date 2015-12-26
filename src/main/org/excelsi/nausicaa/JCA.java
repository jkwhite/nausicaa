package org.excelsi.nausicaa;


import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;


public class JCA extends JComponent {
    private final int _w;
    private final int _h;
    private Image _i;


    public JCA(int w, int h) {
        _w = w;
        _h = h;
    }

    //public int getWidth() {
        //return _w;
    //}
//
    //public int getHeight() {
        //return _h;
    //}
//
    @Override public Dimension getPreferredSize() {
        return _i==null?new Dimension(_w, _h):new Dimension(_i.getWidth(null), _i.getHeight(null));
    }

    public void setImage(Image i) {
        _i = i;
        //invalidate();
        repaint();
    }

    @Override public void paint(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        int x = (getWidth()-_i.getWidth(null))/2;
        //int y = (getHeight()-_i.getHeight(null))/2;
        //int y = (getHeight()-_h)/2;
        int y = 0;
        if(_i!=null) {
            //g.drawImage(_i, null, x, y);
            g.drawImage(_i, x, y, Color.BLACK, null);
        }
        else {
            g.setPaint(Color.RED);
            g.fillRect(x, y, _w, _h);
        }
    }
}
