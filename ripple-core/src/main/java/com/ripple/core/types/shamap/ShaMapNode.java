package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BytesSink;

abstract public class ShaMapNode {
    protected Hash256 hash;

    // This saves a lot of instanceof checks
    public abstract boolean isLeaf();
    public abstract boolean isInner();

    public ShaMapLeaf asLeaf() {
        return (ShaMapLeaf) this;
    }
    public ShaMapInner asInner() {
        return (ShaMapInner) this;
    }

    abstract Prefix hashPrefix();
    abstract public void toBytesSink(BytesSink sink);

    public void invalidate() {hash = null;}
    public Hash256 hash() {
        if (hash == null) {
            hash = createHash();
        }
        return hash;
    }
    public Hash256 createHash() {
        HalfSha512 half = HalfSha512.prefixed256(hashPrefix());
        toBytesSink(half);
        return half.finish();
    }
    /**
     * Walk any leaves, possibly this node itself, if it's terminal.
     */
    public void walkAnyLeaves(LeafWalker leafWalker) {
        if (isLeaf()) {
            leafWalker.onLeaf(asLeaf());
        } else {
            asInner().walkLeaves(leafWalker);
        }
    }
}
