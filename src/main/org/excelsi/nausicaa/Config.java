package org.excelsi.nausicaa;


import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import com.google.gson.*;
import org.excelsi.nausicaa.ca.Json;


public class Config {
    public static final String CONFIG_FILE = System.getProperty("user.home")+"/.nausicaa";
    private List<ConfigListener> _listeners = new ArrayList<>();
    private int _w;
    private int _h;
    private int _d;
    private int _prelude = 0;
    private float _weight = 1f;
    private long _seed = System.currentTimeMillis(); //19450806L;
    private float _scale = 1f;
    private long _animationDelay = 100;
    private boolean _forceSymmetry = true;
    private boolean _ruleVariations = true;
    private boolean _hueVariations = false;
    private boolean _weightVariations = true;
    private final Map<String,Object> _variables = new HashMap<>();
    private String _saveDir = System.getProperty("user.home");
    private String _imgDir = System.getProperty("user.home");
    private String _genDir = System.getProperty("user.home");


    public Config() {
        this(300, 300, 1, 1f);
    }

    public Config(int w, int h, int d, float weight) {
        _w = w;
        _h = h;
        _d = d;
        _weight = weight;
    }

    public void addListener(ConfigListener l) {
        _listeners.add(l);
    }

    public void removeListener(ConfigListener l) {
        _listeners.remove(l);
    }

    public void setSize(int w, int h) {
        _w = w;
        _h = h;
        notify("size");
    }

    public void setSize(int w, int h, int d) {
        _w = w;
        _h = h;
        _d = d;
        notify("size");
    }

    public void setSize(int w, int h, int d, int prelude) {
        _w = w;
        _h = h;
        _d = d;
        _prelude = prelude;
        notify("size");
    }

    public void setSize(int w, int h, int d, int prelude, float weight) {
        _w = w;
        _h = h;
        _d = d;
        _prelude = prelude;
        _weight = weight;
        notify("size");
    }

    public void setScale(float s) {
        _scale = s;
        notify("scale");
    }

    public void setPrelude(int prelude) {
        _prelude = prelude;
    }

    public int getWidth() {
        return _w;
    }

    public int getHeight() {
        return _h;
    }

    public int getDepth() {
        return _d;
    }

    public float getScale() {
        return _scale;
    }

    public int getPrelude() {
        return _prelude;
    }

    public float getWeight() {
        return _weight;
    }

    public void setWeight(float w) {
        _weight = w;
    }

    public void setSeed(long seed) {
        _seed = seed;
        notify("seed");
    }

    public long getSeed() {
        return _seed;
    }

    public void setAnimationDelay(long delay) {
        if(_animationDelay!=delay) {
            _animationDelay = delay;
            notify("animationDelay");
        }
    }

    public long getAnimationDelay() {
        return _animationDelay;
    }

    public void setForceSymmetry(boolean forceSymmetry) {
        _forceSymmetry = forceSymmetry;
        notify("forceSymmetry");
    }

    public boolean getForceSymmetry() {
        return _forceSymmetry;
    }

    public void setRuleVariations(boolean ruleVariations) {
        _ruleVariations = ruleVariations;
        notify("ruleVariations");
    }

    public boolean getRuleVariations() {
        return _ruleVariations;
    }

    public void setHueVariations(boolean hueVariations) {
        _hueVariations = hueVariations;
        notify("hueVariations");
    }

    public boolean getHueVariations() {
        return _hueVariations;
    }

    public void setWeightVariations(boolean weightVariations) {
        _weightVariations = weightVariations;
        notify("weightVariations");
    }

    public boolean getWeightVariations() {
        return _weightVariations;
    }

    public void setSaveDir(String dir) {
        _saveDir = dir;
        notify("saveDir");
    }

    public String getSaveDir() {
        return _saveDir;
    }

    public void setImgDir(String dir) {
        _imgDir = dir;
        notify("imgDir");
    }

    public String getImgDir() {
        return _imgDir;
    }

    public void setGenDir(String dir) {
        _genDir = dir;
        notify("genDir");
    }

    public String getGenDir() {
        return _genDir;
    }

    public void setVariable(String name, Object o) {
        _variables.put(name, o);
        notify(name);
    }

