package com.ripple.core.types;

import com.ripple.core.serialized.*;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
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

            while (parser.notConsumedOrAtMarker(arrayEnd))
                stArray.add(STObject.translate.fromParser(parser, nfields));

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
        public void toBytesTree(STArray obj, BytesTree bytes) {
            for (STObject stObject : obj) {
                STObject.translate.toBytesTree(stObject, bytes);
            }
        }
    }
    static public Translator translate = new Translator();

    private STArray(){}
    
    protected abstract static class STArrayField implements HasField{}
    public static STArrayField starrayField(final Field f) {
        return new STArrayField(){ @Override public Field getField() {return f;}};
    }

    static public STArrayField AffectedNodes = starrayField(Field.AffectedNodes);

    static public STArrayField SigningAccounts = starrayField(Field.SigningAccounts);
    static public STArrayField TxnSignatures = starrayField(Field.TxnSignatures);
    static public STArrayField Signatures = starrayField(Field.Signatures);
    static public STArrayField Template = starrayField(Field.Template);
    static public STArrayField Necessary = starrayField(Field.Necessary);
    static public STArrayField Sufficient = starrayField(Field.Sufficient);

    
}
