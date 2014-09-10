package com.ripple.core.types.shamap2;

import com.ripple.core.coretypes.hash.Hash;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BytesSink;

import java.util.ArrayDeque;
import java.util.Iterator;

public class ShaMapInner extends ShaMapNode {
    private ShaMapNode[] branches = new ShaMapNode[16];
    // Toying with this idea
//    private Hash256[] hashes;

    public int depth;
    boolean doCoW;
    public int slotBits = 0;
    public int version = 0;

    public ShaMapInner(int depth) {
        this(false, depth, 0);
    }

    public ShaMapInner(boolean isCopy, int depth, int version) {
        this.doCoW = isCopy;
        this.depth = depth;
        this.version = version;
    }

    protected ShaMapInner copy(int version) {
        ShaMapInner copy = copyInner(depth);
        System.arraycopy(branches, 0, copy.branches, 0, branches.length);
        copy.slotBits = slotBits;
        copy.hash = hash;
        copy.version = version;
        doCoW = true;

        return copy;
    }

    protected ShaMapInner copyInner(int depth) {
        return new ShaMapInner(true, depth, version);
    }

    protected ShaMapInner makeInnerChild() {
        return new ShaMapInner(doCoW, depth + 1, version);
    }

    // Descend into the tree, find the leaf matching this index
    // and if the tree has it.
    private void setLeaf(ShaMapLeaf leaf) {
        setBranch(leaf.index, leaf);
    }

    private void removeBranch(Hash256 index) {
        removeBranch(selectBranch(index));
    }

    public static class PathToIndex {
        Hash256 index;
        ArrayDeque<ShaMapInner> inners;
        ShaMapInner[] dirtied;

        ShaMapLeaf leaf = null;
        boolean matched = false;

        public boolean hasLeaf() {
            return leaf != null;
        }

        public boolean leafMatchedIndex() {
            return matched;
        }

        // returns the
        public ShaMapInner dirtyOrCopyInners() {
            if (maybeCopyOnWrite()) {
                int ix = 0;
                // We want to make a uniformly accessed array of the inners
                dirtied = new ShaMapInner[inners.size()];
                // from depth 0 to 1, to 2, to 3, don't be fooled by the api
                Iterator<ShaMapInner> it = inners.descendingIterator();

                // This is actually the root which COULD be the top of the stack
                // Think about it ;)
                ShaMapInner top = it.next();
                dirtied[ix++] = top;
                top.invalidate();
                // we set this to true, but we may
                boolean doCopies = true;

                while (it.hasNext()) {
                    ShaMapInner next = it.next();
                    if (ix == 1) {
                        doCopies = next.version != top.version;
                    }

                    if (doCopies) {
                        ShaMapInner copy = next.copy(top.version);
                        copy.invalidate();
                        top.setBranch(index, copy);
                        next = copy;
                    } else {
                        next.invalidate();
                    }
                    top = next;
                    dirtied[ix++] = top;
                }
                return top;
            } else {
                copyInnersToDirtiedArray();
                return inners.peekFirst();
            }
        }

        // So can be used by makeValid etc
        private void copyInnersToDirtiedArray() {
            int ix = 0;
            dirtied = new ShaMapInner[inners.size()];
            Iterator<ShaMapInner> descending = inners.descendingIterator();
            while (descending.hasNext()) {
                dirtied[ix++] = descending.next();
            }
        }

        private boolean maybeCopyOnWrite() {
            return inners.peekLast().doCoW;
        }

        public PathToIndex(ShaMapInner root, Hash256 index) {
            this.index = index;
            inners = makeStack(root, index);
        }

        private ArrayDeque<ShaMapInner> makeStack(ShaMapInner root, Hash256 index) {
            ArrayDeque<ShaMapInner> inners = new ArrayDeque<ShaMapInner>();
            ShaMapInner top = root;

            while (true) {
                inners.push(top);
                ShaMapNode existing = top.getBranch(index);
                if (existing == null) {
                    break;
                } else if (existing.isLeaf()) {
                    leaf = existing.asLeaf();
                    matched = leaf.index.equals(index);
                    break;
                }
                else if (existing.isInner()) {
                    top = existing.asInner();
                }
            }
            return inners;
        }

        public void collapseSingleLeafInners() {
            ShaMapInner next;
            ShaMapLeaf singleLeaf = null;

            for (int i = dirtied.length - 1; i >= 0; i--) {
                next = dirtied[i];
                if (singleLeaf != null) {
                    next.setBranch(singleLeaf.index, singleLeaf);
                }
                singleLeaf = next.singleLeaf();
                if (singleLeaf == null) {
                    break;
                }
            }
        }
    }

