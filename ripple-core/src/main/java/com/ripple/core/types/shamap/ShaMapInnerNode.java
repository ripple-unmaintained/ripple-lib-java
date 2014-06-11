package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.types.known.sle.LedgerEntry;

import java.util.ArrayDeque;

public class ShaMapInnerNode extends ShaMapNode {
    public ShaMapNode[] branches;
    protected int slotBits = 0;
    public int depth;
    public Hash256 hash;

    protected ShaMapInnerNode(int node_depth) {
        branches = new ShaMapNode[16];
        type = NodeType.tnINNER;
        hashingPrefix = HashPrefix.innerNode;
        depth = node_depth;
    }

    public Hash256 hash() {
        if (!needsHashing()) {
            return hash;
        }

        if (empty()) {
            return Hash256.ZERO_256;
        }

        HalfSha512 hasher = new HalfSha512();
        hasher.update(hashingPrefix);

        int fullBranches = 0;
        for (ShaMapNode node : branches) {
            if (node != null) {
                Hash256 hash = node.hash();
                hasher.update(hash);
                fullBranches++;
            } else {
                hasher.update(Hash256.ZERO_256);
            }
        }

        hash = hasher.finish();
        onHash(hash, fullBranches);
        return hash;
    }

    public void onHash(Hash256 hash, int fullBranches) {

    }

    public boolean needsHashing() {
        return hash == null;
    }

    protected void setNode(int slot, ShaMapNode node) {
        slotBits = slotBits | (1 << slot);
        branches[slot] = node;
    }

    protected void invalidate() {
        hash = null;
    }

    @SuppressWarnings("unused")
    protected void removeNode(int slot) {
        branches[slot] = null;
        slotBits = slotBits & ~(1 << slot);
    }

    public boolean empty() {
        return slotBits == 0;
    }

    public ShaMapLeafNode getFirstLeaf(Hash256 id, boolean invalidating) {
        int ix = id.nibblet(depth);
        ShaMapNode existing = branches[ix];
        if (invalidating) {
            invalidate();
        }
        if (existing == null) {
            return null;
        }
        else if (existing instanceof ShaMapLeafNode) {
            return (ShaMapLeafNode) existing;
        } else {
            return ((ShaMapInnerNode) existing).getFirstLeaf(id, invalidating);
        }
    }

    public ShaMapLeafNode getLeafForUpdating(Hash256 id) {
        ShaMapLeafNode leaf = getFirstLeaf(id, true);
        if (!leaf.index.equals(id)) {
            throw new IllegalStateException("Invalidated the tree, and the leaf wasn't there. TODO: better API");
        }
        return leaf;
    }

    public ShaMapLeafNode getLeaf(Hash256 id) {
        ShaMapLeafNode leaf = getFirstLeaf(id, false);
        if (leaf != null && !leaf.index.equals(id)) {
            // This is not the leaf we requested ;)
            leaf = null;
        }
        return leaf;
    }

    protected void addLeaf(Hash256 index, ShaMapLeafNode leaf) {
        int ix = index.nibblet(depth);
        ShaMapNode existing = branches[ix];
        invalidate();

        if (existing == null) {
            setNode(ix, leaf);
        } else if (existing instanceof ShaMapLeafNode) {
            ShaMapLeafNode existingLeaf = (ShaMapLeafNode) existing;
            if (existingLeaf.index.equals(index)) {
                throw new UnsupportedOperationException("Tried to add node already in tree!");
            } else {
                ShaMapInnerNode container = makeInnerChild();
                container.addLeaf(existingLeaf.index, existingLeaf);
                container.addLeaf(index, leaf);
                setNode(ix, container);
            }
        } else {
            ShaMapInnerNode existingInner = (ShaMapInnerNode) existing;
            existingInner.addLeaf(index, leaf);
        }
    }

    protected ShaMapInnerNode makeInnerChild() {
        return new ShaMapInnerNode(depth + 1);
    }

    public void addItem(Hash256 index, NodeType nodeType, ShaMapLeafNode.Item blob) {
        addLeaf(index, new ShaMapLeafNode(index, nodeType, blob));
    }

    public int selectBranch(Hash256 id) {
        return id.nibblet(depth);
    }
    public boolean branchIsEmpty(int i) {
        return (branches[i] == null);
    }
    public ShaMapNode getBranch(int i) {
        return branches[i];
    }

    public ArrayDeque<ShaMapNode> walkStackTo(Hash256 id, boolean includeNonMatchingLeaf) {
        ArrayDeque<ShaMapNode> stack = new ArrayDeque<ShaMapNode>();
        ShaMapInnerNode top = this;

        while (true) {
            stack.push(top);
            int ix = top.selectBranch(id);
            ShaMapNode existing = top.getBranch(ix);
            if (existing == null) {
                break;
            } else if (existing instanceof ShaMapLeafNode) {
                ShaMapLeafNode leaf = (ShaMapLeafNode) existing;
                if (includeNonMatchingLeaf || leaf.index.equals(id)) {
                    stack.push(existing);
                }
                break;
            }
            else if (existing instanceof ShaMapInnerNode) {
                top = (ShaMapInnerNode) existing;
            }
        }
        return stack;
    }

    /**
     * @param id index returned is relative to this
     * @param end the largest index
     * @return An index where id < index <= end if found, or null
     */
    public Hash256 getNextIndex(Hash256 id, Hash256 end) {
        Hash256 nextIndex = getNextIndex(id);

        if (nextIndex != null) {
            if (nextIndex.compareTo(id) != 1) throw new AssertionError();
        }

        if (nextIndex != null && nextIndex.compareTo(end) > -1) {
            nextIndex = null;
        }
        return nextIndex;
    }

    public Hash256 getNextIndex(Hash256 id) {
        ArrayDeque<ShaMapNode> stack = walkStackTo(id, true);

        while (stack.size() > 0) {
            ShaMapNode node = stack.pop();
            if (node instanceof ShaMapLeafNode) {
                ShaMapLeafNode leaf = (ShaMapLeafNode) node;
                if (leaf.index.compareTo(id) == 1) {
                    return leaf.index;
                }
            }
            else if (node instanceof ShaMapInnerNode) {
                ShaMapInnerNode inner = (ShaMapInnerNode) node;
                for (int ix = inner.selectBranch(id) + 1; ix < 16; ix++) {
                    if (!inner.branchIsEmpty(ix)) {
                        ShaMapLeafNode ret = inner.getBranch(ix).firstLeafBelow();
                        if (ret != null) {
                            return ret.index;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean hasLeaf(Hash256 hash) {
        return getLeaf(hash) != null;
    }
}
