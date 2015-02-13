package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.encodings.common.B16;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Funnily enough, yes, in rippled a currency is represented by a Hash160 type.
 * For the sake of consistency and convenience, this quirk is repeated here.
 *
 * https://gist.github.com/justmoon/8597643
 */
public class Currency extends Hash160 {
    public static final Currency NEUTRAL = new Currency(BigInteger.ONE.toByteArray());
    public static final Currency XRP = new Currency(BigInteger.ZERO.toByteArray());

    @Override
    public Object toJSON() {
        return translate.toJSON(this);
    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        translate.toBytesSink(this, to);
    }

    public boolean isNative() {
        return this == Currency.XRP || equals(Currency.XRP);
    }

    public boolean isIOU() {
        return !isNative();
    }

    public static enum Type {
        HASH,
        ISO,      // three letter isoCode
        DEMURRAGE,
        UNKNOWN;

        public static Type fromByte(byte typeByte) {
            if (typeByte == 0x00) {
                return ISO;
            } else if (typeByte == 0x01) {
                return DEMURRAGE;
            } else if ((typeByte & 0x80) != 0) {
                return HASH;
            } else {
                return UNKNOWN;
            }
        }
    }
    Type type;

    public static class Demurrage {
        Date interestStart;
        String isoCode;
        double interestRate;

        static public BigDecimal applyRate(BigDecimal amount, BigDecimal rate, TimeUnit time, long units) {
            BigDecimal appliedRate = getSeconds(time, units).divide(rate, MathContext.DECIMAL64);
            BigDecimal factor = BigDecimal.valueOf(Math.exp(appliedRate.doubleValue()));
            return amount.multiply(factor, MathContext.DECIMAL64);
        }

        static public BigDecimal calculateRate(BigDecimal rate, TimeUnit time, long units) {
            BigDecimal seconds = getSeconds(time, units);
            BigDecimal log = ln(rate);
            return seconds.divide(log, MathContext.DECIMAL64);
        }

        private static BigDecimal ln(BigDecimal bd) {
            return BigDecimal.valueOf(Math.log(bd.doubleValue()));
        }

        private static BigDecimal getSeconds(TimeUnit time, long units) {
            return BigDecimal.valueOf(time.toSeconds(units));
        }

        public Demurrage(byte[] bytes) {
            BinaryParser parser = new BinaryParser(bytes);
            parser.skip(1); // The type
            isoCode = isoCodeFromBytesAndOffset(parser.read(3), 0);// The isoCode
            interestStart = RippleDate.fromParser(parser);
            long l = UInt64.translate.fromParser(parser).longValue();
            interestRate = Double.longBitsToDouble(l);
        }
    }
    public Demurrage demurrage = null;
    public Currency(byte[] bytes) {
        super(bytes);
        type = Type.fromByte(this.hash[0]);
        if (type == Type.DEMURRAGE) {
            demurrage = new Demurrage(bytes);
        }
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
        public Object toJSON(Currency obj) {
            return obj.toString();
        }

        @Override
        public Currency fromString(String value) {
            if (value.length() == 40 /* byteWidth() * 2 */) {
                return newInstance(B16.decode(value));
            } else if (value.equals("XRP")) {
                return XRP;
            } else {
                if (!(value.length() == 3)) {
//                if (!value.matches("[A-Z0-9]{3}")) {
                    throw new RuntimeException("Currency code must be 3 characters");
                }
                return newInstance(encodeCurrency(value));
            }
        }
    }

    public static Currency fromString(String currency) {
        return translate.fromString(currency);
    }

    @Override
    public String toString() {
        switch (type) {
            case ISO:
                String code = getCurrencyCodeFromTLCBytes(bytes());
                if (code.equals("XRP")) {
                    // HEX of the bytes
                    return super.toString();
                } else if (code.equals("\0\0\0")) {
                    return "XRP";
                } else {
                    // the 3 letter isoCode
                    return code;
                }
            case HASH:
            case DEMURRAGE:
            case UNKNOWN:
            default:
                return super.toString();
        }
    }

    public String humanCode() {
        if (type == Type.ISO) {
            return getCurrencyCodeFromTLCBytes(hash);
        } else if (type == Type.DEMURRAGE) {
            return isoCodeFromBytesAndOffset(hash, 1);
        } else {
            throw new IllegalStateException("No human code for currency of type " + type);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Currency) {
            Currency other = (Currency) obj;
            byte[] bytes = this.bytes();
            byte[] otherBytes = other.bytes();

            if (type == Type.ISO && other.type == Type.ISO) {
                return (bytes[12] == otherBytes[12] &&
                        bytes[13] == otherBytes[13] &&
                        bytes[14] == otherBytes[14]);
            }
        }
        return super.equals(obj); // Full comparison
    }

    public static CurrencyTranslator translate = new CurrencyTranslator();

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

    public static String getCurrencyCodeFromTLCBytes(byte[] bytes) {
        int i;
        boolean zeroInNonCurrencyBytes = true;

        for (i = 0; i < 20; i++) {
            zeroInNonCurrencyBytes = zeroInNonCurrencyBytes &&
                    ((i == 12 || i == 13 || i == 14) || // currency bytes (0 or any other)
                            bytes[i] == 0);                   // non currency bytes (0)
        }

        if (zeroInNonCurrencyBytes) {
            return isoCodeFromBytesAndOffset(bytes, 12);
        } else {
            throw new IllegalStateException("Currency is invalid");
        }
    }

    private static char charFrom(byte[] bytes, int i) {
        return (char) bytes[i];
    }

    private static String isoCodeFromBytesAndOffset(byte[] bytes, int offset) {
        char a = charFrom(bytes, offset);
        char b = charFrom(bytes, offset + 1);
        char c = charFrom(bytes, offset + 2);
        return "" + a + b + c;
    }
}
