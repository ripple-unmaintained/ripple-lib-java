package com.ripple.core.types.shamap2;

import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.types.known.sle.LedgerEntry;

public class LedgerEntryItem extends ShaMapItem<LedgerEntry> {
    public LedgerEntryItem(LedgerEntry entry) {
        this.entry = entry;
    }

    LedgerEntry entry;

    @Override
    void toBytesSink(BytesSink sink) {
        entry.toBytesSink(sink);
    }

    @Override
    void fromParser(BinaryParser parser) {
        entry = (LedgerEntry) STObject.translate.fromParser(parser);
    }

    @Override
    void copyFrom(LedgerEntry other) {
        entry = other;
    }

    @Override
    public Prefix hashPrefix() {
        return HashPrefix.leafNode;
    }
}
