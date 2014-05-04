package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.serialized.BytesSink;

import javax.xml.soap.Node;

public class ShaMapLeafNode extends ShaMapNode {
    Hash256 index;
    private Item blob;

    public ShaMapLeafNode() {
    }

    public Item getBlob() {
        return blob;
    }

    public void setBlob(Item blob) {
        this.blob = blob;
    }

    public interface Item {
        public byte[] bytes();
        // TODO
//        public void toBytesSink(BytesSink to);
    }

    @Override
    public Hash256 hash() {
        Hash256.HalfSha512 half = new Hash256.HalfSha512();
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

    public ShaMapLeafNode(Hash256 index, NodeType type, Item blob) {
        this.index = index;
        this.type = type;
        this.setBlob(blob);
    }
}
