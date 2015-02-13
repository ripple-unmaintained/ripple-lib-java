package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BytesSink;

abstract public class ShaMapItem<T> {
    abstract void toBytesSink(BytesSink sink);
    public abstract ShaMapItem<T> copy();
    public abstract T value();
    public abstract Prefix hashPrefix();
}
