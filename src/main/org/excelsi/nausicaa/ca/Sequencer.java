package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import com.google.gson.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import static org.excelsi.nausicaa.ca.Sequence.Segment;


public class Sequencer {
    private static final Logger LOG = LoggerFactory.getLogger(Sequencer.class);

    private final File _root;
    private Sequence _seq;


    public Sequencer(String root) throws IOException {
        _root = new File(root);
        if(!_root.exists()) {
            _root.mkdirs();
        }
    }

    public String[] listSequences() {
        return _root.list();
    }

    public void setActive(Sequence s) {
        _seq = s;
        if(_seq!=null) {
            File sdir = sequenceDir();
            if(!sdir.exists()) {
                sdir.mkdirs();
            }
        }
    }

    public Sequence getActive() {
        return _seq;
    }

    public File sequenceDir() {
        File s = new File(_root, _seq.name());
        return s;
    }

    public List<Action> sync() {
        List<Action> acts = new ArrayList<>();
        if(_seq!=null) {
            File s = sequenceDir();
            Segment[] segs = _seq.segments();
            for(int i=0;i<segs.length;i++) {
                File segDir = new File(s, ""+i);
                if(!segDir.exists()) {
                    acts.add(new Action(
                        "Generate "+i+" for "+segs[i].gens()+": "+segs[i].ca().getName(),
                        ()->{}
                        ));
                }
            }
        }
        return acts;
    }

    public static class Action {
        private final String _desc;
        private final Runnable _r;


        public Action(String desc, Runnable r) {
            _desc = desc;
            _r = r;
        }

        public String desc() { return _desc; }
        public Runnable r() { return _r; }
    }
}
