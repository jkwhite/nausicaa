package org.excelsi.nausicaa;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;
import org.excelsi.nausicaa.ca.Colors;
import org.excelsi.nausicaa.ca.Pattern;
import org.excelsi.nausicaa.ca.Rule;
import org.excelsi.nausicaa.ca.ComputedRule2d;
import org.excelsi.nausicaa.ca.CA;
import org.excelsi.nausicaa.ca.IndexedRule;
import org.excelsi.nausicaa.ca.Palette;
import org.excelsi.nausicaa.ca.Genomic;
import org.excelsi.nausicaa.ca.MutationFactor;


public class RuleEditor extends JComponent implements TimelineListener {
    private UIActions _ui;
    private Rule _rule;
    private JTextArea _ruleText;
    private JFrame _root;
    // private int[] _colors;
    private final Timeline _timeline;
    private final MutationFactor _f;


    public RuleEditor(JFrame root, UIActions ui, Timeline timeline, MutationFactor f) {
        _root = root;
        _ui = ui;
        _timeline = timeline;
        _f = f;
        timeline.addTimelineListener(this);
        futureChanged();
    }

    @Override public void timelineChanged(TimelineEvent e) {
        futureChanged();
    }

    public void disconnect() {
        _timeline.removeTimelineListener(this);
    }

    public void commit() {
        _ui.doWait(new Runnable() {
            public void run() {
                String g = _ruleText.getText();
                int caret = _ruleText.getCaretPosition();
                final CA current = _ui.getActiveCA();
                _ui.setActiveCA(current.mutate(_rule.origin().create(g, _f), _ui.getActiveCA().getRandom()));
                _ruleText.requestFocus();
                _ruleText.setCaretPosition(caret);
            }
        }, 1000);
    }

    public void futureChanged() {
        removeAll();
        setLayout(new BorderLayout());

        final CA current = _ui.getActiveCA();
        _rule = current.getRule();
        //JPanel scr = new JPanel(new FlowLayout());
        JPanel scr = new JPanel();
        BoxLayout bl = new BoxLayout(scr, BoxLayout.Y_AXIS);
        scr.setLayout(bl);

        scr.add(new JLabel("Genome"));

        final JTextArea rule = new JTextArea(10,80);
        _ruleText = rule;
        if(_rule instanceof Genomic) {
            rule.setText(((Genomic)_rule).prettyGenome());
        }
        scr.add(new JScrollPane(rule));
        scr.add(new JLabel("Test Pattern"));
        final JTextArea pat = new JTextArea(3,80);
        scr.add(pat);
        JButton testp = new JButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MutationFactor mf = Actions.createMutationFactor(current, _ui.getConfig(), new Random(), true);
                System.err.println("PAT TEXT: "+pat.getText());
                String[] ptext = pat.getText().replace("\n", " ").split(" ");
                Pattern p = ((ComputedRule2d)_rule.origin().create(rule.getText(), mf)).createPattern();
                double[] psd = new double[ptext.length];
                int[] psi = new int[ptext.length];
                for(int i=0;i<ptext.length;i++) {
                    if(p.archetype().isDiscrete()) {
                        psi[i] = Integer.parseInt(ptext[i]);
                        System.err.println("PARSE: '"+ptext[i]+"' TO "+psi[i]);
                    }
                    else {
                        psd[i] = Double.parseDouble(ptext[i]);
                        System.err.println("PARSE: '"+ptext[i]+"' TO "+psd[i]);
                    }
                }
                if(p.archetype().isDiscrete()) {
                    int next = p.next(0, psi, new Pattern.Ctx());
                    System.err.println("Next: "+next);
                }
                else {
                    double next = p.next(0, psd, new Pattern.Ctx());
                    System.err.println("Next: "+next);
                }
            }
        });
        testp.setText("Test");
        scr.add(testp);
        /*
        rule.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if(e.getModifiers()!=0) {
                    return;
                }
                if(e.getKeyChar()=='\n') {
                    _ui.doWait(new Runnable() {
                        public void run() {
                            String g = rule.getText();
                            //System.err.println("*** FACTOR: "+_f.transition());
                            _ui.setActiveCA(current.mutate(_rule.origin().create(g, _f), _ui.getActiveCA().getRandom()));
                            rule.setText(g);
                            rule.requestFocus();
                        }
                    }, 1000);
                }
            }
        });
        */
        /*
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
                            _ui.setActiveCA(current.palette(new Palette(_colors)));
                        }
                    }, 1000);
                }
            });
        }
        add(cols, BorderLayout.CENTER);
        */
        add(scr, BorderLayout.CENTER);
        validate();
        if(getParent()!=null) {
            Dimension dim = getParent().getPreferredSize();
            _root.setSize(16+dim.width, 4*24+dim.height);
        }
    }
}
