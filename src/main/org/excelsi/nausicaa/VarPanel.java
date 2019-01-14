package org.excelsi.nausicaa;


import javax.swing.*;
import java.awt.GridLayout;
import java.util.*;

import org.excelsi.nausicaa.ca.*;


public class VarPanel extends JPanel {
    private final java.util.List<JComponent[]> _pairs = new ArrayList<JComponent[]>();
    private final Map<String,JTextField> _vals = new HashMap<>();
    private final Varmap _v;


    public VarPanel(Varmap v) {
        System.err.println("*** showing vars: "+v);
        for(String n:v.names()) {
            addPair(n, v.get(n));
        }
        done();
        _v = v;
    }

    public VarPanel addPair(String label, String value) {
        JTextField val = new JTextField(value, 10);
        _pairs.add(new JComponent[]{new JLabel(label), val});
        _vals.put(label, val);
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
    }

    public void commit() {
        for(Map.Entry<String,JTextField> e:_vals.entrySet()) {
            _v.put(e.getKey(), e.getValue().getText());
        }
    }
}
