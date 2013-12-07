package com.ripple.encodings.common;

import org.ripple.bouncycastle.util.encoders.Base64;

public class B64 {
    public static String toString(byte[] bytes) {
        return Base64.toBase64String(bytes);
    }
    public static byte[] decode(String string) {
        return Base64.decode(string);
    }
}
