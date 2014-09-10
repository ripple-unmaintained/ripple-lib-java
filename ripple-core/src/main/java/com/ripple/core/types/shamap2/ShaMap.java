package com.ripple.core.types.shamap2;

import com.ripple.core.types.known.sle.LedgerEntry;

public class ShaMap extends ShaMapInner {
    public ShaMap() {
        super(0);
    }
    public ShaMap(boolean isCopy, int depth) {
        super(isCopy, depth, 0);
    }
    @Override
    protected ShaMapInner copyInner(int depth) {
        return new ShaMap(true, depth);
    }

    public ShaMap copy() {
        return (ShaMap) copy((++version) + 1);
    }

    public void addLE(LedgerEntry fromJSON) {
        LedgerEntryItem item = new LedgerEntryItem(fromJSON);
        addItem(fromJSON.index(), item);
    }
}
