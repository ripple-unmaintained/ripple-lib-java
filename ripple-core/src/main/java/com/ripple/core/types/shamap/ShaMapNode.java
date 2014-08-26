package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.Prefix;

abstract public class ShaMapNode {
    protected ShaMapNode(){}

    public NodeType type;
    public Prefix hashingPrefix;
    abstract public Hash256 hash();

    ShaMapLeaf firstLeafBelow() {
        ShaMapNode node = this;

        do {
            if (node instanceof ShaMapLeaf) {
                return (ShaMapLeaf) node;
            }

            ShaMapInner innerNode = (ShaMapInner) node;
            boolean foundNode = false;

            for (int i = 0; i < 16; ++i)
                if (!innerNode.branchIsEmpty(i)) {
                    node = innerNode.getBranch(i);
                    foundNode = true;
                    break;
                }

            if (!foundNode)
                return null;

        } while (true);

    }

    /**
     * Walk any leaves, possibly this node itself, if it's terminal.
     */
    public void walkAnyLeaves(LeafWalker leafWalker) {
        if (this instanceof ShaMapLeaf) {
            leafWalker.onLeaf((ShaMapLeaf) this);

        } else {
            ((ShaMapInner) this).walkItems(leafWalker);
        }
    }

    public static enum NodeType
    {
        tnERROR,
        tnINNER,
        tnTRANSACTION_NM, // transaction, no metadata
        tnTRANSACTION_MD, // transaction, with metadata
        tnACCOUNT_STATE
    }
}
