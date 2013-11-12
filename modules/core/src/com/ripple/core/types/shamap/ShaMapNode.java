package com.ripple.core.types.shamap;

import com.ripple.core.types.hash.Hash256;

abstract public class ShaMapNode {
    protected NodeType type;
    protected ShaMapNode(){}
    abstract public Hash256 hash();
    public static enum NodeType
    {
        tnERROR,
        tnINNER,
        tnTRANSACTION_NM,//   = 2, // transaction, no metadata
        tnTRANSACTION_MD, //    = 3, // transaction, with metadata
        tnACCOUNT_STATE//     = 4
    }
}
