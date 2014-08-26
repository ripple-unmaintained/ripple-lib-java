package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.Hash256;

public interface TreeWalker {
    public void onLeaf(Hash256 h, ShaMapLeaf le);
    public void onInner(Hash256 h, ShaMapInner inner);
}
