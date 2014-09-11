package com.ripple.core.types.shamap2;

import com.ripple.core.types.known.sle.LedgerEntry;

import java.util.concurrent.atomic.AtomicInteger;

public class ShaMap extends ShaMapInner {
    private AtomicInteger versioner;

    public ShaMap() {
        super(0);
        versioner = new AtomicInteger();
    }
    public ShaMap(boolean isCopy, int depth) {
        super(isCopy, depth, 0);
    }
    @Override
    protected ShaMapInner copyInner(int depth) {
        return new ShaMap(true, depth);
    }

    public ShaMap copy() {
        int newVersion = versioner.incrementAndGet();
        ShaMap copy = (ShaMap) copy(newVersion);
        copy.versioner = versioner;
        return copy;
    }

    public void addLE(LedgerEntry fromJSON) {
        LedgerEntryItem item = new LedgerEntryItem(fromJSON);
        addItem(fromJSON.index(), item);
    }
}
