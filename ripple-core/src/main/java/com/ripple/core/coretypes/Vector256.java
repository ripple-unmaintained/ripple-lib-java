package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;
import com.ripple.core.fields.Vector256Field;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;
import org.json.JSONArray;

import java.util.ArrayList;

public class Vector256 extends ArrayList<Hash256> implements SerializedType {

    @Override
    public Object toJSON() {
        return toJSONArray();
    }

    public JSONArray toJSONArray() {
        JSONArray array = new JSONArray();

        for (Hash256 hash256 : this) {
            array.put(hash256.toString());
        }

        return array;
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
        for (Hash256 hash256 : this) {
            hash256.toBytesSink(to);
        }
    }

    @Override
    public Type type() {
        return Type.Vector256;
    }

    /**
     * This method puts the last element in the removed elements slot, and
     *  pops off the back, thus preserving contiguity but losing ordering.
     * @param ledgerIndex the ledger entry index to remove
     */
    public boolean removeUnstable(Hash256 ledgerIndex) {
        int i = indexOf(ledgerIndex);
        if (i == -1) {
            return false;
        }

        int last = size() - 1;
        Hash256 lastIndex = get(last);
        set(i, lastIndex);
        remove(last);

        return true;
    }

    public static class Translator extends TypeTranslator<Vector256> {
        @Override
        public Vector256 fromParser(BinaryParser parser, Integer hint) {
            Vector256 vector256 = new Vector256();
            if (hint == null) {
                hint = parser.size() - parser.pos();
            }
            for (int i = 0; i < hint / 32; i++) {
                vector256.add(Hash256.translate.fromParser(parser));
            }

            return vector256;
        }

        @Override
        public JSONArray toJSONArray(Vector256 obj) {
            return obj.toJSONArray();
        }

        @Override
        public Vector256 fromJSONArray(JSONArray jsonArray) {
            Vector256 vector = new Vector256();

            for (int i = 0; i < jsonArray.length(); i++) {
                String hex = jsonArray.getString(i);
                vector.add(new Hash256(B16.decode(hex)));
            }

            return vector;
        }
    }
    static public Translator translate = new Translator();

    public Vector256(){}

    public static Vector256Field vector256Field(final Field f) {
        return new Vector256Field(){ @Override public Field getField() {return f;}};
    }
    
    static public Vector256Field Indexes = vector256Field(Field.Indexes);
    static public Vector256Field Hashes = vector256Field(Field.Hashes);
    static public Vector256Field Amendments = vector256Field(Field.Amendments);
}
