package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BytesSink;

import java.util.Iterator;

public class ShaMapInner extends ShaMapNode implements Iterable<ShaMapNode> {
    public int depth;
    int slotBits = 0;
    int version = 0;
    boolean doCoW;
    protected ShaMapNode[] branches = new ShaMapNode[16];

    public ShaMapInner(int depth) {
        this(false, depth, 0);
    }

    public ShaMapInner(boolean isCopy, int depth, int version) {
        this.doCoW = isCopy;
        this.depth = depth;
        this.version = version;
    }

    protected ShaMapInner copy(int version) {
        ShaMapInner copy = makeInnerOfSameClass(depth);
        System.arraycopy(branches, 0, copy.branches, 0, branches.length);
        copy.slotBits = slotBits;
        copy.hash = hash;
        copy.version = version;
        doCoW = true;

        return copy;
    }

    protected ShaMapInner makeInnerOfSameClass(int depth) {
        return new ShaMapInner(true, depth, version);
    }

    protected ShaMapInner makeInnerChild() {
        int childDepth = depth + 1;
        if (childDepth >= 64) throw new AssertionError();
        return new ShaMapInner(doCoW, childDepth, version);
    }

    // Descend into the tree, find the leaf matching this index
    // and if the tree has it.
    protected void setLeaf(ShaMapLeaf leaf) {
        if (leaf.version == -1) {
            leaf.version = version;
        }
        setBranch(leaf.index, leaf);
    }

    private void removeBranch(Hash256 index) {
        removeBranch(selectBranch(index));
    }

    public void walkLeaves(LeafWalker leafWalker) {
        for (ShaMapNode branch : branches) {
            if (branch != null) {
                if (branch.isInner()) {
                    branch.asInner().walkLeaves(leafWalker);
                } else if (branch.isLeaf()) {
                    leafWalker.onLeaf(branch.asLeaf());
                }
            }
        }
    }

    public void walkTree(TreeWalker treeWalker) {
        treeWalker.onInner(this);
        for (ShaMapNode branch : branches) {
            if (branch != null) {
                if (branch.isLeaf()) {
                    ShaMapLeaf ln = branch.asLeaf();
                    treeWalker.onLeaf(ln);
                } else if (branch.isInner()) {
                    ShaMapInner childInner = branch.asInner();
                    childInner.walkTree(treeWalker);
                }
            }
        }

    }

    public void walkHashedTree(HashedTreeWalker walker) {
        walker.onInner(hash(), this);

        for (ShaMapNode branch : branches) {
            if (branch != null) {
                if (branch.isLeaf()) {
                    ShaMapLeaf ln = branch.asLeaf();
                    walker.onLeaf(branch.hash(), ln);
                } else if (branch.isInner()) {
                    ShaMapInner childInner = branch.asInner();
                    childInner.walkHashedTree(walker);
                }
            }
        }
    }

    /**
     * @return the `only child` leaf or null if other children
     */
    public ShaMapLeaf onlyChildLeaf() {
        ShaMapLeaf leaf = null;
        int leaves = 0;

        for (ShaMapNode branch : branches) {
            if (branch != null) {
                if (branch.isInner()) {
                    leaf = null;
                    break;
                } else if (++leaves == 1) {
                    leaf = branch.asLeaf();
                } else {
                    leaf = null;
                    break;
                }
            }
        }
        return leaf;
    }

    public boolean removeLeaf(Hash256 index) {
        PathToIndex path = pathToIndex(index);
        if (path.hasMatchedLeaf()) {
            ShaMapInner top = path.dirtyOrCopyInners();
            top.removeBranch(index);
            path.collapseOnlyLeafChildInners();
            return true;
        } else {
            return false;
        }
    }

    public ShaMapItem getItem(Hash256 index) {
        ShaMapLeaf leaf = getLeaf(index);

        @SuppressWarnings("unchecked")
        ShaMapItem shaMapItem = leaf == null ? null : leaf.item;
        return shaMapItem;
    }

    public boolean addItem(Hash256 index, ShaMapItem item) {
        return addLeaf(new ShaMapLeaf(index, item));
    }

    public boolean updateItem(Hash256 index, ShaMapItem item) {
        return updateLeaf(new ShaMapLeaf(index, item));
    }

    public boolean hasLeaf(Hash256 index) {
        return pathToIndex(index).hasMatchedLeaf();
    }

    public ShaMapLeaf getLeaf(Hash256 index) {
        PathToIndex stack = pathToIndex(index);
        if (stack.hasMatchedLeaf()) {
            return stack.leaf;
        } else {
            return null;
        }
    }

    public boolean addLeaf(ShaMapLeaf leaf) {
        PathToIndex stack = pathToIndex(leaf.index);
        if (stack.hasMatchedLeaf()) {
            return false;
        } else {
            ShaMapInner top = stack.dirtyOrCopyInners();
            top.addLeafToTerminalInner(leaf);
            return true;
        }
    }

    public boolean updateLeaf(ShaMapLeaf leaf) {
        PathToIndex stack = pathToIndex(leaf.index);
        if (stack.hasMatchedLeaf()) {
            ShaMapInner top = stack.dirtyOrCopyInners();
            // Why not update in place? Because of structural sharing
            top.setLeaf(leaf);
            return true;
        } else {
            return false;
        }
    }

    public PathToIndex pathToIndex(Hash256 index) {
        return new PathToIndex(this, index);
    }

    /**
     * This should only be called on the deepest inners, as it
     * does not do any dirtying.
     * @param leaf to add to inner
     */
    void addLeafToTerminalInner(ShaMapLeaf leaf) {
        ShaMapNode branch = getBranch(leaf.index);
        if (branch == null) {
            setLeaf(leaf);
        } else if (branch.isInner()) {
            // This should never be called
            throw new AssertionError();
        } else if (branch.isLeaf()) {
            ShaMapInner inner = makeInnerChild();
            setBranch(leaf.index, inner);
            inner.addLeafToTerminalInner(leaf);
            inner.addLeafToTerminalInner(branch.asLeaf());
        }
    }

    protected void setBranch(Hash256 index, ShaMapNode node) {
        setBranch(selectBranch(index), node);
    }

    protected ShaMapNode getBranch(Hash256 index) {
        return getBranch(index.nibblet(depth));
    }

    public ShaMapNode getBranch(int i) {
        return branches[i];
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

    private void setBranch(int slot, ShaMapNode node) {
        slotBits = slotBits | (1 << slot);
        branches[slot] = node;
        invalidate();
    }

    private void removeBranch(int slot) {
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

    public ShaMapLeaf getLeafForUpdating(Hash256 leaf) {
        PathToIndex path = pathToIndex(leaf);
        if (path.hasMatchedLeaf()) {
            return path.invalidatedPossiblyCopiedLeafForUpdating();
        }
        return null;
    }

    @Override
    public Iterator<ShaMapNode> iterator() {
        return new Iterator<ShaMapNode>() {
            int ix = 0;

            @Override
            public boolean hasNext() {
                return ix != 16;
            }

            @Override
            public ShaMapNode next() {
                return branch(ix++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public int branchCount() {
        int populated = 0;
        for (ShaMapNode branch : branches) {
            if (branch != null) {
                populated ++;
            }
        }
        return populated;
    }
}
