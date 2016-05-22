package com.ripple.core.coretypes.uint;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;
import com.ripple.core.fields.UInt8Field;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.TypeTranslator;

import java.math.BigInteger;

public class UInt8 extends UInt<UInt8> {
    public final static UInt8 ZERO = new UInt8(0);

    public static TypeTranslator<UInt8> translate = new UINTTranslator<UInt8>() {
        @Override
        public UInt8 newInstance(BigInteger i) {
            return new UInt8(i);
        }

        @Override
        public int byteWidth() {
            return 1;
        }
    };

    public UInt8(byte[] bytes) {
        super(bytes);
    }

    public UInt8(BigInteger value) {
        super(value);
    }

    public UInt8(Number s) {
        super(s);
    }

    public UInt8(String s) {
        super(s);
    }

    public UInt8(String s, int radix) {
        super(s, radix);
    }

    @Override
    public int getByteWidth() {
        return 1;
    }

    @Override
    public UInt8 instanceFrom(BigInteger n) {
        return new UInt8(n);
    }

    @Override
    public Short value() {
        return shortValue();
    }

    private UInt8() {
    }

    private static UInt8Field int8Field(final Field f) {
        return new UInt8Field() {@Override public Field getField() {return f; } };
    }

    static public UInt8Field CloseResolution = int8Field(Field.CloseResolution);
    static public UInt8Field Method = int8Field(Field.Method);
    static public UInt8Field TransactionResult = int8Field(Field.TransactionResult);

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
        return Type.UInt8;
    }
}
