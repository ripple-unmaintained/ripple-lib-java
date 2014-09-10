package com.ripple.core.types.shamap2;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BytesSink;

public class ShaMapLeaf extends ShaMapNode {
    public Hash256 index;
    public ShaMapItem item;

    protected ShaMapLeaf(Hash256 index, ShaMapItem item) {
        this.index = index;
        this.item = item;
    }

    @Override public boolean isLeaf() {return true;}
    @Override public boolean isInner() {return false;}

    @Override
    Prefix hashPrefix() {
        return item.hashPrefix();
    }

    @Override
    public void toBytesSink(BytesSink sink) {
        item.toBytesSink(sink);
        index.toBytesSink(sink);
    }
}
