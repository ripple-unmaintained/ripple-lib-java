package com.ripple.core.serialized;

import java.util.ArrayList;

public class ByteArray extends ArrayList<Byte> {
    public void add(byte[] bytes) {
        for (byte aByte : bytes) {
            add(aByte);
        }
    }
    public byte[] toByteArray() {
        byte[] primitive = new byte[size()];
        for (int i = 0; i < primitive.length; i++) {
            primitive[i] = get(i);
        }
        return primitive;
    }
}
