package com.ripple.core.types.shamap;

import com.ripple.core.types.hash.Hash256;

public class ShaMapInnerNode {
    public static final Hash256 ZERO_256 = new Hash256(new byte[32]);
    public static enum NodeType
    {
        tnERROR,
        tnINNER,
        tnTRANSACTION_NM,//   = 2, // transaction, no metadata
        tnTRANSACTION_MD, //    = 3, // transaction, with metadata
        tnACCOUNT_STATE//     = 4
    }

    ShaMapInnerNode[] branches;
    Hash256 id;
    int depth;
    private int slotBits = 0;
    NodeType type = NodeType.tnINNER;

    protected ShaMapInnerNode(Hash256 id, int depth, boolean has_leaves) {
        this.id = id;
        this.depth = depth;
        if (has_leaves) {
            branches = new ShaMapInnerNode[16];
        }
    }

    public ShaMapInnerNode(Hash256 id, int depth) {
        this(id, depth, true);
    }

    public Hash256 hash() {
        if (empty()) {
            return ZERO_256;
        }

        Hash256.HalfSha512 hasher = new Hash256.HalfSha512();
        hasher.update(Hash256.HASH_PREFIX_INNER_NODE);

        for (int i = 0; i < 16; i++) {
            ShaMapInnerNode node = branches[i];
            if (node != null) {
                Hash256 hash = node.hash();
                hasher.update(hash);
            }  else {
                hasher.update(ZERO_256);
            }
        }
        return hasher.finish();
    }

    protected void setNode(int slot, ShaMapInnerNode node) {
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
        ShaMapInnerNode existing = branches[ix];

        if (existing == null) {
            ShaMapLeafNode node;
            if (moved == null) {
                node = new ShaMapLeafNode(id, depth, nodeType, blob);
            } else {
                node = moved;
                node.depth = depth;
            }
            setNode(ix, node);
        } else if (existing instanceof ShaMapLeafNode) {
            if (existing.id.equals(id)) {
                throw new UnsupportedOperationException("Tried to add node already in tree!");
            } else {
                ShaMapLeafNode existingLeaf = (ShaMapLeafNode) existing;
                ShaMapInnerNode container = new ShaMapInnerNode(existing.id, depth + 1);
                container.addLeaf(existing.id, existingLeaf);
                container.addLeaf(id, nodeType, blob);
                setNode(ix, container);
            }
        } else {
            existing.addLeaf(id, nodeType, blob);
        }
    }

    public void addLeaf(Hash256 id, ShaMapLeafNode existingLeaf) {
        addLeaf(id, null, null, existingLeaf);
    }

    public void addLeaf(Hash256 id, NodeType nodeType, ShaMapLeafNode.Item blob) {
        addLeaf(id, nodeType, blob, null);
    }

}
