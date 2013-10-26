package com.ripple.encodings.common;


import static org.bouncycastle.util.encoders.Hex.toHexString;

public class B16 {
    public static String toString(byte[] bytes) {
        return toHexString(bytes);
    }
    public static byte[] decode(String hex) {
        return org.bouncycastle.util.encoders.Hex.decode(hex);
    }
}
