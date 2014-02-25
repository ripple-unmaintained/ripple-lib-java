package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;

public class ShaMapLeafNode extends ShaMapNode {
    Hash256 index;
    private Item blob;

    public Item getBlob() {
        return blob;
    }

    public void setBlob(Item blob) {
        this.blob = blob;
    }

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
                half.update(HashPrefix.txNode.bytes);
                half.update(getBlob().bytes());
                half.update(index);
                return half.finish();
            default:
                throw new UnsupportedOperationException("Currently only support transaction leaf nodes");
        }
    }

    public ShaMapLeafNode(Hash256 index, NodeType type, Item blob) {
        this.index = index;
        this.type = type;
        this.setBlob(blob);
    }
}
