package com.ripple.core.types.shamap2;

import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;

abstract public class ShaMapItem<T> {
    abstract void toBytesSink(BytesSink sink);
    abstract void fromParser(BinaryParser parser);
    abstract void copyFrom(T other);

    public abstract Prefix hashPrefix();
}
