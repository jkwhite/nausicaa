package org.excelsi.ca;


import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.*;
import javax.imageio.*;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.util.Iterator;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import org.imgscalr.Scalr;


public class CA2 implements java.io.Serializable {
    private transient BufferedImage _i;
    private transient JFrame _d;
    private float _scale = 1f;


    public CA2(int width, int time) {
        _i = new BufferedImage(width, time, BufferedImage.TYPE_BYTE_INDEXED);
        //_i = new BufferedImage(width, time, BufferedImage.TYPE_USHORT_555_RGB);
    }

    public CA2(BufferedImage i) {
        _i = i;
    }

    public int getWidth() {
        return _i.getWidth();
    }

    public int getHeight() {
        return _i.getHeight();
    }
}
