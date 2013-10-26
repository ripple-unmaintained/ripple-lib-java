package com.ripple.core.types;

public class Currency {
    public static byte[] encodeCurrency(String currencyCode) {
        byte[] currencyBytes = new byte[20];
        currencyBytes[12] = (byte) currencyCode.codePointAt(0);
        currencyBytes[13] = (byte) currencyCode.codePointAt(1);
        currencyBytes[14] = (byte) currencyCode.codePointAt(2);
        return currencyBytes;
    }

    public static String decodeCurrency(byte[] bytes) {
        int i;
        boolean zeroExceptCurrency = true;

        for (i=0; i<20; i++) {
            zeroExceptCurrency = zeroExceptCurrency && (i == 12 || i == 13 || i == 14 || bytes[i] == 0);
        }

        if (zeroExceptCurrency) {
            char a = charFrom(bytes, 12);
            char b = charFrom(bytes, 13);
            char c = charFrom(bytes, 14);
            return "" + a + b + c;
        }
        else {
            return "";
        }
    }

    private static char charFrom(byte[] bytes, int i) {
        return (char) bytes[i];
    }
}
