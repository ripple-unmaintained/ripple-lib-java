package com.ripple.core.types;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class STArray extends ArrayList<STObject> implements SerializedType {
    @Override
    public TypeTranslator translator() {
        return translate;
    }

    static class Translator extends TypeTranslator<STArray> {
        @Override
        public STArray fromWireBytes(byte[] bytes) {
            return null;
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
        public byte[] toWireBytes(STArray obj) {
            return new byte[0];
        }
    }
    static public Translator translate = new Translator();

    private STArray(){}
    
    protected abstract static class STArrayField extends STArray implements HasField{}
    public static STArrayField starrayField(final Field f) {
        return new STArrayField(){ @Override public Field getField() {return f;}};
    }
    
    static public STArrayField SigningAccounts = starrayField(Field.SigningAccounts);
    static public STArrayField TxnSignatures = starrayField(Field.TxnSignatures);
    static public STArrayField Signatures = starrayField(Field.Signatures);
    static public STArrayField Template = starrayField(Field.Template);
    static public STArrayField Necessary = starrayField(Field.Necessary);
    static public STArrayField Sufficient = starrayField(Field.Sufficient);
    static public STArrayField AffectedNodes = starrayField(Field.AffectedNodes);
    
    
}
