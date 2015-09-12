
package org.excelsi.ca;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;


public class PaletteEditor extends JComponent implements ViewerListener {
    private Futures _f;
    private Rule _rule;
    private JFrame _root;
    private int[] _colors;


    public PaletteEditor(JFrame root, Viewer v) {
        _root = root;
        v.addViewerListener(this);
        if(v.currentFuture()!=null) {
            futureChanged(v.currentFuture());
        }
    }

    public void futureChanging(Futures f) {
    }

    public void futureChanged(Futures f) {
        removeAll();
        _f = f;
        setLayout(new BorderLayout());
        Display d = f.getMainDisplay();
        _rule = d.getRule();
        JPanel scr = new JPanel(new FlowLayout());
        scr.setLayout(new BoxLayout(scr, BoxLayout.Y_AXIS));
        final int[] colors = _rule.colors();
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
                    _colors[idx] = (rgb&CA.COLOR_MASK)|(_colors[idx]&CA.ALPHA_MASK);
                    c.setColor(_colors[idx]);
                    Viewer.getInstance().doWait(new Runnable() {
                        public void run() {
                            try {
                                if(_rule instanceof Multirule) {
                                    Rule[] orig = ((Multirule)_rule).rules();
                                    final Rule[] ar = new Rule[orig.length];
                                    for(int j=0;j<ar.length;j++) {
                                        ar[j] = Hue.replace(orig[j], colors[idx], _colors[idx]);
                                    }
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            _f.tick(_rule.origin().create((Object[])ar));
                                        }
                                    });
                                }
                            }
                            catch(MutationFailedException ex) {
                                ex.printStackTrace();
                            }
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