    public interface TreeWalker {
        public void onLeaf(Hash256 h, ShaMapLeaf le);
        public void onInner(Hash256 h, ShaMapInner inner);
    }

    public void walkTree(TreeWalker walker) {
        walker.onInner(hash(), this);

        for (ShaMapNode branch : branches) {
            if (branch == null) {
                continue;
            }
            if (branch.isLeaf()) {
                ShaMapLeaf ln = branch.asLeaf();
                walker.onLeaf(branch.hash(), ln);
            } else {
                ShaMapInner childInner = branch.asInner();
                childInner.walkTree(walker);
            }
        }
    }

    private ShaMapLeaf singleLeaf() {
        ShaMapLeaf leaf = null;
        int leaves = 0;
        for (ShaMapNode branch : branches) {
            if (branch.isLeaf()) {
                if (++leaves == 1) {
                    leaf = branch.asLeaf();
                } else {
                    leaf = null;
                }
            }
        }
        return leaf;
    }

    public boolean removeItem(Hash256 index) {
        PathToIndex stack = pathToIndex(index);
        if (stack.hasLeaf() && stack.leafMatchedIndex()) {
            ShaMapInner top = stack.dirtyOrCopyInners();
            top.removeBranch(index);
            stack.collapseSingleLeafInners();
            return true;
        } else {
            return false;
        }
    }

    public boolean hasLeaf(Hash256 index) {
        PathToIndex stack = pathToIndex(index);
        return stack.hasLeaf() && stack.leafMatchedIndex();
    }
    public ShaMapLeaf getLeaf(Hash256 index) {
        PathToIndex stack = pathToIndex(index);
        if (stack.hasLeaf() && stack.leafMatchedIndex()) {
            return stack.leaf;
        } else {
            return null;
        }
    }

    public boolean addItem(Hash256 index, ShaMapItem item) {
        PathToIndex stack = pathToIndex(index);
        if (stack.hasLeaf() && stack.leafMatchedIndex()) {
            return false;
        } else {
            ShaMapInner top = stack.dirtyOrCopyInners();
            top.addLeaf(new ShaMapLeaf(index, item));
            return true;
        }
    }

    public PathToIndex pathToIndex(Hash256 index) {
        return new PathToIndex(this, index);
    }

    public boolean updateItem(Hash256 index, ShaMapItem item) {
        PathToIndex stack = pathToIndex(index);
        if (stack.hasLeaf() && stack.leafMatchedIndex()) {
            ShaMapInner top = stack.dirtyOrCopyInners();
            top.setLeaf(new ShaMapLeaf(index, item));
            return true;
        } else {
            return false;
        }
    }

    public void addLeaf(ShaMapLeaf leaf) {
        ShaMapNode branch = getBranch(leaf.index);
        if (branch == null) {
            setLeaf(leaf);
        } else if (branch instanceof ShaMapInner) {
            ((ShaMapInner) branch).addLeaf(leaf);
        } else if (branch instanceof ShaMapLeaf) {
            ShaMapInner inner = makeInnerChild();
            setBranch(leaf.index, inner);
            inner.addLeaf(leaf);
            inner.addLeaf((ShaMapLeaf) branch);
        }
    }

    protected void setBranch(Hash256 index, ShaMapNode node) {
        setBranch(selectBranch(index), node);
    }

    protected ShaMapNode getBranch(Hash256 index) {
        return branches[index.nibblet(depth)];
    }

    protected int selectBranch(Hash256 index) {
        return index.nibblet(depth);
    }

    public boolean hasLeaf(int i) {
        return branches[i].isLeaf();
    }
    public boolean hasInner(int i) {
        return branches[i].isInner();
    }

    protected void setBranch(int slot, ShaMapNode node) {
        slotBits = slotBits | (1 << slot);
        branches[slot] = node;
        invalidate();
    }

    @SuppressWarnings("unused")
    public void removeBranch(int slot) {
        branches[slot] = null;
        slotBits = slotBits & ~(1 << slot);
    }
    public boolean empty() {
        return slotBits == 0;
    }

    @Override public boolean isLeaf() { return false; }
    @Override public boolean isInner() { return true; }

    @Override
    Prefix hashPrefix() {
        return HashPrefix.innerNode;
    }

    @Override
    public void toBytesSink(BytesSink sink) {
        for (ShaMapNode branch : branches) {
            if (branch != null) {
                branch.hash().toBytesSink(sink);
            } else {
                Hash256.ZERO_256.toBytesSink(sink);
            }
        }
    }
}
