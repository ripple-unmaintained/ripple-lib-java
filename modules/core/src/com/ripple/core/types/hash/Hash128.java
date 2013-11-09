package com.ripple.core.types.hash;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.serialized.TypeTranslator;

public class Hash128 extends HASH {
    public Hash128(byte[] bytes) {
        super(bytes, 16);
    }

    @Override
    public TypeTranslator translator() {
        return translate;
    }

    public static class Translator extends HashTranslator<Hash128> {
        @Override
        public Hash128 newInstance(byte[] b) {
            return new Hash128(b);
        }
    }
    public static Translator translate = new Translator();

    private Hash128(){}

    public abstract static class Hash128Field extends Hash128 implements HasField {}
    public static Hash128Field hash128Field(final Field f) {
        return new Hash128Field(){ @Override public Field getField() {return f;}};
    }

    static public Hash128Field EmailHash = hash128Field(Field.EmailHash);

}
