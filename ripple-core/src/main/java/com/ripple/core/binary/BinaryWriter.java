package com.ripple.core.binary;

import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.SerializedType;

public class BinaryWriter {
    BytesList list;
    public BinaryWriter(BytesList list) {
        this.list = list;
    }
    public void write(SerializedType obj) {
        obj.toBytesSink(list);
    }
}
