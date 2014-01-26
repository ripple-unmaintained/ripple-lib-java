package com.ripple.core.coretypes.hash;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.BytesList;

public class Hash128 extends HASH<Hash128> {
    public Hash128(byte[] bytes) {
        super(bytes, 16);
    }

    @Override
    public Object toJSON() {
        return translate.toJSON(this);
    }

//    @Override
//    public JSONArray toJSONArray() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public JSONObject toJSONObject() {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesList(BytesList to) {
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
