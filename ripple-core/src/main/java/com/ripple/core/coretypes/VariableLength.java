
package com.ripple.core.coretypes;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.VariableLengthField;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;
import org.ripple.bouncycastle.util.encoders.Hex;

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
    public byte[] toBytes() {
        return buffer;
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        translate.toBytesSink(this, to);
    }

    public static VariableLength fromBytes(byte[] bytes) {
        return new VariableLength(bytes);
    }

    public static class Translator extends TypeTranslator<VariableLength> {
        @Override
        public VariableLength fromParser(BinaryParser parser, Integer hint) {
            if (hint == null) {
                hint = parser.size() - parser.pos();
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
        public void toBytesSink(VariableLength obj, BytesSink to) {
            to.add(obj.buffer);
        }
    }

    static public Translator translate = new Translator();

    public static VariableLengthField variablelengthField(final Field f) {
        return new VariableLengthField() {
            @Override
            public Field getField() {
                return f;
            }
        };
    }

    static public VariableLengthField PublicKey = variablelengthField(Field.PublicKey);
    static public VariableLengthField MessageKey = variablelengthField(Field.MessageKey);
    static public VariableLengthField SigningPubKey = variablelengthField(Field.SigningPubKey);
    static public VariableLengthField TxnSignature = variablelengthField(Field.TxnSignature);
    static public VariableLengthField Generator = variablelengthField(Field.Generator);
    static public VariableLengthField Signature = variablelengthField(Field.Signature);
    static public VariableLengthField Domain = variablelengthField(Field.Domain);
    static public VariableLengthField FundCode = variablelengthField(Field.FundCode);
    static public VariableLengthField RemoveCode = variablelengthField(Field.RemoveCode);
    static public VariableLengthField ExpireCode = variablelengthField(Field.ExpireCode);
    static public VariableLengthField CreateCode = variablelengthField(Field.CreateCode);

    static public VariableLengthField MemoType = variablelengthField(Field.MemoType);
    static public VariableLengthField MemoData = variablelengthField(Field.MemoData);
    static public VariableLengthField MemoFormat = variablelengthField(Field.MemoFormat);
}