    public <T> T getVariable(String name, T dvalue) {
        T t = (T) _variables.get(name);
        return t!=null?t:dvalue;
    }

    public int getIntVariable(String name, int dvalue) {
        Object v = (Object) _variables.get(name);
        if(v instanceof String) {
            return Integer.parseInt((String)v);
        }
        else if(v instanceof Integer) {
            return ((Integer)v).intValue();
        }
        else {
            return dvalue;
        }
    }

    public float getFloatVariable(String name, float dvalue) {
        Object v = (Object) _variables.get(name);
        if(v instanceof String) {
            return Float.parseFloat((String)v);
        }
        else if(v instanceof Float) {
            return ((Float)v).floatValue();
        }
        else if(v instanceof Integer) {
            return (float) ((Integer)v).intValue();
        }
        else {
            return dvalue;
        }
    }

    public void notify(final String p) {
        for(ConfigListener l:new ArrayList<>(_listeners)) {
            l.configChanged(this, p);
        }
        save();
    }

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("width", _w);
        o.addProperty("height", _h);
        o.addProperty("depth", _d);
        o.addProperty("prelude", _prelude);
        o.addProperty("weight", _weight);
        o.addProperty("scale", _scale);
        o.addProperty("animationDelay", _animationDelay);
        o.addProperty("forceSymmetry", _forceSymmetry);
        o.addProperty("ruleVariations", _ruleVariations);
        o.addProperty("hueVariations", _hueVariations);
        o.addProperty("weightVariations", _weightVariations);
        o.addProperty("saveDir", _saveDir);
        o.addProperty("imgDir", _imgDir);
        o.addProperty("genDir", _genDir);
        JsonObject vars = new JsonObject();
        for(Map.Entry<String,Object> e:_variables.entrySet()) {
            JsonObject vo = new JsonObject();
            vo.addProperty("type", e.getValue().getClass().getName());
            vo.addProperty("value", e.getValue().toString());
            vars.add(e.getKey(), vo);
        }
        o.add("variables", vars);
        return o;
    }

    public void save() {
        try(FileWriter w = new FileWriter(CONFIG_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(toJson(), w);
        }
        catch(IOException e) {
            System.err.println("failed saving config to "+CONFIG_FILE+": "+e);
            e.printStackTrace();
        }
    }

    public static Config load() {
        Reader r = null;
        try {
            File f = new File(CONFIG_FILE);
            if(!f.exists()) {
                System.err.println("no config file, using default");
                return new Config();
            }
            JsonObject o = (JsonObject) new JsonParser().parse(new BufferedReader(new FileReader(f)));
            Config c = new Config();
            c._w = Json.integer(o, "width", 300);
            c._h = Json.integer(o, "height", 300);
            c._d = Json.integer(o, "depth", 1);
            c._prelude = Json.integer(o, "prelude", 0);
            c._weight = Json.flot(o, "weight", 1f);
            c._scale = Json.flot(o, "scale", 1f);
            c._animationDelay = Json.lng(o, "animationDelay", 100);
            c._forceSymmetry = Json.bool(o, "forceSymmetry", false);
            c._ruleVariations = Json.bool(o, "ruleVariations", true);
            c._hueVariations = Json.bool(o, "hueVariations", true);
            c._weightVariations = Json.bool(o, "weightVariations", true);
            c._saveDir = Json.string(o, "saveDir", c._saveDir);
            c._imgDir = Json.string(o, "imgDir", c._imgDir);
            c._genDir = Json.string(o, "genDir", c._genDir);
            JsonElement evars = o.get("variables");
            if(evars!=null && evars instanceof JsonObject) {
                JsonObject vars = (JsonObject) evars;
                for(Map.Entry<String,JsonElement> e:vars.entrySet()) {
                    try {
                        JsonObject vo = (JsonObject) e.getValue();
                        c._variables.put(e.getKey(), Json.string(vo, "value"));
                    }
                    catch(Exception ex) {
                        System.err.println("failed reading var "+e.getKey()+", skipping: "+ex);
                        ex.printStackTrace();
                    }
                }
            }
            return c;
        }
        catch(IOException e) {
            System.err.println("failed reading "+CONFIG_FILE+", using default: "+e);
            e.printStackTrace();
        }
        return new Config();
    }
}
