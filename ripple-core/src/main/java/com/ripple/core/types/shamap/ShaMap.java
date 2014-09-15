package com.ripple.core.types.shamap;

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
    protected ShaMapInner makeInnerOfSameClass(int depth) {
        return new ShaMap(true, depth);
    }

    public ShaMap copy() {
        version = copies.incrementAndGet();
        ShaMap copy = (ShaMap) copy(copies.incrementAndGet());
        copy.copies = copies;
        return copy;
    }

}
