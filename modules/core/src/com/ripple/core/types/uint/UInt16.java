package com.ripple.core.types.uint;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.serialized.TypeTranslator;

import java.math.BigInteger;

public class UInt16 extends UINT<Integer, UInt16> {
    public static TypeTranslator<UInt16> translate = new UINTTranslator<UInt16>() {
        @Override
        public UInt16 newInstance(BigInteger i) {
            return new UInt16(i);
        }
    };

    public UInt16(byte[] bytes) {
        super(bytes);
    }

    public UInt16(BigInteger value) {
        super(value);
    }

    public UInt16(Number s) {
        super(s);
    }

    public UInt16(String s) {
        super(s);
    }

    public UInt16(String s, int radix) {
        super(s, radix);
    }

    @Override
    public int getByteWidth() {
        return 2;
    }

    @Override
    public UInt16 instanceFrom(BigInteger n) {
        return new UInt16(n);
    }

    @Override
    public Integer value() {
        return intValue();
    }

    private UInt16(){}

    public abstract static class UInt16Field extends UInt16 implements HasField {}
    public static UInt16Field int16Field(final Field f) {
        return new UInt16Field(){ @Override public Field getField() {return f;}};
    }

    static public UInt16Field LedgerEntryType = int16Field(Field.LedgerEntryType);
    static public UInt16Field TransactionType = int16Field(Field.TransactionType);

}
