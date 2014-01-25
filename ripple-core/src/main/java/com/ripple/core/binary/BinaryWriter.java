package com.ripple.core.binary;

import com.ripple.core.serialized.BytesList;
import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.Hash128;
import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.serialized.SerializedType;

public class BinaryWriter {
    BytesList list;
    public BinaryWriter(BytesList list) {
        this.list = list;
    }
    public void write(SerializedType obj) {
        obj.toBytesList(list);
    }
}
