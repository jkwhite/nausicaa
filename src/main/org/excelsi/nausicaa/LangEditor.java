package org.excelsi.nausicaa;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;
import org.excelsi.nausicaa.ca.*;


public class LangEditor extends JComponent implements TimelineListener {
    private UIActions _ui;
    private Rule _rule;
    private Language _lang;
    private JFrame _root;
    private int[] _colors;
    private final Timeline _timeline;
    private final MutationFactor _f;


    public LangEditor(JFrame root, UIActions ui, Timeline timeline, MutationFactor f) {
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

    public void futureChanged() {
        removeAll();
        setLayout(new BorderLayout());

        final CA current = _ui.getActiveCA();
        _rule = current.getRule();
        _lang = ((AbstractComputedRuleset)_rule.origin()).language();
        //JPanel scr = new JPanel(new FlowLayout());
        JPanel scr = new JPanel();
        BoxLayout bl = new BoxLayout(scr, BoxLayout.Y_AXIS);
        scr.setLayout(bl);

        scr.add(new JLabel("Dictionary"));

        //final JTextField rule = new JTextField(50);
        final JTextArea dict = new JTextArea(10,80);
        //if(_rule instanceof Genomic) {
            //rule.setText(((Genomic)_rule).prettyGenome());
        //}
        String text = buildText(_lang);
        dict.setText(text);

        scr.add(new JScrollPane(dict));

        final JCheckBox det = new JCheckBox("Deterministic");
        det.setSelected(_lang.deterministic());
        scr.add(det);

        final JCheckBox ndet = new JCheckBox("Nondeterministic");
        ndet.setSelected(_lang.nondeterministic());
        scr.add(ndet);

        final JCheckBox ctx = new JCheckBox("Contextual");
        ctx.setSelected(_lang.context());
        scr.add(ctx);

        //scr.add(new JLabel("Test Pattern"));
        //final JTextArea pat = new JTextArea(3,80);
        //scr.add(pat);
        /*
        JButton testp = new JButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MutationFactor mf = Actions.createMutationFactor(current, _ui.getConfig(), new Random(), true);
                String[] ptext = pat.getText().replace("\n", " ").split(" ");
                Pattern p = ((ComputedRule2d)_rule.origin().create(rule.getText(), mf)).createPattern();
                double[] ps = new double[ptext.length];
                for(int i=0;i<ptext.length;i++) {
                    ps[i] = Double.parseDouble(ptext[i]);
                }
                double next = p.next(0, ps, new Pattern.Ctx());
                System.err.println("Next: "+next);
            }
        });
        testp.setText("Test");
        scr.add(testp);
        */
        dict.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if(e.getModifiers()!=0) {
                    return;
                }
                if(e.getKeyChar()=='\n') {
                    _ui.doWait(new Runnable() {
                        public void run() {
                            updateCA(dict, det, ndet, ctx);
                        }
                    }, 1000);
                }
            }
        });
        add(scr, BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton ne = new JButton("Ok");
        //JButton de = new JButton("Cancel");
        bot.add(ne);
        add(bot, BorderLayout.SOUTH);
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                updateCA(dict, det, ndet, ctx);
            }
        });

        validate();
        if(getParent()!=null) {
            Dimension dim = getParent().getPreferredSize();
            _root.setSize(16+dim.width, 4*24+dim.height);
        }
    }

    private void updateCA(JTextArea dict, JCheckBox det, JCheckBox ndet, JCheckBox ctx) {
        final CA current = _ui.getActiveCA();
        String d = dict.getText();
        Language nlang = parseLang(d, det.isSelected(), ndet.isSelected(), ctx.isSelected());
        //System.err.println("*** FACTOR: "+_f.transition());
        //_ui.setActiveCA(current.mutate(_rule.origin().create(g, _f), _ui.getActiveCA().getRandom()));
        _ui.setActiveCA(current.mutate(((ComputedRule2d)_rule).derive(nlang), _ui.getActiveCA().getRandom()));
        dict.setText(d);
        dict.requestFocus();
    }

    private String buildText(Language lang) {
        StringBuilder dict = new StringBuilder();
        for(Map.Entry<String,String> en:lang.dict().entrySet()) {
            dict.append(en.getKey()).append(" : ")
                .append(en.getValue()).append("\n");
        }
        return dict.toString();
    }

    private Language parseLang(String s, boolean det, boolean ndet, boolean ctx) {
        Language lang = new Language("apwodkaw");
        lang.deterministic(det);
        lang.nondeterministic(ndet);
        lang.context(ctx);
        return lang;
    }
}
