package org.excelsi.ca;


import java.io.*;


public class Yggdrasil implements Serializable {
    private static final long serialVersionUid = 1L;
    private static final String STORE = ".yggdrasil";
    private transient String _dir;
    private Ruleset _rules;
    private Branch<World> _root;


    /*
    public static Yggdrasil revive(String dir) {
        ObjectInputStream ois = null;
        Yggdrasil y = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(dir+File.separator+STORE));
            y = (Yggdrasil) ois.readObject();
        }
        catch(IOException e) {
            throw new Error(e);
        }
        catch(ClassNotFoundException e) {
            throw new Error(e);
        }
        finally {
            if(ois!=null) {
                try {
                    ois.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        y._dir = dir;
        return y;
    }

    public static Yggdrasil root(String dir, Ruleset rules) {
        if(dir!=null) {
            File d = new File(dir);
            if(d.exists()) {
                throw new IllegalStateException("storage area '"+dir+"' already exists");
            }
            d.mkdirs();
        }
        Yggdrasil y = new Yggdrasil();
        y._rules = rules;
        y._dir = dir;
        y._root = new Branch<World>(new World(rules.random().next(), World.getSize(), World.getSize()));
        if(dir!=null) {
            y.save();
        }
        return y;
    }
    */

    public static Yggdrasil revive(String dir) {
        ObjectInputStream ois = null;
        Yggdrasil y = null;
        try {
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(dir+".ygg")));
            y = (Yggdrasil) ois.readObject();
        }
        catch(IOException e) {
            throw new Error(e);
        }
        catch(ClassNotFoundException e) {
            throw new Error(e);
        }
        finally {
            if(ois!=null) {
                try {
                    ois.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        y._dir = dir;
        return y;
    }

    public static Yggdrasil root(String file, Ruleset rules) {
        if(file!=null) {
            File f = new File(file);
        }
        Yggdrasil y = new Yggdrasil();
        y._rules = rules;
        y._dir = file;
        y._root = new Branch<World>(new World(rules.random().next(), World.getSize(), World.getSize()));
        //if(dir!=null) {
            //y.save();
        //}
        return y;
    }

    private Yggdrasil() {
    }

    public Branch<World> root() {
        return _root;
    }

    public void save(String file) throws IOException {
        _dir = file;
        save();
    }

    public void save() throws IOException {
        if(_dir==null) {
            throw new IllegalStateException("no directory set");
        }
        ObjectOutputStream oos = null;
        try {
            //oos = new ObjectOutputStream(new FileOutputStream(_dir+File.separator+STORE));
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(_dir+".ygg")));
            oos.writeObject(this);
        }
        finally {
            if(oos!=null) {
                try {
                    oos.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
