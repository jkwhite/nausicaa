package org.excelsi.nausicaa.ifs;


import java.util.*;


public class TreeTape {
    public static final class Tape {
        final List<Op> _ops;

        public Tape() {
            _ops = new ArrayList<>();
        }

        public Tape(List<Op> ops) {
            _ops = ops;
        }

        public Tape append(Op o) {
            _ops.add(o);
            return this;
        }

        public Tape prepend(Op o) {
            _ops.add(0, o);
            return this;
        }

        public Iterator<Op> iterator() {
            return _ops.iterator();
        }

        public List<Op> getOps() {
            return _ops;
        }

        public Tape copy() {
            return new Tape(new ArrayList<>(_ops));
        }

        @Override public String toString() {
            return "[Tape: "+_ops+"]";
        }
    }

    public static interface Op {
        void op(TreeNode t);
    }

    public static final class TreeNode {
        private final List<TreeNode> _childs = new ArrayList<>();
        private final Tape _t;

        public TreeNode(Tape t) {
            _t = t;
        }

        public TreeNode fork() {
            _childs.add(new TreeNode(_t.copy()));
            TreeNode fork = new TreeNode(_t.copy());
            _childs.add(fork);
            return fork;
        }

        public Tape getTape() {
            return _t;
        }

        public List<TreeNode> getChildren() {
            return _childs;
        }

        public boolean isLeaf() {
            return _childs.isEmpty();
        }

        //public TreeNode<T> addChild(T t) {
            //_childs.add(new TreeNode<T>(t));
        //}
        @Override public String toString() {
            return "[TreeNode, tape: "+_t+", childs: "+_childs+"]";
        }
    }

    private TreeNode _root;


    public TreeTape(Op... root) {
        this(Arrays.asList(root));
    }

    public TreeTape(List<Op> root) {
        _root = new TreeNode(new Tape());
        //_root.getTape().append(root);
        for(Op op:root) {
            op.op(_root);
        }
    }

    public TreeNode getRoot() {
        return _root;
    }

    public List<TreeNode> getLeaves() {
        List<TreeNode> ls = new ArrayList<>();
        List<TreeNode> frontier = new LinkedList<>();
        frontier.add(_root);
        while(!frontier.isEmpty()) {
            TreeNode c = frontier.remove(0);
            if(c.isLeaf()) {
                ls.add(c);
            }
            else {
                for(TreeNode f:c.getChildren()) {
                    frontier.add(f);
                }
            }
        }
        return ls;
    }

    @Override public String toString() {
        return _root.toString();
    }
}
