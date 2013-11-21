package com.ripple.core.types.uint;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.serialized.TypeTranslator;

import java.math.BigInteger;

public class UInt64 extends UINT<UInt64> {
    public static TypeTranslator<UInt64> translate = new UINTTranslator<UInt64>() {
        @Override
        public UInt64 newInstance(BigInteger i) {
            return new UInt64(i);
        }

        @Override
        public int byteWidth() {
            return 8;
        }
    };

    public UInt64(byte[] bytes) {
        super(bytes);
    }

    public UInt64(BigInteger value) {
        super(value);
    }

    public UInt64(Number s) {
        super(s);
    }

    public UInt64(String s) {
        super(s);
    }

    public UInt64(String s, int radix) {
        super(s, radix);
    }

    @Override
    public int getByteWidth() {
        return 8;
    }

    @Override
    public UInt64 instanceFrom(BigInteger n) {
        return new UInt64(n);
    }

    @Override
    public BigInteger value() {
        return bigInteger();
    }

    private UInt64(){}

    public abstract static class UInt64Field implements HasField {}
    private static UInt64Field int64Field(final Field f) {
        return new UInt64Field(){ @Override public Field getField() {return f;}};
    }

    static public UInt64Field IndexNext = int64Field(Field.IndexNext);
    static public UInt64Field IndexPrevious = int64Field(Field.IndexPrevious);
    static public UInt64Field BookNode = int64Field(Field.BookNode);
    static public UInt64Field OwnerNode = int64Field(Field.OwnerNode);
    static public UInt64Field BaseFee = int64Field(Field.BaseFee);
    static public UInt64Field ExchangeRate = int64Field(Field.ExchangeRate);
    static public UInt64Field LowNode = int64Field(Field.LowNode);
    static public UInt64Field HighNode = int64Field(Field.HighNode);

}
