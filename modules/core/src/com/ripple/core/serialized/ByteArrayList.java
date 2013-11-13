package com.ripple.core.serialized;

import java.util.ArrayList;

public class ByteArrayList {
    /*private ArrayList<byte[]> buffers = new ArrayList<byte[]>();

    public void add(byte aByte) {
        add(new byte[]{aByte});
    }

    public void add(byte[] bytes) {
        buffers.add(bytes);
    }

    public byte[] bytes() {
        int n = 0, destPos = 0;
        for (byte[] bytes : buffers) n += bytes.length;
        byte[] joined = new byte[n];

        for (byte[] bytes : buffers) {
            int length = bytes.length;
            System.arraycopy(bytes, 0, joined, destPos, length);
            destPos += length;
        }
        return joined;
    }*/


    private ArrayList<Object> buffers = new ArrayList<Object>();

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

    private int addBytes(byte[] bytes, int destPos) {
        for (Object o : buffers) {
            if (o instanceof byte[]) {
                byte[] buf = (byte[]) o;
                System.arraycopy(buf, 0, bytes, destPos, buf.length);
                destPos += buf.length;
            } else {
                destPos = ((ByteArrayList) o).addBytes(bytes, destPos);
            }
        }
        return destPos;
    }

    public void add(ByteArrayList ba1) {
        buffers.add(ba1);
    }

    int length() {
        int n = 0;
        for (Object buffer : buffers) {
            if (buffer instanceof byte[]) {
                n+= ((byte[]) buffer).length;
            } else {
                n+= ((ByteArrayList) buffer).length();
            }
        }
        return n;
    }

}
