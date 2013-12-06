package com.ripple.core.types;

import com.ripple.core.types.hash.Hash160;
import com.ripple.encodings.common.B16;

/**
 * Funnily enough, yes, in rippled a currency is represented by a Hash160 type.
 * For the sake of consistency and convenience, this quirk is repeated here.
 */
public class Currency extends Hash160 {
    public Currency(byte[] bytes) {
        super(bytes);
    }

    /**
     * It's better to extend HashTranslator than the Hash160.Translator directly
     * That way the generics can still vibe with the @Override
     */
    public static class CurrencyTranslator extends HashTranslator<Currency> {
        @Override
        public int byteWidth() {
            return 20;
        }

        @Override
        public Currency newInstance(byte[] b) {
            return new Currency(b);
        }

        @Override
        public Currency fromString(String value) {
            if (value.length() == 40 /* byteWidth() * 2 */) {
                return newInstance(B16.decode(value));
            } else if (value.equals("XRP")) {
                return XRP;
            } else {
                if (!value.matches("[A-Z0-9]{3}")) {
                    throw new RuntimeException("Currency code must be 3 characters");
                }
                return newInstance(encodeCurrency(value));
            }
        }

        @Override
        public String toString(Currency obj) {
            return obj.toString();
        }
    }

    public static Currency fromString(String currency) {
        return translate.fromString(currency);
    }

    @Override
    public String toString() {
        String code = decodeCurrency(bytes());
        if (code.equals("XRP")) {
            // HEX of the bytes
            return super.toString();
        } else if (code.equals("\0\0\0")) {
            return "XRP";
        } else {
            // the 3 letter code
            return code;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Currency) {
            Currency other = (Currency) obj;
            byte[] bytes = this.bytes();
            byte[] otherBytes = other.bytes();

            return (bytes[12] == otherBytes[12] &&
                    bytes[13] == otherBytes[13] &&
                    bytes[14] == otherBytes[14]);
        }
        return super.equals(obj);
    }

    public static CurrencyTranslator translate = new CurrencyTranslator();

    // This is used to represent a native currency
    public static final byte[] ZERO = new byte[20];
    public static final Currency XRP = new Currency(ZERO);

    /*
    * The following are static methods, legacy from when there was no
    * usage of Currency objects, just String with "XRP" ambiguity.
    * */
    public static byte[] encodeCurrency(String currencyCode) {
        byte[] currencyBytes = new byte[20];
        currencyBytes[12] = (byte) currencyCode.codePointAt(0);
        currencyBytes[13] = (byte) currencyCode.codePointAt(1);
        currencyBytes[14] = (byte) currencyCode.codePointAt(2);
        return currencyBytes;
    }

    public static String normalizeIOUCode(String code) {
        if (code.equals("XRP")) {
            return "0000000000000000000000005852500000000000";
        } else {
            return code;
        }
    }

    public static String decodeCurrency(byte[] bytes) {
        int i;
        boolean zeroInNonCurrencyBytes = true;

        for (i = 0; i < 20; i++) {
            zeroInNonCurrencyBytes = zeroInNonCurrencyBytes &&
                    ((i == 12 || i == 13 || i == 14) || // currency bytes (0 or any other)
                            bytes[i] == 0);                   // non currency bytes (0)
        }

        if (zeroInNonCurrencyBytes) {
            return currencyStringFromBytesAndOffset(bytes, 12);
        } else {
            throw new IllegalStateException("Currency is invalid");
        }
    }

    private static char charFrom(byte[] bytes, int i) {
        return (char) bytes[i];
    }

    private static String currencyStringFromBytesAndOffset(byte[] bytes, int offset) {
        char a = charFrom(bytes, offset);
        char b = charFrom(bytes, offset + 1);
        char c = charFrom(bytes, offset + 2);
        return "" + a + b + c;
    }
}
