package com.ripple.core.types;

import org.bouncycastle.util.encoders.Hex;

public class Currency {
    public static final byte[] ZERO = new byte[20];

    public static byte[] encodeCurrency(String currencyCode) {
        byte[] currencyBytes = new byte[20];
        currencyBytes[12] = (byte) currencyCode.codePointAt(0);
        currencyBytes[13] = (byte) currencyCode.codePointAt(1);
        currencyBytes[14] = (byte) currencyCode.codePointAt(2);
        return currencyBytes;
    }

    public static String decodeCurrency(byte[] bytes) {
        int i;
        boolean zeroInNonCurrencyBytes = true;

        for (i=0; i<20; i++) {
            zeroInNonCurrencyBytes = zeroInNonCurrencyBytes &&
                                   ((i == 12 || i == 13 || i == 14) || // currency bytes (0 or any other)
                                     bytes[i] == 0);                   // non currency bytes (0)
        }

        if (zeroInNonCurrencyBytes) {
            return currencyStringFromBytesAndOffset(bytes, 12);
        }
        else {
            throw new IllegalStateException("Currency is invalid");
        }
    }

    private static char charFrom(byte[] bytes, int i) {
        return (char) bytes[i];
    }

    public static String normalizeCurrency(String currency) {
        if (currency.length() == 40) {
            byte[] bytes = Hex.decode(currency.substring(24, 24 + 6));
            assert bytes.length == 3;
            return currencyStringFromBytesAndOffset(bytes, 0);
        }
        return currency;
    }

    private static String currencyStringFromBytesAndOffset(byte[] bytes, int offset) {
        char a = charFrom(bytes, offset);
        char b = charFrom(bytes, offset + 1);
        char c = charFrom(bytes, offset + 2);
        return "" + a + b + c;
    }
}
