package com.ripple.core.serialized;

import java.util.ArrayList;

public class ByteArrayList {
    ArrayList<byte[]> buffers = new ArrayList<byte[]>();

    public void add(byte aByte) {
        add(new byte[]{aByte});
    }

    public void add(byte[] bytes) {
        buffers.add(bytes);
    }

    public byte[] toByteArray() {
        int n = 0, destPos = 0;
        for (byte[] bytes : buffers) n += bytes.length;
        byte[] joined = new byte[n];

        for (byte[] bytes : buffers) {
            int length = bytes.length;
            System.arraycopy(bytes, 0, joined, destPos, length);
            destPos += length;
        }
        return joined;
    }
}
