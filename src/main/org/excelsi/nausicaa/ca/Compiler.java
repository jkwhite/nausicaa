package org.excelsi.nausicaa.ca;


import java.util.*;
import java.io.IOException;


public class Compiler {
    private final Genome _g;
    private final Implicate _i;
    private Register _reg;
    private Program _prg;


    public Compiler(Genome g, Implicate i) {
        _g = g;
        _i = i;
        _reg = new Register();
        _prg = new Program();
    }

    public Implicate implicate() {
        return _i;
    }

    public Register register() {
        return _reg;
    }

    public Program program() {
        return _prg;
    }

    public String nativeType() {
        switch(implicate().archetype().values()) {
            case discrete:
                return "int";
            case continuous:
                return "double";
            default:
                throw new IllegalArgumentException("unsupported type "+implicate().archetype().values());
        }
    }

    public String compile() {
        final Codon[] cs = _g.codons(_i);
        final Register r = new Register();

        for(int i=0;i<cs.length;i++) {
            final Codon c = cs[i];
            c.compile(this);
        }
        Node n = r.pop();
        if(n instanceof Variable) {
            r.push(new Assign((Variable)n));
        }
        return "this is where the output program goes if there was one";
    }

    public static class Register {
        private final List<Node> _values = new ArrayList<>();

        public void push(Node n) {
            _values.add(n);
        }

        public Node[] values() {
            return _values.toArray(new Node[0]);
        }

        public Node[] popAll() {
            Node[] ns = values();
            _values.clear();
            return ns;
        }

        public Node pop() {
            Node n = _values.remove(_values.size()-1);
            return n;
        }
    }

    public static interface Node {
        void emit(Appendable a) throws IOException;
    }

    public static class Return implements Node {
        private final Variable _v;

        public Return(Variable v) {
            _v = v;
        }

        @Override public void emit(Appendable a) {
            //a.append
        }
    }

    public static class Op implements Node {
        private final Node[] _childs;
        private final String _op;


        public Op(String op, Node[] childs) {
            _op = op;
            _childs = childs;
        }

        public void emit(Appendable a) throws IOException {
            for(int i=0;i<_childs.length;i++) {
                _childs[i].emit(a);
                if(i<_childs.length-1) {
                    a.append(_op);
                }
            }
        }
    }

    public static class Assign implements Node {
        private final Variable _v;


        public Assign(Variable v) {
            _v = v;
        }

        @Override public void emit(Appendable a) throws IOException {
            a.append(_v.type()+" "+_v.name()+" = ");
            _v.emit(a);
        }
    }

    public static class Variable implements Node {
        private static int _n = 0;

        private final String _name;
        private final String _type;
        private String _value;

        private Variable(String name, String type) {
            _name = name;
            _type = type;
        }

        @Override public String toString() { return _name; }

        @Override public void emit(Appendable a) throws IOException {
            if(_value!=null) {
                a.append(_value);
            }
            else {
                a.append("<error: unassigned value for "+_name+">");
            }
        }

        public String name() { return _name; }
        public String type() { return _type; }

        public Variable value(String value) {
            _value = value;
            return this;
        }

        public static Variable next(String type) {
            return new Variable("var"+(_n++), type);
        }
    }

    public static class Program {
        private final List<Statement> _stmts = new ArrayList<>();

        public void add(Statement s) {
            _stmts.add(s);
        }
    }

    public static interface Statement extends Node {
    }
}
