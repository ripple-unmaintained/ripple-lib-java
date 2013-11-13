package com.ripple.core.types;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.core.types.hash.Hash256;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Vector256 extends ArrayList<Hash256> implements SerializedType {

    static class Translator extends TypeTranslator<Vector256> {
        @Override
        public Vector256 fromWireBytes(BinaryParser parser) {
            return null;
        }

        @Override
        public Object toJSON(Vector256 obj) {
            return null;
        }

        @Override
        public Vector256 fromJSONArray(JSONArray jsonArray) {
            Vector256 vector = new Vector256();

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    String hex = jsonArray.getString(i);
                    vector.add(Hash256.translate.fromString(hex));

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            return vector;
        }

        @Override
        public byte[] toWireBytes(Vector256 obj) {
            return new byte[0];
        }
    }
    static public Translator translate = new Translator();

    private Vector256(){}
    
    protected abstract static class Vector256Field extends Vector256 implements HasField{}
    public static Vector256Field vector256Field(final Field f) {
        return new Vector256Field(){ @Override public Field getField() {return f;}};
    }
    
    static public Vector256Field Indexes = vector256Field(Field.Indexes);
    static public Vector256Field Hashes = vector256Field(Field.Hashes);
    static public Vector256Field Features = vector256Field(Field.Features);
}
