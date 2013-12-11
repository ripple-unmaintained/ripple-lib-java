package com.ripple.core.serialized;

import java.util.ArrayList;

public class BytesTree {
    private ArrayList<Object> buffers = new ArrayList<Object>();

    public void add(BytesTree ba1) {
        buffers.add(ba1);
    }

    public void add(byte aByte) {
        add(new byte[]{aByte});
    }

    public void add(byte[] bytes) {
        buffers.add(bytes);
    }

    public byte[] bytes() {
        int n = length(), destPos = 0;
        byte[] bytes = new byte[n];
        addBytes(bytes, destPos);
        return bytes;
    }

    int length() {
        int n = 0;
        for (Object buffer : buffers) {
            if (buffer instanceof byte[]) {
                n+= ((byte[]) buffer).length;
            } else {
                n+= ((BytesTree) buffer).length();
            }
        }
        return n;
    }

    private int addBytes(byte[] bytes, int destPos) {
        for (Object o : buffers) {
            if (o instanceof byte[]) {
                byte[] buf = (byte[]) o;
                System.arraycopy(buf, 0, bytes, destPos, buf.length);
                destPos += buf.length;
            } else {
                destPos = ((BytesTree) o).addBytes(bytes, destPos);
            }
        }
        return destPos;
    }
}
