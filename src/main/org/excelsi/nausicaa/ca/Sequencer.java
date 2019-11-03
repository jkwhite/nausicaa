package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.concurrent.ExecutorService;
import com.google.gson.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import static org.excelsi.nausicaa.ca.Sequence.Segment;


public class Sequencer implements SequenceRepo {
    private static final Logger LOG = LoggerFactory.getLogger(Sequencer.class);

    private final File _root;
    private Sequence _seq;


    public Sequencer(String root) throws IOException {
        _root = new File(root);
        if(!_root.exists()) {
            _root.mkdirs();
        }
    }

    public String[] listSequenceNames() {
        return _root.list();
    }

    public String findUniqueName(String base) {
        Set<String> names = new HashSet<>(Arrays.asList(listSequenceNames()));
        int id = 0;
        while(names.contains(base+id)) {
            id++;
        }
        return base+id;
    }

    public List<Sequence> listSequences() {
        JsonParser p = new JsonParser();
        List<Sequence> ss = new ArrayList<>();
        for(String name:listSequenceNames()) {
            File f = sequenceFile(_root, name);
            try(BufferedReader r = new BufferedReader(new FileReader(f))) {
                JsonElement e = p.parse(r);
                ss.add(Sequence.fromJson(e));
            }
            catch(IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return ss;
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

    public void saveActive() {
        createSaveAction(_seq).run();
    }

    public Sequence getActive() {
        return _seq;
    }

    public File sequenceDir() {
        return sequenceDir(_root, _seq.name());
    }

    public List<Action> sync() {
        List<Action> acts = new ArrayList<>();
        if(_seq!=null) {
            File s = sequenceDir();
            File seqFile = sequenceFile(_root, _seq.name());
            if(!seqFile.exists()) {
                acts.add(new Action(
                    "Save sequence file", createSaveAction(_seq)));
            }
            Segment[] segs = _seq.segments();
            for(int i=0;i<segs.length;i++) {
                //File segDir = new File(s, ""+i);
                File segDir = segmentDir(segs[i], i);
                SegmentInfo info = readSegmentInfo(segs[i], i);
                if(!segDir.exists()) {
                    acts.add(new Action(
                        "Does not exist. Generate "+i+" for "+segs[i].gens()+": "+segs[i].ca().getName(),
                        createCAGeneratorAction(segs[i], i)
                        ));
                }
                else if(!info.generated()) {
                    acts.add(new Action(
                        "Not generated. Generate "+i+" for "+segs[i].gens()+": "+segs[i].ca().getName(),
                        createCAGeneratorAction(segs[i], i)
                        ));
                }
                else if(info.frames().length!=segs[i].gens()) {
                    acts.add(new Action(
                        "Wrong frame count. Generate "+i+" for "+segs[i].gens()+": "+segs[i].ca().getName(),
                        createCAGeneratorAction(segs[i], i)
                        ));
                }
            }
        }
        return acts;
    }

    public SegmentInfo readSegmentInfo(Segment s, int idx) {
        //File segDir = new File(sequenceDir(), ""+idx);
        File segDir = segmentDir(s, idx);
        if(segDir.exists()) {
            //File caFile = new File(segDir, s.ca().getName()+".ca");
            String[] frames = segDir.list((d,f)->{ return f.endsWith(".png"); });
            Arrays.sort(frames);
            return new SegmentInfo(s, segDir, frames, idx);
        }
        return new SegmentInfo(s, segDir, new String[0], idx);
    }

    @Override public Plane readPlane(Segment seg, int ord, int gen, ExecutorService pool, GOptions opt) throws IOException {
        File img = frameFile(ord, gen);
        BufferedImage bi = ImageIO.read(img);
        Plane dest = seg.ca().createPlane(pool, opt);
        ImageInitializer ii = new ImageInitializer(bi);
        ii.init(dest, seg.ca().compileRule(), null);
        return dest;
    }

    private Runnable createSaveAction(Sequence s) {
        return new Runnable() {
            @Override public void run() {
                File seqFile = sequenceFile(_root, s.name());
                seqFile.getParentFile().mkdirs();
                try(BufferedWriter w=new BufferedWriter(new FileWriter(seqFile))) {
                    Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                    gson.toJson(s.toJson(), w);
                }
                catch(IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        };
    }

    private static Runnable createCAGeneratorAction(Segment s, int idx) {
        return null;
    }

    private static File sequenceDir(File root, String seqname) {
        File s = new File(root, seqname);
        return s;
    }

    private static File sequenceFile(File root, String seqname) {
        File d = sequenceDir(root, seqname);
        return new File(d, seqname+".seq");
    }

    private File frameFile(int segment, int gen) {
        File segDir = segmentDir(segment);
        String frame = String.format("%6d.png", gen);
        return new File(segDir, frame);
    }

    private File segmentDir(Segment s, int idx) {
        //File segDir = new File(sequenceDir(), ""+idx);
        //return segDir;
        return segmentDir(idx);
    }

    private File segmentDir(int idx) {
        File segDir = new File(sequenceDir(), ""+idx);
        return segDir;
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

    public static class SegmentInfo {
        private final File _dir;
        private final String[] _frames;
        private final Segment _seg;
        private final int _idx;

        public SegmentInfo(Segment seg, File dir, String[] frames, int idx) {
            _seg = seg;
            _dir = dir;
            _frames = frames;
            _idx = idx;
        }

        public File dir() { return _dir; }
        public String[] frames() { return _frames; }
        public int index() { return _idx; }
        public Segment segment() { return _seg; }

        public boolean exists() { return _frames.length>0; }
        public boolean generated() { return exists() && new File(_dir, _frames[_frames.length-1]).exists(); }
        public long framesGeneratedTs() {
            if(exists() && _frames.length>0) {
                return new File(_dir, _frames[0]).lastModified();
            }
            return -1;
        }
    }
}
