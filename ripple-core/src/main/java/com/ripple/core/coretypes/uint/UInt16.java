package com.ripple.core.coretypes.uint;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;
import com.ripple.core.fields.UInt16Field;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.TypeTranslator;

import java.math.BigInteger;

public class UInt16 extends UInt<UInt16> {
    public final static UInt16 ZERO = new UInt16(0);

    public static TypeTranslator<UInt16> translate = new UINTTranslator<UInt16>() {
        @Override
        public UInt16 newInstance(BigInteger i) {
            return new UInt16(i);
        }

        @Override
        public int byteWidth() {
            return 2;
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

    public static UInt16Field int16Field(final Field f) {
        return new UInt16Field(){ @Override public Field getField() {return f;}};
    }

    static public UInt16Field LedgerEntryType = int16Field(Field.LedgerEntryType);
    static public UInt16Field TransactionType = int16Field(Field.TransactionType);
    static public UInt16Field SignerWeight = int16Field(Field.SignerWeight);

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

    @Override
    public Type type() {
        return Type.UInt16;
    }
}
