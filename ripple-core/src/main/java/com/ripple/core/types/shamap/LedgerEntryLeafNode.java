package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.types.known.sle.LedgerEntry;

public class LedgerEntryLeafNode extends ShaMapLeafNode {
    LedgerEntry sle;

    public LedgerEntryLeafNode(LedgerEntry so) {
        this.index = so.index();
        this.sle = so;
    }

    @Override
    public Hash256 hash() {
        Hash256.HalfSha512 half = new Hash256.HalfSha512();
        half.update(HashPrefix.leafNode);
        sle.toBytesSink(half);
        half.update(index);
        return half.finish();
    }
}
