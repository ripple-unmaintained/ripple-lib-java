package com.ripple.core.types.hash;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.types.AccountID;

public class Hash160 extends HASH {
    public Hash160(byte[] bytes) {
        super(bytes, 20);
    }

    public static class Translator extends HashTranslator<Hash160> {
        @Override
        public Hash160 newInstance(byte[] b) {
            return new Hash160(b);
        }

        @Override
        public int byteWidth() {
            return 20;
        }

        @Override
        public Hash160 fromString(String value) {
            if (value.startsWith("r")) {
                return newInstance(AccountID.fromAddress(value).bytes());
            }
            return super.fromString(value);
        }
    }
    public static Translator translate = new Translator();
    private Hash160(){}

    public static TypedFields.Hash160Field hash160Field(final Field f) {
        return new TypedFields.Hash160Field(){ @Override public Field getField() {return f;}};
    }

    static public TypedFields.Hash160Field TakerPaysCurrency = hash160Field(Field.TakerPaysCurrency);
    static public TypedFields.Hash160Field TakerPaysIssuer = hash160Field(Field.TakerPaysIssuer);
    static public TypedFields.Hash160Field TakerGetsCurrency = hash160Field(Field.TakerGetsCurrency);
    static public TypedFields.Hash160Field TakerGetsIssuer = hash160Field(Field.TakerGetsIssuer);
}
