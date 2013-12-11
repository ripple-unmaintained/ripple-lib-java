package com.ripple.core.types.shamap;

import com.ripple.core.types.hash.Hash256;

public class ShaMapLeafNode extends ShaMapNode {
    Hash256 index;
    Item blob;

    public interface Item {
        public byte[] bytes();
    }

    @Override
    public Hash256 hash() {
        switch (type) {
            case tnTRANSACTION_NM:
                return index;
            case tnTRANSACTION_MD:
                Hash256.HalfSha512 half = new Hash256.HalfSha512();
                half.update(Hash256.HASH_PREFIX_TX_NODE);
                half.update(blob.bytes());
                half.update(index);
                return half.finish();
            default:
                throw new UnsupportedOperationException("Currently only support transaction leaf nodes");
        }
    }

    public ShaMapLeafNode(Hash256 index, NodeType type, Item blob) {
        this.index = index;
        this.type = type;
        this.blob = blob;
    }
}
