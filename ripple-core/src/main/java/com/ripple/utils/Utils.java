package com.ripple.utils;

import com.ripple.encodings.common.B16;

import java.math.BigInteger;

public class Utils {
    public static String bigHex(BigInteger bn) {
        return B16.toStringTrimmed(bn.toByteArray());
    }
    public static BigInteger uBigInt(byte[] bytes) {
        return new BigInteger(1, bytes);
    }
}
