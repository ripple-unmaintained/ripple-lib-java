package com.ripple.core.serialized;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;

import java.util.Arrays;

public class BinarySerializer {
    private final BytesSink sink;

    public BinarySerializer(BytesSink sink) {
        this.sink = sink;
    }

    public void add(byte[] n) {
        sink.add(n);
    }

    public void addLengthEncoded(byte[] n) {
        add(encodeVL(n.length));
        add(n);
    }

    public static byte[] encodeVL(int  length) {
        // TODO: bytes
        byte[] lenBytes = new byte[4];

        if (length <= 192)
        {
            lenBytes[0] = (byte) (length);
            return Arrays.copyOf(lenBytes, 1);
        }
        else if (length <= 12480)
        {
            length -= 193;
            lenBytes[0] = (byte) (193 + (length >>> 8));
            lenBytes[1] = (byte) (length & 0xff);
            return Arrays.copyOf(lenBytes, 2);
        }
        else if (length <= 918744) {
            length -= 12481;
            lenBytes[0] = (byte) (241 + (length >>> 16));
            lenBytes[1] = (byte) ((length >> 8) & 0xff);
            lenBytes[2] = (byte) (length & 0xff);
            return Arrays.copyOf(lenBytes, 3);
        } else {
            throw new RuntimeException("Overflow error");
        }
    }

    public void add(BytesList bl) {
        for (byte[] bytes : bl.rawList()) {
            sink.add(bytes);
        }
    }

    public int addFieldHeader(Field f) {
        if (!f.isSerialized()) {
            throw new IllegalStateException(String.format("Field %s is a discardable field", f));
        }
        byte[] n = f.getBytes();
        add(n);
        return n.length;
    }

    public void add(byte type) {
        sink.add(type);
    }

    public void addLengthEncoded(BytesList bytes) {
        add(encodeVL(bytes.bytesLength()));
        add(bytes);
    }

    public void add(Field field, SerializedType value) {
        addFieldHeader(field);
        if (field.isVLEncoded()) {
            addLengthEncoded(value);
        } else {
            value.toBytesSink(sink);
            if (field.getType() == Type.STObject) {
                addFieldHeader(Field.ObjectEndMarker);
            } else if (field.getType() == Type.STArray) {
                addFieldHeader(Field.ArrayEndMarker);
            }
        }
    }

    public void addLengthEncoded(SerializedType value) {
        BytesList bytes = new BytesList();
        value.toBytesSink(bytes);
        addLengthEncoded(bytes);
    }
}
