package com.ripple.core.coretypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;

import java.util.BitSet;

// TODO
public class Flags extends BitSet implements SerializedType {
    @Override
    public JsonNode toJackson() {
        return null;
    }

    @Override
    public Object toJSON() {
        return null;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public String toHex() {
        return null;
    }

    @Override
    public void toBytesSink(BytesSink to) {

    }
}
