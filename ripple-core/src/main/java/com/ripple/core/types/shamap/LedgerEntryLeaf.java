package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.types.known.sle.LedgerEntry;

public class LedgerEntryLeaf extends ShaMapLeafNode {
    public LedgerEntry le;

    public LedgerEntryLeaf(LedgerEntry so) {
        this.index = so.index();
        this.le = so;
    }

    @Override
    public Hash256 hash() {
        HalfSha512 half = new HalfSha512();
        half.update(HashPrefix.leafNode);
        le.toBytesSink(half);
        half.update(index);
        return half.finish();
    }
}
