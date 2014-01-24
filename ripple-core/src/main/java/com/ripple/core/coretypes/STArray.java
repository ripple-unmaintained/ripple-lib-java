package com.ripple.core.coretypes;

import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.*;
import com.ripple.core.fields.Field;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class STArray extends ArrayList<STObject> implements SerializedType {

    public static class Translator extends TypeTranslator<STArray> {

        @Override
        public STArray fromParser(BinaryParser parser, Integer hint) {
            byte arrayEnd = Markers.ARRAY_END;
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
        public Object toJSON(STArray obj) {
            JSONArray array = new JSONArray();

            for (STObject so : obj) {
                array.put(STObject.translate.toJSONObject(so));
            }

            return array;
        }

        @Override
        public void toBytesTree(STArray obj, BytesList bytes) {
            for (STObject stObject : obj) {
                STObject.translate.toBytesTree(stObject, bytes);
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
