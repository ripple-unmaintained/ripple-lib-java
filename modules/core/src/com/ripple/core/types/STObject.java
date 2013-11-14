package com.ripple.core.types;

import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.FieldSymbolics;
import com.ripple.core.fields.HasField;
import com.ripple.core.fields.Type;
import com.ripple.core.formats.Format;
import com.ripple.core.formats.SLEFormat;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.serialized.*;
import com.ripple.core.types.hash.Hash128;
import com.ripple.core.types.hash.Hash160;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt16;
import com.ripple.core.types.uint.UInt32;
import com.ripple.core.types.uint.UInt64;
import com.ripple.core.types.uint.UInt8;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.TreeMap;

public class STObject implements SerializedType, Iterable<Field> {
    private TreeMap<Field, SerializedType> fields = new TreeMap<Field, SerializedType>();

    public String toHex() {
        return translate.toHex(this);
    }
    public byte[] toWireBytes() {
        return translate.toWireBytes(this);
    }
    public static STObject fromJSONObject(JSONObject json) {
        return translate.fromJSONObject(json);
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }
    public TransactionEngineResult transactionResult() {
        UInt8 uInt8 = get(UInt8.TransactionResult);
        return TransactionEngineResult.fromNumber(uInt8.intValue());
    }
    public LedgerEntryType ledgerEntryType() {
        UInt16 uInt16 = get(UInt16.LedgerEntryType);
        return LedgerEntryType.fromNumber(uInt16.intValue());
    }
    public TransactionType transactionType() {
        UInt16 uInt16 = get(UInt16.TransactionType);
        return TransactionType.fromNumber(uInt16.intValue());
    }
    public Format format;

    public SerializedType remove(Field f) {
        return fields.remove(f);
    }

    public boolean has(Field f) {
        return fields.containsKey(f);
    }

    public <T extends HasField> boolean has(T hf) {
        return has(hf.getField());
    }

    public void validate() throws RuntimeException {

    }

    @Override
    public Iterator<Field> iterator() {
        return fields.keySet().iterator();
    }

    public SerializedType get(Field field) {
        return fields.get(field);
    }

    public static class Translator extends TypeTranslator<STObject> {

        @Override
        public STObject fromParser(BinaryParser parser, Integer hint) {
            byte objectEnd = Markers.OBJECT_END;
            STObject so = new STObject();
            TypeTranslator<SerializedType> tr;
            SerializedType st;
            Field field;
            Integer sizeHint;
            Integer fieldsToParse = hint != null ? hint : 0;

            while (parser.notConsumedOrAtMarker(objectEnd)) {
                int fieldCode = parser.readFieldCode();
                field = Field.fromCode(fieldCode);
                if (field == null) {
                    throw new IllegalStateException("Couldn't parse field from " +
                            Integer.toHexString(fieldCode));
                }
                tr = Translators.forField(field);
                sizeHint = field.isVLEncoded() ? parser.readVLLength() : null;
                st = tr.fromParser(parser, sizeHint);
                if (st == null) {
                    throw new IllegalStateException("Parsed " + field + "as null");
                }
                so.put(field, st);
                if (so.size() == fieldsToParse) break;
            }

            parser.safelyAdvancePast(objectEnd);
            return so;
        }

        @Override
        public Object toJSON(STObject obj) {
            return toJSONObject(obj);
        }

