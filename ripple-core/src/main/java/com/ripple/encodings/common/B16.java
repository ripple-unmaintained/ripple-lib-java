package com.ripple.encodings.common;


import static org.ripple.bouncycastle.util.encoders.Hex.toHexString;

public class B16 {
    public static String toString(byte[] bytes) {
        return toHexString(bytes).toUpperCase();
    }
    public static byte[] decode(String hex) {
        return org.ripple.bouncycastle.util.encoders.Hex.decode(hex);
    }
}
