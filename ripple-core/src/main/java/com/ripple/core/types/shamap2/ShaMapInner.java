package com.ripple.core.types.shamap2;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BytesSink;

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
        int childDepth = depth + 1;
        if (childDepth >= 64) throw new AssertionError();
        return new ShaMapInner(doCoW, childDepth, version);
    }

    // Descend into the tree, find the leaf matching this index
    // and if the tree has it.
    private void setLeaf(ShaMapLeaf leaf) {
        setBranch(leaf.index, leaf);
    }

    private void removeBranch(Hash256 index) {
        removeBranch(selectBranch(index));
    }

    public interface HashedTreeWalker {
        public void onLeaf(Hash256 h, ShaMapLeaf le);
        public void onInner(Hash256 h, ShaMapInner inner);
    }

    public void walkHashedTree(HashedTreeWalker walker) {
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
                childInner.walkHashedTree(walker);
            }
        }
    }

    ShaMapLeaf singleLeaf() {
        ShaMapLeaf leaf = null;
        int leaves = 0;
        for (ShaMapNode branch : branches) {
            if (branch != null && branch.isLeaf()) {
                if (++leaves == 1) {
                    leaf = branch.asLeaf();
                } else {
                    leaf = null;
                }
            }
        }
        return leaf;
    }

    public boolean removeLeaf(Hash256 index) {
        PathToIndex stack = pathToIndex(index);
        if (stack.hasMatchedLeaf()) {
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
        return stack.hasMatchedLeaf();
    }
    public ShaMapLeaf getLeaf(Hash256 index) {
        PathToIndex stack = pathToIndex(index);
        if (stack.hasMatchedLeaf()) {
            return stack.leaf;
        } else {
            return null;
        }
    }

    public boolean addItem(Hash256 index, ShaMapItem item) {
        PathToIndex stack = pathToIndex(index);
        if (stack.hasMatchedLeaf()) {
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
        if (stack.hasMatchedLeaf()) {
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
        } else if (branch.isInner()) {
            branch.asInner().addLeaf(leaf);
        } else if (branch.isLeaf()) {
            ShaMapInner inner = makeInnerChild();
            setBranch(leaf.index, inner);
            inner.addLeaf(leaf);
            inner.addLeaf(branch.asLeaf());
        }
    }

    protected void setBranch(Hash256 index, ShaMapNode node) {
        setBranch(selectBranch(index), node);
    }

    protected ShaMapNode getBranch(Hash256 index) {
        return branches[index.nibblet(depth)];
    }

    public ShaMapNode branch(int i) {
        return branches[i];
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
    public boolean hasNone(int i) {return branches[i] == null;}

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

    @Override
    public Hash256 hash() {
        if (empty()) {
            // empty inners have a hash of all ZERO
            // it's only valid for a root node to be empty
            // any other inner node, must contain at least a
            // single leaf
            assert depth == 0;
            return Hash256.ZERO_256;
        } else {
            // hash the hashPrefix() and toBytesSink
            return super.hash();
        }
    }
}
