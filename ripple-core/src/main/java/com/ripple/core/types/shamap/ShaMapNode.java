package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.Prefix;

abstract public class ShaMapNode {
    protected ShaMapNode(){}

    public NodeType type;
    public Prefix hashingPrefix;
    abstract public Hash256 hash();

    ShaMapLeafNode firstLeafBelow() {
        ShaMapNode node = this;

        do {
            if (node instanceof ShaMapLeafNode) {
                return (ShaMapLeafNode) node;
            }

            ShaMapInnerNode innerNode = (ShaMapInnerNode) node;
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

    public static enum NodeType
    {
        tnERROR,
        tnINNER,
        tnTRANSACTION_NM, // transaction, no metadata
        tnTRANSACTION_MD, // transaction, with metadata
        tnACCOUNT_STATE
    }
}
