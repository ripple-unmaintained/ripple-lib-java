package com.ripple.core.types;

import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesTree;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;
import org.bouncycastle.util.encoders.Hex;

public class VariableLength implements SerializedType {
    public VariableLength(byte[] bytes) {
        buffer = bytes;
    }

    byte[] buffer;

    static class Translator extends TypeTranslator<VariableLength> {
        @Override
        public VariableLength fromWireBytes(BinaryParser parser) {
            return null;
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

//        @Override
//        public byte[] toWireBytes(VariableLength obj) {
//            return obj.buffer;
//        }

        @Override
        public void toWireBytes(VariableLength obj, BytesTree to) {
            to.add(obj.buffer);
        }
    }
    static public Translator translate = new Translator();

    private VariableLength(){}
    
    abstract static class VariableLengthField extends VariableLength implements HasField{}
    public static VariableLengthField variablelengthField(final Field f) {
        return new VariableLengthField(){ @Override public Field getField() {return f;}};
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
    
    
}
