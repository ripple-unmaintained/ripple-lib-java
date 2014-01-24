package com.ripple.core.serialized;

import java.security.MessageDigest;
import java.util.ArrayList;

public class BytesList {
    private ArrayList<byte[]> buffers = new ArrayList<byte[]>();
    private int len = 0;

    public void add(BytesList ba1) {
        buffers.addAll(ba1.buffers);
        len += ba1.len;
    }

    public void add(byte aByte) {
        add(new byte[]{aByte});
    }

    public void add(byte[] bytes) {
        len += bytes.length;
        buffers.add(bytes);
    }

    public byte[] bytes() {
        int n = length();
        byte[] bytes = new byte[n];
        addBytes(bytes, 0);
        return bytes;
    }

    static public String[] hexLookup = new String[256];
    static {
        for (int i = 0; i < 256; i++) {
            String s = Integer.toHexString(i).toUpperCase();
            if (s.length() == 1) {
                s = "0" + s;
            }
            hexLookup[i] = s;
        }
    }

    public String bytesHex() {
        StringBuilder builder = new StringBuilder(len * 2);
        for (byte[] buffer : buffers) {
            for (byte aBytes : buffer) {
                builder.append(hexLookup[aBytes & 0xFF]);
            }
        }
        return builder.toString();
    }

    int length() {
        return len;
    }

    private int addBytes(byte[] bytes, int destPos) {
        for (byte[] buf : buffers) {
            System.arraycopy(buf, 0, bytes, destPos, buf.length);
            destPos += buf.length;
        }
        return destPos;
    }

    public void updateDigest(MessageDigest digest) {
        for (byte[] buffer : buffers) {
            digest.update(buffer);
        }
    }
}
