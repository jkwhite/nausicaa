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
import org.excelsi.nausicaa.ca.Parameters;
import org.excelsi.nausicaa.ca.AbstractComputedRuleset;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class RuleEditor extends JComponent implements TimelineListener {
    private static final Logger LOG = LoggerFactory.getLogger(RuleEditor.class);
    private final UIActions _ui;
    private Rule _rule;
    private JTextArea _ruleText;
    private JTextField _trans;
    private final JFrame _root;
    private final Timeline _timeline;
    private final Parameters _params;


    public RuleEditor(JFrame root, UIActions ui, Timeline timeline, Parameters p) {
        _root = root;
        _ui = ui;
        _timeline = timeline;
        _params = p;
        timeline.addTimelineListener(this);
        futureChanged();
    }

    @Override public void timelineChanged(TimelineEvent e) {
        futureChanged();
    }

    public void disconnect() {
        _timeline.removeTimelineListener(this);
        LOG.debug("removed rule editor as timeline listener");
    }

    public void commit() {
        _ui.doWait(new Runnable() {
            public void run() {
                String g = _ruleText.getText();
                int caret = _ruleText.getCaretPosition();
                final CA current = _ui.getActiveCA();
                final float trans = Float.valueOf(_trans.getText());
                _ui.setActiveCA(current.mutate(_rule.origin()
                    .create(g, _params.transition(trans)), _ui.getActiveCA().getRandom()));
                _ruleText.requestFocus();
                _ruleText.setCaretPosition(caret);
            }
        }, 1000);
    }

    public void focusIncantation() {
        _ruleText.requestFocus();
    }

    public void futureChanged() {
        removeAll();
        setLayout(new BorderLayout());

        final CA current = _ui.getActiveCA();
        _rule = current.getRule();
        JPanel scr = new JPanel();
        BoxLayout bl = new BoxLayout(scr, BoxLayout.Y_AXIS);
        scr.setLayout(bl);

        JPanel in = new JPanel(new FlowLayout(FlowLayout.LEFT));
        in.add(new JLabel("Incantation"));
        scr.add(in);

        final JTextArea rule = new JTextArea(10,80);
        if(_rule instanceof Genomic) {
            final String gen = ((Genomic)_rule).prettyGenome();
            rule.setText(gen);
            int nls = 0;
            for(int i=0;i<gen.length();i++) {
                if(gen.charAt(i)=='\n') nls++;
            }
            rule.setRows(Math.max(10, Math.min(20,nls)));
        }
        scr.add(new JScrollPane(rule));
        _ruleText = rule;

        if(_rule instanceof ComputedRule2d) {
            JPanel params = new JPanel(new FlowLayout(FlowLayout.LEFT));
            params.add(new JLabel("Transition"));
            _trans = new JTextField(8);
            _trans.setText(Float.toString(((ComputedRule2d)_rule).transition()));
            params.add(_trans);
            scr.add(params);
        }

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ne = new JButton("Ok");
        JButton upd = new JButton("Update");
        JButton de = new JButton("Cancel");

        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                commit();
                _ui.toggleRuleEditor();
            }
        });
        upd.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                commit();
            }
        });
        de.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _ui.toggleRuleEditor();
            }
        });
        bot.add(ne);
        bot.add(upd);
        bot.add(de);
        /*
        TODO: Need to figure out languages first before this is really useful
        if(current.getRule().origin() instanceof AbstractComputedRuleset) {
            JPanel insertions = new JPanel();
            BoxLayout ins = new BoxLayout(insertions, BoxLayout.X_AXIS);
            insertions.add(new JLabel("Insert"));
            java.util.List<String> inserts = new ArrayList<>();
            for(String opt:((AbstractComputedRuleset)current.getRule().origin()).language().dict().keySet()) {
                inserts.add(opt);
            }
            LOG.info("built inserts: "+inserts);
            JComboBox codons = new JComboBox(inserts.toArray(new String[0]));
            insertions.add(codons);
            scr.add(insertions);
        }
        */

        /*
        TODO: either improve test pattern or remove it entirely
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
        */

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
        add(scr, BorderLayout.CENTER);
        add(bot, BorderLayout.SOUTH);
        validate();
    }
}
