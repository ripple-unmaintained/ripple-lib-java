package com.ripple.core.types.shamap2;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;

public class Hash256Item extends ShaMapItem<Hash256> {
    Hash256 item;

    public Hash256Item(Hash256 item) {
        this.item = item;
    }

    @Override
    void toBytesSink(BytesSink sink) {
        item.toBytesSink(sink);
    }

    @Override
    void fromParser(BinaryParser parser) {
        item = Hash256.translate.fromParser(parser);
    }

    @Override
    void copyFrom(Hash256 other) {
        item = other;
    }

    @Override
    public Prefix hashPrefix() {
        return new Prefix() {
            @Override
            public byte[] bytes() {
                return new byte[]{};
            }
        };
    }
}
