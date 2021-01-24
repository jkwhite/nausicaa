package org.excelsi.nausicaa;


import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;


public class JIteratedFunction extends JComponent {
    public JIteratedFunction() {
    }

    public int getWidth() { return 600; }
    public int getHeight() { return 600; }

    protected void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        for(int i=0;i<42;i++) {
            g.drawLine(300,300,600,600);
            g.rotate(Math.PI/8d, 300, 300);
            g.scale(0.9d, 0.9d);
        }
    }
}
