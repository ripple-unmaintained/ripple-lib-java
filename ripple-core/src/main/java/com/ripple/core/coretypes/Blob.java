
package com.ripple.core.coretypes;

import com.ripple.core.fields.BlobField;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.Type;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;
import org.ripple.bouncycastle.util.encoders.Hex;

public class Blob implements SerializedType {
    public Blob(byte[] bytes) {
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

    @Override
    public Type type() {
        return Type.Blob;
    }

    public static Blob fromBytes(byte[] bytes) {
        return new Blob(bytes);
    }

    public static class Translator extends TypeTranslator<Blob> {
        @Override
        public Blob fromParser(BinaryParser parser, Integer hint) {
            if (hint == null) {
                hint = parser.size() - parser.pos();
            }
            return new Blob(parser.read(hint));
        }

        @Override
        public Object toJSON(Blob obj) {
            return toString(obj);
        }

        @Override
        public String toString(Blob obj) {
            return B16.toString(obj.buffer);
        }

        @Override
        public Blob fromString(String value) {
            return new Blob(Hex.decode(value));
        }

        @Override
        public void toBytesSink(Blob obj, BytesSink to) {
            to.add(obj.buffer);
        }
    }

    static public Translator translate = new Translator();

    public static BlobField blobField(final Field f) {
        return new BlobField() {
            @Override
            public Field getField() {
                return f;
            }
        };
    }

    static public BlobField PublicKey = blobField(Field.PublicKey);
    static public BlobField MessageKey = blobField(Field.MessageKey);
    static public BlobField SigningPubKey = blobField(Field.SigningPubKey);
    static public BlobField TxnSignature = blobField(Field.TxnSignature);
    static public BlobField Signature = blobField(Field.Signature);
    static public BlobField Domain = blobField(Field.Domain);
    static public BlobField FundCode = blobField(Field.FundCode);
    static public BlobField RemoveCode = blobField(Field.RemoveCode);
    static public BlobField ExpireCode = blobField(Field.ExpireCode);
    static public BlobField CreateCode = blobField(Field.CreateCode);

    static public BlobField MemoType = blobField(Field.MemoType);
    static public BlobField MemoData = blobField(Field.MemoData);
    static public BlobField MemoFormat = blobField(Field.MemoFormat);
}
