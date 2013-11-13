package com.ripple.core.serialized;

public class Markers {
    public final static byte[] OBJECT_END_MARKER = new byte[]{(byte) 0xE1};
    public static final byte[] ARRAY_END_MARKER = new byte[]{(byte) 0xF1};
}
