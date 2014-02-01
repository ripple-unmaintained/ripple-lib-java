package com.ripple.core.serialized;

import com.ripple.core.fields.Field;

import java.util.Arrays;

public class BinarySerializer {
    public final BytesList buffer;

    public BinarySerializer() {
        this.buffer = new BytesList();
    }

    public BinarySerializer(BytesList buffer) {
        this.buffer = buffer;
    }

    public void add(byte[] n) {
        buffer.add(n);
    }

    public void addLengthEncoded(byte[] n) {
        add(encodeVL(n.length));
        add(n);
    }

    public static byte[] encodeVL(int  length) {
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

    public void add(BytesList inner) {
        buffer.add(inner);
    }

    public int addFieldHeader(Field f) {
        if (!f.isSerialized()) {
            throw new IllegalStateException(String.format("Field %s is a discardable field", f));
        }
        byte[] n = f.getBytes();
        add(n);
        return n.length;
    }

    public byte[] bytes() {
        return buffer.bytes();
    }

    public void add(byte type) {
        buffer.add(type);
    }

    public void addLengthEncoded(BytesList bytes) {
        add(BinarySerializer.encodeVL(bytes.length()));
        add(bytes);
    }
}
