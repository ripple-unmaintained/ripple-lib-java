package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.Prefix;

abstract public class ShaMapNode {
    protected ShaMapNode(){}

    public NodeType type;
    public Prefix hashingPrefix;
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
