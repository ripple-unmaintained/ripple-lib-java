package com.ripple.core.types.shamap;

import com.ripple.core.types.hash.Hash256;

public class ShaMapLeafNode extends ShaMapInnerNode {
    Item blob;

    public interface Item {
        public byte[] bytes();
    }

    @Override
    public Hash256 hash() {
        switch (type) {
            case tnTRANSACTION_NM:
                return id;
            case tnTRANSACTION_MD:
                Hash256.HalfSha512 half = new Hash256.HalfSha512();
                half.update(Hash256.HASH_PREFIX_TX_NODE);
                half.update(blob.bytes());
                half.update(id);
                return half.finish();
            default:
                throw new UnsupportedOperationException("Currently only support transaction leaf nodes");
        }
    }

    public ShaMapLeafNode(Hash256 id, int depth, NodeType type, Item blob) {
        super(id, depth, false);
        this.type = type;
        this.blob = blob;
    }
}
