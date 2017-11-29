package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;
import java.util.concurrent.ExecutorService;
import javax.swing.JComponent;


public abstract class PlaneDisplay extends JComponent {
    abstract public JComponent getDisplayComponent();
    abstract public CA getCA();
    abstract public Rule getRule();
    abstract public long getRuleSeed();
    abstract public Plane getPlane();
    abstract public void setCA(CA ca);
    abstract public void setCA(CA ca, ExecutorService pool, GOptions opt);
    abstract public void setPlane(Plane plane);
    abstract public void setScale(float scale);
    abstract public float getScale();
    abstract public int getCAWidth();
    abstract public int getCAHeight();
    abstract public void setCAWidth(int w);
    abstract public void setCAHeight(int h);
    abstract public void reroll(Initializer i);
    abstract public void generate(Initializer i);
    abstract public void toggleShow();
    abstract public boolean delegateUnlock();
}
