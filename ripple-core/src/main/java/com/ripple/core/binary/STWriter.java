package com.ripple.core.binary;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.serialized.BinarySerializer;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.tx.result.TransactionResult;

public class STWriter implements BytesSink {
    BytesSink sink;
    BinarySerializer serializer;
    public STWriter(BytesSink bytesSink) {
        serializer = new BinarySerializer(bytesSink);
        sink = bytesSink;
    }
    public void write(SerializedType obj) {
        obj.toBytesSink(sink);
    }
    public void writeVl(SerializedType obj) {
        serializer.addLengthEncoded(obj);
    }

    @Override
    public void add(byte aByte) {
        sink.add(aByte);
    }

    @Override
    public void add(byte[] bytes) {
        sink.add(bytes);
    }

    public void write(TransactionResult result) {
        write(result.hash);
        writeVl(result.txn);
        writeVl(result.meta);
    }

    public void write(Hash256 hash256, LedgerEntry le) {
        write(hash256);
        writeVl(le);
    }
}