        @Override
        public JSONObject toJSONObject(STObject obj) {
            JSONObject json = new JSONObject();

            for (Field f : obj) {
                TypeTranslator<SerializedType> ts = Translators.forField(f);

                try {
                    SerializedType obj1 = obj.get(f);

                    Object object = ts.toJSON(obj1);
                    if (FieldSymbolics.isSymbolicField(f) && object instanceof Number) {
                        object = FieldSymbolics.asString(f, ((Number) object).intValue());
                    }
                    json.put(f.name(), object);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            return json;
        }

        @Override
        public STObject fromJSONObject(JSONObject jsonObject) {
            STObject so = STObject.newInstance();

            Iterator keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    Object value = jsonObject.get(key);
                    Field fieldKey = null;
                    try {
                        fieldKey = Field.valueOf(key);
                    } catch (IllegalArgumentException e) {
                        fieldKey = null;
                    }
                    if (fieldKey == null) {
                        continue;
//                        throw new RuntimeException("Unknown message " + key);
                    } else if (fieldKey == Field.TransactionType) {
                        TxFormat format = TxFormat.fromValue(value);

                        if (format == null) {
                            throw new RuntimeException("Value (un)specified for TransactionType is invalid. " +
                                    "Must be string or int mapping to a TxFormat");
                        } else {
                            so.setFormat(format);
                            value = format.transactionType.asInteger();
                        }

                    } else if (fieldKey == Field.LedgerEntryType) {
                        SLEFormat format = SLEFormat.fromValue(value);

                        if (format == null) {
                            throw new RuntimeException("Value specified for LedgerEntryType is invalid. " +
                                    "Must be string or int mapping to a SLEFormat");
                        } else {
                            so.setFormat(format);
                            value = format.ledgerEntryType.asInteger();
                        }
                    } else if (fieldKey == Field.TransactionResult && value instanceof String) {
                        value = TransactionEngineResult.valueOf((String) value).asInteger();

                    }
                    so.put(fieldKey, value);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            return so;
        }

        @Override
        public void toBytesTree(STObject obj, BytesTree to) {
            BinarySerializer serializer = new BinarySerializer(to);

            for (Field field : obj) {
                if (field.isSerialized()) {
                    SerializedType value = obj.fields.get(field);
                    serializer.add(field, value, Translators.forField(field));
                }
            }
        }
    }

    public int size() {
        return fields.size();
    }

    static public Translator translate = new Translator();

    protected STObject() {
    }

    public static STObject newInstance() {
        return new STObject();
    }


    private abstract static class STObjectField extends STObject implements HasField{}

    public static STObjectField stobjectField(final Field f) {
        return new STObjectField() {@Override public Field getField() {return f; } };
    }

    static public STObjectField TransactionMetaData = stobjectField(Field.TransactionMetaData);
    static public STObjectField CreatedNode = stobjectField(Field.CreatedNode);
    static public STObjectField DeletedNode = stobjectField(Field.DeletedNode);
    static public STObjectField ModifiedNode = stobjectField(Field.ModifiedNode);
    static public STObjectField PreviousFields = stobjectField(Field.PreviousFields);
    static public STObjectField FinalFields = stobjectField(Field.FinalFields);
    static public STObjectField NewFields = stobjectField(Field.NewFields);
    static public STObjectField TemplateEntry = stobjectField(Field.TemplateEntry);

    public <T extends HasField> void put(T f, Object value) {
        if (value instanceof String) {
            put(f, (String) value);
        } else {
            put(f.getField(), value);
        }
    }

    public <T extends HasField> void put(T hf, Integer i) {
        put(hf.getField(), i);
    }

    public void put(Field f, Integer i) {
        fields.put(f, Translators.forField(f).fromInteger(i));
    }

    public <T extends HasField> void put(T hf, String s) {
        put(hf.getField(), s);
    }

//    public <T extends HasField> void put(T hf, SerializedType s) {
//        put(hf.getField(), s);
//    }

    public <T extends HasField> void put(T hf, byte [] bytes) {
        Field f = hf.getField();
        put(f, bytes);
    }

    private void put(Field f, byte[] bytes) {
        // TODO, all!!!
        fields.put(f, Translators.forField(f).fromParser(new BinaryParser(bytes)));
    }

    public void put(Field f, String s) {
        if (FieldSymbolics.isSymbolicField(f)) {
            put(f, FieldSymbolics.asInteger(f, s));
            return;
        }

        fields.put(f, Translators.forField(f).fromString(s));
    }

    public void put(Field f, SerializedType value) {
        fields.put(f, value);
    }

    public void put(Field f, Object value) {
        TypeTranslator typeTranslator = Translators.forField(f);
        SerializedType value1 = null;
        try {
            value1 = typeTranslator.fromValue(value);
        } catch (Exception e) {

            throw new RuntimeException("Couldn't put `" +value+ "` into field `" + f + "`\n" + e.toString());
        }
        fields.put(f, value1);
    }

    public <T extends AccountID.AccountIDField> AccountID get(T f) {
        return (AccountID) fields.get(f.getField());
    }

    public <T extends Amount.AmountField> Amount get(T f) {
        return (Amount) fields.get(f.getField());
    }

    public <T extends STArray.STArrayField> STArray get(T f) {
        return (STArray) fields.get(f.getField());
    }

    public <T extends Hash128.Hash128Field> Hash128 get(T f) {
        return (Hash128) fields.get(f.getField());
    }

    public <T extends Hash160.Hash160Field> Hash160 get(T f) {
        return (Hash160) fields.get(f.getField());
    }

    public <T extends Hash256.Hash256Field> Hash256 get(T f) {
        return (Hash256) fields.get(f.getField());
    }

    public <T extends STObject.STObjectField> STObject get(T f) {
        return (STObject) fields.get(f.getField());
    }

    public <T extends PathSet.PathSetField> PathSet get(T f) {
        return (PathSet) fields.get(f.getField());
    }

    public <T extends UInt16.UInt16Field> UInt16 get(T f) {
        return (UInt16) fields.get(f.getField());
    }

    public <T extends UInt32.UInt32Field> UInt32 get(T f) {
        return (UInt32) fields.get(f.getField());
    }

    public <T extends UInt64.UInt64Field> UInt64 get(T f) {
        return (UInt64) fields.get(f.getField());
    }

    public <T extends UInt8.UInt8Field> UInt8 get(T f) {
        return (UInt8) fields.get(f.getField());
    }

    public <T extends Vector256.Vector256Field> Vector256 get(T f) {
        return (Vector256) fields.get(f.getField());
    }

    public <T extends VariableLength.VariableLengthField> VariableLength get(T f) {
        return (VariableLength) fields.get(f.getField());
    }

    public static class Translators {
        public static TypeTranslator forType(Type type) {
            switch (type) {
                case OBJECT:     return translate;

                case AMOUNT:     return Amount.translate;
                case UINT16:     return UInt16.translate;
                case UINT32:     return UInt32.translate;
                case UINT64:     return UInt64.translate;
                case HASH128:    return Hash128.translate;
                case HASH256:    return Hash256.translate;
                case VL:         return VariableLength.translate;
                case ACCOUNT:    return AccountID.translate;
                case ARRAY:      return STArray.translate;
                case UINT8:      return UInt8.translate;
                case HASH160:    return Hash160.translate;
                case PATHSET:    return PathSet.translate;
                case VECTOR256:  return Vector256.translate;
                default:         throw new RuntimeException("Unknown type");
            }
        }

        public static TypeTranslator<SerializedType> forField(Field field) {
            if (field.tag == null) {
                field.tag = forType(field.getType());
            }
            return getCastedTag(field);
        }

        @SuppressWarnings("unchecked")
        private static TypeTranslator<SerializedType> getCastedTag(Field field) {
            return (TypeTranslator<SerializedType>) field.tag;
        }
    }
}
