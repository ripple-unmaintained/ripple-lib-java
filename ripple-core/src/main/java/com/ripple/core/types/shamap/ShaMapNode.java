package com.ripple.core.types.shamap;

import com.ripple.core.types.hash.Hash256;

abstract public class ShaMapNode {
    protected ShaMapNode(){}

    public NodeType type;
    abstract public Hash256 hash();

    public static enum NodeType
    {
        tnERROR,
        tnINNER,
        tnTRANSACTION_NM, // transaction, no metadata
        tnTRANSACTION_MD, // transaction, with metadata
        tnACCOUNT_STATE
    }
}
