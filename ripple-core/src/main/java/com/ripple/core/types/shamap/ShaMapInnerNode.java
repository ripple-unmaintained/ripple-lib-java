package com.ripple.core.types.shamap;

import com.ripple.core.types.hash.Hash256;

public class ShaMapInnerNode extends ShaMapNode {
    public static final Hash256 ZERO_256 = new Hash256(new byte[32]);
    ShaMapNode[] branches;
    private int slotBits = 0;
    int depth;

    protected ShaMapInnerNode(int node_depth) {
        branches = new ShaMapNode[16];
        type = NodeType.tnINNER;
        depth = node_depth;
    }

    public Hash256 hash() {
        if (empty()) {
            return ZERO_256;
        }

        Hash256.HalfSha512 hasher = new Hash256.HalfSha512();
        hasher.update(Hash256.HASH_PREFIX_INNER_NODE);

        for (ShaMapNode node : branches) {
            if (node != null) {
                Hash256 hash = node.hash();
                hasher.update(hash);
            }  else {
                hasher.update(ZERO_256);
            }
        }

        return hasher.finish();
    }

    protected void setNode(int slot, ShaMapNode node) {
        slotBits = slotBits | (1 << slot);
        branches[slot] = node;
    }

    @SuppressWarnings("unused")
    protected void removeNode(int slot) {
        branches[slot] = null;
        slotBits = slotBits & ~(1 << slot);
    }

    public boolean empty() {
        return slotBits == 0;
    }

    private void addLeaf(Hash256 id, NodeType nodeType, ShaMapLeafNode.Item blob, ShaMapLeafNode moved) {
        int ix = id.nibblet(depth);
        ShaMapNode existing = branches[ix];

        if (existing == null) {
            ShaMapLeafNode node;
            if (moved == null) {
                node = new ShaMapLeafNode(id, nodeType, blob);
            } else {
                node = moved;
            }
            setNode(ix, node);
        } else if (existing instanceof ShaMapLeafNode) {
            ShaMapLeafNode existingLeaf = (ShaMapLeafNode) existing;
            if (existingLeaf.index.equals(id)) {
                throw new UnsupportedOperationException("Tried to add node already in tree!");
            } else {
                ShaMapInnerNode container = new ShaMapInnerNode(depth + 1);
                container.addLeaf(existingLeaf.index, existingLeaf);
                container.addLeaf(id, nodeType, blob);
                setNode(ix, container);
            }
        } else {
            ((ShaMapInnerNode) existing).addLeaf(id, nodeType, blob);
        }
    }
    private void addLeaf(Hash256 id, ShaMapLeafNode existingLeaf) {
        addLeaf(id, null, null, existingLeaf);
    }
    public void addLeaf(Hash256 id, NodeType nodeType, ShaMapLeafNode.Item blob) {
        addLeaf(id, nodeType, blob, null);
    }
}
