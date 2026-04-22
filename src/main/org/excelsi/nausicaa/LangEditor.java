package org.excelsi.nausicaa;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;
import org.excelsi.nausicaa.ca.*;
import com.google.gson.*;


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
        JPanel scr = new JPanel();
        BoxLayout bl = new BoxLayout(scr, BoxLayout.Y_AXIS);
        scr.setLayout(bl);

        JPanel nam = new JPanel();
        final JTextField name = new JTextField(40);
        name.setText(_lang.name());
        nam.add(new JLabel("Name"));
        nam.add(name);
        scr.add(nam);

        scr.add(new JLabel("Dictionary"));

        final JTextArea dict = new JTextArea(10,80);
        String text = buildText(_lang);
        dict.setText(text);

        scr.add(new JScrollPane(dict));
        JPanel check = new JPanel(new FlowLayout(FlowLayout.LEFT));

        final JCheckBox pat = new JCheckBox("Pattern");
        pat.setSelected(_lang.pattern());
        check.add(pat);

        final JCheckBox det = new JCheckBox("Deterministic");
        det.setSelected(_lang.deterministic());
        check.add(det);

        final JCheckBox ndet = new JCheckBox("Nondeterministic");
        ndet.setSelected(_lang.nondeterministic());
        check.add(ndet);

        final JCheckBox ctx = new JCheckBox("Contextual");
        ctx.setSelected(_lang.contextual());
        check.add(ctx);

        final JCheckBox loc = new JCheckBox("Location");
        loc.setSelected(_lang.location());
        check.add(loc);

        final JCheckBox tap = new JCheckBox("Tape");
        tap.setSelected(_lang.tape());
        check.add(tap);

        final JCheckBox pos = new JCheckBox("Positioning");
        pos.setSelected(_lang.positioning());
        check.add(pos);

        scr.add(check);

        add(scr, BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton ok = new JButton("Ok");
        JButton ne = new JButton("Update");
        JButton de = new JButton("Cancel");
        JButton exp = new JButton("Export ...");
        JButton imp = new JButton("Import ...");
        bot.add(ok);
        bot.add(ne);
        bot.add(de);
        bot.add(exp);
        bot.add(imp);
        add(bot, BorderLayout.SOUTH);
        ok.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                updateCA(name, dict, pat, det, ndet, ctx, loc, tap, pos);
                _ui.toggleLangEditor();
            }
        });
        ne.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                updateCA(name, dict, pat, det, ndet, ctx, loc, tap, pos);
            }
        });
        de.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                _ui.toggleLangEditor();
            }
        });
        exp.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser f = new JFileChooser(_ui.getConfig().getLangDir());
                f.setDialogTitle("Export language");
                f.setDialogType(f.SAVE_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showSaveDialog(_root);
                if(ret==f.APPROVE_OPTION) {
                    File lang = f.getSelectedFile();

                    _ui.getConfig().setLangDir(lang.getParent());
                    if(!lang.getName().endsWith(".lang")) {
                        lang = new File(lang.toString()+".lang");
                    }
                    try(BufferedWriter w=new BufferedWriter(new FileWriter(lang))) {
                        Gson gson = new GsonBuilder()
                            .setPrettyPrinting()
                            .create();
                        gson.toJson(_lang.toJson(), w);
                    }
                    catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        imp.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {
                final JFileChooser f = new JFileChooser(_ui.getConfig().getLangDir());
                f.setDialogTitle("Import language");
                f.setDialogType(f.OPEN_DIALOG);
                f.setMultiSelectionEnabled(false);
                int ret = f.showOpenDialog(_root);
                if(ret==f.APPROVE_OPTION) {
                    File lang = f.getSelectedFile();
                    _ui.getConfig().setLangDir(lang.getParent());
                    try(BufferedReader r=new BufferedReader(new FileReader(lang))) {
                        JsonElement e = new JsonParser().parse(r);
                        Language nlang = Language.fromJson(e);
                        name.setText(nlang.name());
                        dict.setText(buildText(nlang));
                        det.setSelected(nlang.deterministic());
                        ndet.setSelected(nlang.nondeterministic());
                        ctx.setSelected(nlang.contextual());
                        pos.setSelected(nlang.positioning());
                        _lang = nlang;
                    }
                    catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        validate();
        if(getParent()!=null) {
            Dimension dim = getParent().getPreferredSize();
            _root.setSize(16+dim.width, 4*24+dim.height);
        }
    }

    private void updateCA(JTextField name, JTextArea dict, JCheckBox pat, JCheckBox det, JCheckBox ndet, JCheckBox ctx, JCheckBox loc, JCheckBox tap, JCheckBox pos) {
        String d = dict.getText();
        Language nlang = parseLang(name.getText(), d, pat.isSelected(), det.isSelected(), ndet.isSelected(), ctx.isSelected(), loc.isSelected(), tap.isSelected(), pos.isSelected());
        updateCA(nlang);
        dict.setText(d);
        dict.requestFocus();
    }

    private void updateCA(Language nlang) {
        final CA current = _ui.getActiveCA();
        _ui.setActiveCA(current.mutate(((ComputedRule2d)_rule).derive(nlang), _ui.getActiveCA().getRandom()));
    }

    private String buildText(Language lang) {
        StringBuilder dict = new StringBuilder();
        for(Map.Entry<String,String> en:lang.dict().entrySet()) {
            dict.append(en.getKey()).append(" : ")
                .append(en.getValue()).append("\n");
        }
        return dict.toString();
    }

    private Language parseLang(String name, String dict, boolean pat, boolean det, boolean ndet, boolean ctx, boolean loc, boolean tap, boolean pos) {
        Language lang = new Language(name);
        for(String ent:dict.split("\\n")) {
            if(ent.trim().length()>0) {
                String[] kv = ent.split(":");
                lang.add(kv[0].trim(), kv[1].trim());
            }
        }
        lang.pattern(pat)
            .deterministic(det)
            .nondeterministic(ndet)
            .contextual(ctx)
            .location(loc)
            .tape(tap)
            .positioning(pos);
        return lang;
    }
}
