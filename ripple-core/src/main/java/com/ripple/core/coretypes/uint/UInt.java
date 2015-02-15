package com.ripple.core.coretypes.uint;

import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;

import java.math.BigInteger;

abstract public class UInt<Subclass extends UInt> extends Number implements SerializedType, Comparable<UInt> {

    private BigInteger value;

    public static BigInteger Max8  = new BigInteger("256"),
                             Max16 = new BigInteger("65536"),
                             Max32 = new BigInteger("4294967296"),
                             Max64 = new BigInteger("18446744073709551616");

    public BigInteger getMinimumValue() {
        return BigInteger.ZERO;
    }
    public UInt(byte[] bytes) {
        setValue(new BigInteger(1, bytes));
    }
    public UInt(BigInteger bi) {
        setValue(bi);
    }
    public UInt(Number s) {
        setValue(BigInteger.valueOf(s.longValue()));
    }
    public UInt(String s) {
        setValue(new BigInteger(s));
    }
    public UInt(String s, int radix) {
        setValue(new BigInteger(s, radix));
    }


    @Override
    public String toString() {
        return value.toString();
    }

    public UInt() {}

    public abstract int getByteWidth();
    public abstract Subclass instanceFrom(BigInteger n);

    public boolean isValid(BigInteger n) {
        return !((bitLength() / 8) > getByteWidth());
    }

    public Subclass add(UInt val) {
        return instanceFrom(value.add(val.value));
    }

    public Subclass subtract(UInt val) {
        return instanceFrom(value.subtract(val.value));
    }

    public Subclass multiply(UInt val) {
        return instanceFrom(value.multiply(val.value));
    }

    public Subclass divide(UInt val) {
        return instanceFrom(value.divide(val.value));
    }

    public Subclass or(UInt val) {
        return instanceFrom(value.or(val.value));
    }

    public Subclass shiftLeft(int n) {
        return instanceFrom(value.shiftLeft(n));
    }

    public Subclass shiftRight(int n) {
        return instanceFrom(value.shiftRight(n));
    }

    public int bitLength() {
        return value.bitLength();
    }

    public int compareTo(UInt val) {
        return value.compareTo(val.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UInt) {
            return equals((UInt) obj);
        }
        else return super.equals(obj);
    }

    public boolean equals(UInt x) {
        return value.equals(x.value);
    }

    public BigInteger min(BigInteger val) {
        return value.min(val);
    }

    public BigInteger max(BigInteger val) {
        return value.max(val);
    }

    public String toString(int radix) {
        return value.toString(radix);
    }
    public byte[] toByteArray() {
        int length = getByteWidth();

        {
            byte[] bytes = value.toByteArray();

            if (bytes[0] == 0) {
                if (bytes.length - 1 > length) {
                    throw new IllegalArgumentException("standard length exceeded for value");
                }

                byte[] tmp = new byte[length];

                System.arraycopy(bytes, 1, tmp, tmp.length - (bytes.length - 1), bytes.length - 1);

                return tmp;
            } else {
                if (bytes.length == length) {
                    return bytes;
                }

                if (bytes.length > length) {
                    throw new IllegalArgumentException("standard length exceeded for value");
                }

                byte[] tmp = new byte[length];

                System.arraycopy(bytes, 0, tmp, tmp.length - bytes.length, bytes.length);

                return tmp;
            }
        }
    }

    abstract public Object value();

    public BigInteger bigInteger(){
        return value;
    }


    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public byte byteValue() {
        return value.byteValue();
    }

    @Override
    public short shortValue() {
        return value.shortValue();
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public <T extends UInt> boolean  lte(T sequence) {
        return compareTo(sequence) < 1;
    }

    public boolean testBit(int f) {
        // TODO, optimized ;) // move to Uint32
        return value.testBit(f);
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    static public abstract class UINTTranslator<T extends UInt> extends TypeTranslator<T> {
        public abstract T newInstance(BigInteger i);
        public abstract int byteWidth();

        @Override
        public T fromParser(BinaryParser parser, Integer hint) {
            return newInstance(new BigInteger(1, parser.read(byteWidth())));
        }

        @Override
        public Object toJSON(T obj) {
            if (obj.getByteWidth() <= 4) {
                return obj.longValue();
            } else {
                return toString(obj);
            }
        }

        @Override
        public T fromLong(long aLong) {
            return newInstance(BigInteger.valueOf(aLong));
        }

        @Override
        public T fromString(String value) {
            int radix = byteWidth() <= 4 ? 10 : 16;
            return newInstance(new BigInteger(value, radix));
        }

        @Override
        public T fromInteger(int integer) {
            return fromLong(integer);
        }

        @Override
        public String toString(T obj) {
            return B16.toString(obj.toByteArray());
        }

        @Override
        public void toBytesSink(T obj, BytesSink to) {
            to.add(obj.toByteArray());
        }
    }
}
