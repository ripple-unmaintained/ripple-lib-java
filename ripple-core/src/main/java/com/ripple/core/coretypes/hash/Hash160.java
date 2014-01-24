package com.ripple.core.coretypes.hash;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.serialized.BytesList;
import org.json.JSONArray;
import org.json.JSONObject;

public class Hash160 extends HASH<Hash160> {
    public Hash160(byte[] bytes) {
        super(bytes, 20);
    }

    @Override
    public Object toJSON() {
        return translate.toJSON(this);
    }

    @Override
    public JSONArray toJSONArray() {
        return null;
    }

    @Override
    public JSONObject toJSONObject() {
        return null;
    }

    @Override
    public byte[] toWireBytes() {
        return new byte[0];
    }

    @Override
    public String toWireHex() {
        return null;
    }

    @Override
    public void toBytesList(BytesList to) {
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

    public static TypedFields.Hash160Field hash160Field(final Field f) {
        return new TypedFields.Hash160Field(){ @Override public Field getField() {return f;}};
    }

    static public TypedFields.Hash160Field TakerPaysIssuer = hash160Field(Field.TakerPaysIssuer);
    static public TypedFields.Hash160Field TakerGetsCurrency = hash160Field(Field.TakerGetsCurrency);
    static public TypedFields.Hash160Field TakerPaysCurrency = hash160Field(Field.TakerPaysCurrency);
    static public TypedFields.Hash160Field TakerGetsIssuer = hash160Field(Field.TakerGetsIssuer);
}
