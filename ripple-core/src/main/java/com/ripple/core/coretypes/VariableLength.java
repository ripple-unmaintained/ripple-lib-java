
package com.ripple.core.coretypes;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ripple.bouncycastle.util.encoders.Hex;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;

public class VariableLength implements SerializedType {
    public VariableLength(byte[] bytes) {
        buffer = bytes;
    }

    byte[] buffer;

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

    public static class Translator extends TypeTranslator<VariableLength> {
        @Override
        public VariableLength fromParser(BinaryParser parser, Integer hint) {
            if (hint == null) {
                hint = parser.getSize();
            }
            return new VariableLength(parser.read(hint));
        }

        @Override
        public Object toJSON(VariableLength obj) {
            return toString(obj);
        }

        @Override
        public String toString(VariableLength obj) {
            return B16.toString(obj.buffer);
        }

        @Override
        public VariableLength fromString(String value) {
            return new VariableLength(Hex.decode(value));
        }

        @Override
        public void toBytesList(VariableLength obj, BytesList to) {
            to.add(obj.buffer);
        }
    }

    static public Translator translate = new Translator();

    private VariableLength() {
    }

    public static TypedFields.VariableLengthField variablelengthField(final Field f) {
        return new TypedFields.VariableLengthField() {
            @Override
            public Field getField() {
                return f;
            }
        };
    }

    static public TypedFields.VariableLengthField PublicKey = variablelengthField(Field.PublicKey);
    static public TypedFields.VariableLengthField MessageKey = variablelengthField(Field.MessageKey);
    static public TypedFields.VariableLengthField SigningPubKey = variablelengthField(Field.SigningPubKey);
    static public TypedFields.VariableLengthField TxnSignature = variablelengthField(Field.TxnSignature);
    static public TypedFields.VariableLengthField Generator = variablelengthField(Field.Generator);
    static public TypedFields.VariableLengthField Signature = variablelengthField(Field.Signature);
    static public TypedFields.VariableLengthField Domain = variablelengthField(Field.Domain);
    static public TypedFields.VariableLengthField FundCode = variablelengthField(Field.FundCode);
    static public TypedFields.VariableLengthField RemoveCode = variablelengthField(Field.RemoveCode);
    static public TypedFields.VariableLengthField ExpireCode = variablelengthField(Field.ExpireCode);
    static public TypedFields.VariableLengthField CreateCode = variablelengthField(Field.CreateCode);

}
