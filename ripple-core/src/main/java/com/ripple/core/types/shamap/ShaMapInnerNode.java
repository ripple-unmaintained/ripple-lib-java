package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;

public class ShaMapInnerNode extends ShaMapNode {
    public static final Hash256 ZERO_256 = new Hash256(new byte[32]);
    public ShaMapNode[] branches;
    protected int slotBits = 0;
    protected int depth;

    public Hash256 hash;

    protected ShaMapInnerNode(int node_depth) {
        branches = new ShaMapNode[16];
        type = NodeType.tnINNER;
        depth = node_depth;
    }

    public Hash256 hash() {
        if (!needsHashing()) {
            return hash;
        }

        if (empty()) {
            return ZERO_256;
        }

        Hash256.HalfSha512 hasher = new Hash256.HalfSha512();
        hasher.update(Hash256.HASH_PREFIX_INNER_NODE);

        int fullBranches = 0;
        for (ShaMapNode node : branches) {
            if (node != null) {
                Hash256 hash = node.hash();
                hasher.update(hash);
                fullBranches++;
            } else {
                hasher.update(ZERO_256);
            }
        }

        hash = hasher.finish();
        onHash(hash, fullBranches);
        return hash;
    }

    public void onHash(Hash256 hash, int fullBranches) {
        //TODO: clean configurable instrumentation
//        hashes.put(hash, fullBranches);
    }

    protected boolean needsHashing() {
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

    protected ShaMapLeafNode getLeaf(Hash256 id, boolean invalidating) {
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
            return ((ShaMapInnerNode) existing).getLeaf(id, invalidating);
        }
    }

    public ShaMapLeafNode getLeafForUpdating(Hash256 id) {
        return getLeaf(id, true);
    }

    public ShaMapLeafNode getLeafFor(Hash256 id) {
        return getLeaf(id, false);
    }

    private void addLeaf(Hash256 index, ShaMapLeafNode leaf) {
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

    public ShaMapInnerNode makeInnerChild() {
        return new ShaMapInnerNode(depth + 1);
    }

    public void addItem(Hash256 index, NodeType nodeType, ShaMapLeafNode.Item blob) {
        addLeaf(index, new ShaMapLeafNode(index, nodeType, blob));
    }
}
