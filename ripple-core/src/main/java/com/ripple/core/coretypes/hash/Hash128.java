package com.ripple.core.coretypes.hash;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;

public class Hash128 extends HASH<Hash128> {
    public Hash128(byte[] bytes) {
        super(bytes, 16);
    }

    public static class Translator extends HashTranslator<Hash128> {
        @Override
        public Hash128 newInstance(byte[] b) {
            return new Hash128(b);
        }

        @Override
        public int byteWidth() {
            return 16;
        }
    }
    public static Translator translate = new Translator();

    public static TypedFields.Hash128Field hash128Field(final Field f) {
        return new TypedFields.Hash128Field(){ @Override public Field getField() {return f;}};
    }

    static public TypedFields.Hash128Field EmailHash = hash128Field(Field.EmailHash);

}
