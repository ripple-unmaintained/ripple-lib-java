package com.ripple.core.serialized;

/**
 * This class should parse headers and object markers
 */
public class BinaryParser {
    byte[] bytes;
    private int cursor = 0;
    int size;

    public BinaryParser(byte[] bytes) {
        this.bytes = bytes;
        size = bytes.length;
    }
    public byte[] peek(int n) {
        return read(n, false);
    }
    public byte peekOne() {
        return read(1, false)[0];
    }
    public byte[] read(int n) {
        return read(n, true);
    }
    public byte readOne(int n) {
        return read(1, true)[0];
    }
    private byte[] read(int n, boolean advance) {
        if (cursor + n > size) {
            throw new IllegalStateException("Trying to read out of bounds");
        }
        byte[] ret = new byte[n];
        System.arraycopy(bytes, cursor, ret, 0, n);
        if (advance) {
            cursor += n;
        }
        return ret;
    }
}
