package org.excelsi.nausicaa;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;
import org.excelsi.nausicaa.ca.Colors;
import org.excelsi.nausicaa.ca.Rule;
import org.excelsi.nausicaa.ca.CA;
import org.excelsi.nausicaa.ca.IndexedRule;
import org.excelsi.nausicaa.ca.Palette;
import org.excelsi.nausicaa.ca.IndexedPalette;


public class PaletteEditor extends JComponent implements TimelineListener {
    private UIActions _ui;
    private Rule _rule;
    private JFrame _root;
    private int[] _colors;
    private final Timeline _timeline;


    public PaletteEditor(JFrame root, UIActions ui, Timeline timeline) {
        _root = root;
        _ui = ui;
        _timeline = timeline;
        timeline.addTimelineListener(this);
        futureChanged();
    }

    @Override public void timelineChanged(TimelineEvent e) {
        futureChanged();
    }

    public void disconnect() {
        _timeline.removeTimelineListener(this);
    }

    public void futureChanged() {
        removeAll();
        setLayout(new BorderLayout());

        final CA current = _ui.getActiveCA();
        _rule = current.getRule();
        JPanel scr = new JPanel(new FlowLayout());
        scr.setLayout(new BoxLayout(scr, BoxLayout.Y_AXIS));
        final int[] colors = current.getPalette().getColors();
        _colors = new int[colors.length];
        System.arraycopy(colors, 0, _colors, 0, colors.length);
        JPanel cols = new JPanel();
        for(int i=0;i<colors.length;i++) {
            final CAEditor.Cell c = new CAEditor.Cell(colors[i]);
            final int idx = i;
            cols.add(c);
            c.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    java.awt.Color co = JColorChooser.showDialog(
                        PaletteEditor.this, "New color", new java.awt.Color(_colors[idx]));
                    int rgb = co.getRGB();
                    _colors[idx] = (rgb&Colors.COLOR_MASK)|(_colors[idx]&Colors.ALPHA_MASK);
                    c.setColor(_colors[idx]);
                    _ui.doWait(new Runnable() {
                        public void run() {
                            _ui.setActiveCA(current.palette(new IndexedPalette(_colors)));
                        }
                    }, 1000);
                }
            });
        }
        add(cols, BorderLayout.CENTER);
        validate();
        if(getParent()!=null) {
            Dimension dim = getParent().getPreferredSize();
            _root.setSize(16+dim.width, 24+dim.height);
        }
    }
}
