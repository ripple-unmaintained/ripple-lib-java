package com.ripple.core.types.shamap;

import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BinarySerializer;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.types.known.tx.result.TransactionResult;

public class TransactionResultItem extends ShaMapItem<TransactionResult> {
    public TransactionResult result;

    public TransactionResultItem(TransactionResult result) {
        this.result = result;
    }

    @Override
    void toBytesSink(BytesSink sink) {
        BinarySerializer write = new BinarySerializer(sink);
        write.addLengthEncoded(result.txn);
        write.addLengthEncoded(result.meta);
    }

    @Override
    public ShaMapItem<TransactionResult> copy() {
        // that's ok right ;) these bad boys are immutable anyway
        return this;
    }

    @Override
    public TransactionResult value() {
        return result;
    }

    @Override
    public Prefix hashPrefix() {
        return HashPrefix.txNode;
    }
}
