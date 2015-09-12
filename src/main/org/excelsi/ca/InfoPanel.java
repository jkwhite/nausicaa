package org.excelsi.ca;


import javax.swing.*;
import java.awt.GridLayout;
import java.util.*;


public class InfoPanel extends JPanel {
    private java.util.List<JComponent[]> _pairs = new ArrayList<JComponent[]>();


    public InfoPanel() {
    }

    public InfoPanel addPair(String label, Object value) {
        JComponent val = null;
        if(value instanceof JComponent) {
            val = (JComponent) value;
        }
        else {
            val = new JLabel(value.toString());
        }
        _pairs.add(new JComponent[]{new JLabel(label), val});
        return this;
    }

    public void done() {
        GroupLayout g = new GroupLayout(this);
        g.setAutoCreateGaps(true);
        g.setAutoCreateContainerGaps(true);
        setLayout(g);

        GroupLayout.SequentialGroup hGroup = g.createSequentialGroup();
        GroupLayout.Group gr1 = g.createParallelGroup();
        for(JComponent[] p:_pairs) {
            gr1.addComponent(p[0]);
        }
        hGroup.addGroup(gr1);
        GroupLayout.Group gr2 = g.createParallelGroup();
        for(JComponent[] p:_pairs) {
            gr2.addComponent(p[1]);
        }
        hGroup.addGroup(gr2);
        g.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = g.createSequentialGroup();
        for(JComponent[] p:_pairs) {
            GroupLayout.Group vgr1 = g.createParallelGroup(GroupLayout.Alignment.BASELINE);
            vgr1.addComponent(p[0]).addComponent(p[1]);
            vGroup.addGroup(vgr1);
        }
        g.setVerticalGroup(vGroup);
        /*
        GridLayout g = new GridLayout(_pairs.size(), 2);
        setLayout(g);
        for(JComponent[] p:_pairs) {
            add(p[0]);
            add(p[1]);
        }
        */
    }
}
