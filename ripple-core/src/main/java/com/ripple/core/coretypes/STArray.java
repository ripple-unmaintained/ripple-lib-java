package com.ripple.core.coretypes;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class STArray extends ArrayList<STObject> implements SerializedType {
    public JSONArray toJSONArray() {
        JSONArray array = new JSONArray();

        for (STObject so : this) {
            array.put(so.toJSON());
        }

        return array;
    }

    @Override
    public Object toJSON() {
        return toJSONArray();
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
        for (STObject stObject : this) {
            stObject.toBytesSink(to);
        }
    }

    public static class Translator extends TypeTranslator<STArray> {

        @Override
        public STArray fromParser(BinaryParser parser, Integer hint) {
            STArray stArray = new STArray();
            while (!parser.end()) {
                Field field = parser.readField();
                if (field == Field.ArrayEndMarker) {
                    break;
                }
                STObject outer = new STObject();
                // assert field.getType() == Type.STObject;
                outer.put(field, STObject.translate.fromParser(parser));
                stArray.add(STObject.formatted(outer));
            }
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
