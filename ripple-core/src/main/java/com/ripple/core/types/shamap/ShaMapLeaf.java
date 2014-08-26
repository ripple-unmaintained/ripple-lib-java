package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;

public class ShaMapLeaf extends ShaMapNode {
    public Hash256 index;
    private Item blob;

    public ShaMapLeaf() {
    }

    public Item getBlob() {
        return blob;
    }

    public void setBlob(Item blob) {
        this.blob = blob;
    }

    public interface Item {
        public byte[] bytes();
    }

    public void copyItemFrom(ShaMapLeaf other) {
        setBlob(other.getBlob());
    }

    @Override
    public Hash256 hash() {
        HalfSha512 half = new HalfSha512();
        HashPrefix prefix;

        if (type == NodeType.tnTRANSACTION_MD)
            prefix = HashPrefix.txNode;
        else if (type == NodeType.tnACCOUNT_STATE)
            prefix = HashPrefix.leafNode;
        else
            throw new UnsupportedOperationException("No support for " + type);

        half.update(prefix.bytes);
        half.update(getBlob().bytes());
        half.update(index);

        return half.finish();
    }

    public ShaMapLeaf(Hash256 index, NodeType type, Item blob) {
        this.index = index;
        this.type = type;
        this.setBlob(blob);
    }
}
