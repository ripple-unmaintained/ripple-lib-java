package com.ripple.core.types.hash;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
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
        public Hash160 fromString(String value) {
            if (value.startsWith("r")) {
                return newInstance(AccountID.fromAddress(value).bytes());
            }
            return super.fromString(value);
        }
    }
    public static Translator translate = new Translator();
    private Hash160(){}

    public abstract static class Hash160Field extends Hash160 implements HasField {}
    public static Hash160Field hash160Field(final Field f) {
        return new Hash160Field(){ @Override public Field getField() {return f;}};
    }

    static public Hash160Field TakerPaysCurrency = hash160Field(Field.TakerPaysCurrency);
    static public Hash160Field TakerPaysIssuer = hash160Field(Field.TakerPaysIssuer);
    static public Hash160Field TakerGetsCurrency = hash160Field(Field.TakerGetsCurrency);
    static public Hash160Field TakerGetsIssuer = hash160Field(Field.TakerGetsIssuer);
}
