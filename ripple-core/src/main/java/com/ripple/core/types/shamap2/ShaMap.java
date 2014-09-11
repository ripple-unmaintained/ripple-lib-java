package com.ripple.core.types.shamap2;

import com.ripple.core.types.known.sle.LedgerEntry;

import java.util.concurrent.atomic.AtomicInteger;

public class ShaMap extends ShaMapInner {
    private AtomicInteger copies;

    public ShaMap() {
        super(0);
        // This way we can copy the first to the second,
        // copy the second, then copy the first again ;)
        copies = new AtomicInteger();
    }
    public ShaMap(boolean isCopy, int depth) {
        super(isCopy, depth, 0);
    }

    @Override
    protected ShaMapInner copyInner(int depth) {
        return new ShaMap(true, depth);
    }

    public ShaMap copy() {
        ShaMap copy = (ShaMap) copy((version = copies.incrementAndGet()) + 1);
        copy.copies = copies;
        return copy;
    }

    public void addLE(LedgerEntry entry) {
        LedgerEntryItem item = new LedgerEntryItem(entry);
        addItem(entry.index(), item);
    }
}
