package com.ripple.core.coretypes;

import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.*;
import com.ripple.core.fields.Field;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class STArray extends ArrayList<STObject> implements SerializedType {

    public static final byte ARRAY_END = (byte) 0xF1;

    @Override
    public Object toJSON() {
        return toJSONArray();
    }


//    @Override
    public JSONArray toJSONArray() {
        JSONArray array = new JSONArray();

        for (STObject so : this) {
            array.put(so.toJSON());
        }

        return array;
    }

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

    public static class Translator extends TypeTranslator<STArray> {

        @Override
        public STArray fromParser(BinaryParser parser, Integer hint) {
            byte arrayEnd = ARRAY_END;
            STArray stArray = new STArray();
            int nfields = 1; // These top level objects only have one key
                             // and aren't separated by object markers

            while (parser.notConsumedOrAtMarker(arrayEnd)) {
                // We can't really
                STObject outer = new STObject();
                Field field = parser.readField();
                outer.put(field, STObject.translate.fromParser(parser));
                stArray.add(outer);
            }


            parser.safelyAdvancePast(arrayEnd);
            return stArray;
        }

        @Override
        public JSONArray toJSONArray(STArray obj) {
            return obj.toJSONArray();
        }

        @Override
        public STArray fromJSONArray(JSONArray jsonArray) {
            STArray arr = new STArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    Object o = jsonArray.get(i);
                    arr.add(STObject.fromJSONObject((JSONObject) o));

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            return arr;
        }

        @Override
        public void toBytesList(STArray obj, BytesList bytes) {
            for (STObject stObject : obj) {
                STObject.translate.toBytesList(stObject, bytes);
            }
        }
    }
    static public Translator translate = new Translator();

    public STArray(){}

    public static TypedFields.STArrayField starrayField(final Field f) {
        return new TypedFields.STArrayField(){ @Override public Field getField() {return f;}};
    }

    static public TypedFields.STArrayField AffectedNodes = starrayField(Field.AffectedNodes);

    static public TypedFields.STArrayField SigningAccounts = starrayField(Field.SigningAccounts);
    static public TypedFields.STArrayField TxnSignatures = starrayField(Field.TxnSignatures);
    static public TypedFields.STArrayField Signatures = starrayField(Field.Signatures);
    static public TypedFields.STArrayField Template = starrayField(Field.Template);
    static public TypedFields.STArrayField Necessary = starrayField(Field.Necessary);
    static public TypedFields.STArrayField Sufficient = starrayField(Field.Sufficient);
}
