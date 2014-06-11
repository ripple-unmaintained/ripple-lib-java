package com.ripple.core.binary;

import com.ripple.core.serialized.BinarySerializer;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;

public class BinaryWriter {
    BytesSink sink;
    BinarySerializer serializer;
    public BinaryWriter(BytesSink bytesSink) {
        serializer = new BinarySerializer(bytesSink);
        sink = bytesSink;
    }
    public void write(SerializedType obj) {
        obj.toBytesSink(sink);
    }
    public void writeVl(SerializedType obj) {
        serializer.addLengthEncoded(obj);
    }
}
