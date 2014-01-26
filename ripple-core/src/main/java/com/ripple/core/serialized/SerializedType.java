package com.ripple.core.serialized;

public interface SerializedType {
    Object toJSON();
    byte[] toBytes();
    String toHex();
    void toBytesList(BytesList to);
}
