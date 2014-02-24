package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash128;
import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.*;
import com.ripple.core.formats.Format;
import com.ripple.core.formats.SLEFormat;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.serialized.*;
import com.ripple.core.types.known.sle.entries.AccountRoot;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.sle.entries.RippleState;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.TreeMap;

public class STObject implements SerializedType, Iterable<Field> {
    public static STObject fromJSON(String offerJson) {
        try {
            return fromJSONObject(new JSONObject(offerJson));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean wasPreviousNode() {
        return isDeletedNode() || isModifiedNode();
    }

    public static class FieldsMap extends TreeMap<Field, SerializedType> {}

    public String prettyJSON() {
        try {
            return translate.toJSONObject(this).toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected FieldsMap fields;
    public Format format;

    public STObject() {
        fields = new FieldsMap();
    }
    public STObject(FieldsMap fieldsMap) {
        fields = fieldsMap;
    }

    public static STObject newInstance() {
        return new STObject();
    }

    public boolean isCreatedNode() {
        return fields.size() == 1 && has(Field.CreatedNode);
    }

    public boolean isDeletedNode() {
        return fields.size() == 1 && has(Field.DeletedNode);
    }

    public boolean isModifiedNode() {
        return fields.size() == 1 && has(Field.ModifiedNode);
    }

    public STObject nodeAsPrevious() {
        return rebuildFromMeta(true);
    }

    public STObject nodeAsFinal() {
        return rebuildFromMeta(false);
    }

    public STObject rebuildFromMeta(boolean layerPrevious) {
        STObject mixed = new STObject();
        boolean created = isCreatedNode();

        Field wrapperField = created ? Field.CreatedNode :
                             isDeletedNode() ? Field.DeletedNode :
                                               Field.ModifiedNode;

        STObject wrapped = (STObject) get(wrapperField);

        Field finalFields = created ? Field.NewFields :
                                      Field.FinalFields;

        STObject finals = (STObject) wrapped.get(finalFields);
        for (Field field : finals) {
            mixed.put(field, finals.get(field));
        }

        // DirectoryNode LedgerEntryType won't have `PreviousFields`
        if (layerPrevious && wrapped.has(Field.PreviousFields)) {
            STObject previous = wrapped.get(STObject.PreviousFields);
            STObject changed = new STObject();
            mixed.put(Field.FinalFields, changed);

            for (Field field : previous) {
                mixed.put(field, previous.get(field));
                changed.put(field, finals.get(field));
            }
        }

        for (Field field : wrapped) {
            switch (field) {
                case NewFields:
                case PreviousFields:
                case FinalFields:
                    continue;
                default:
                    mixed.put(field, wrapped.get(field));

            }
        }
        return STObject.formatted(mixed);
    }


    public static STObject formatted(STObject source) {
        LedgerEntryType ledgerEntryType = source.ledgerEntryType();
        if (ledgerEntryType == null) {
            return source;
        }

        STObject constructed = null;
        switch (ledgerEntryType) {
            case Offer:
                constructed = new Offer();
                break;
            case RippleState:
                constructed = new RippleState();
                break;
            case AccountRoot:
                constructed = new AccountRoot();
                break;
            case Invalid:
                break;
            case DirectoryNode:
                break;
            case GeneratorMap:
                break;
            case Nickname:
                break;
            case Contract:
                break;
            case LedgerHashes:
                break;
            case EnabledFeatures:
                break;
            case FeeSettings:
                break;
        }
        if (constructed == null) {
            return source;
        }  else {
            // getFormat() may get the Format from the fields
            constructed.setFormat(source.getFormat());
            constructed.fields = source.stealFields();
            return constructed;
        }
    }

    @Override
    public Object toJSON() {
        return translate.toJSON(this);
    }

    public JSONObject toJSONObject() {
        return translate.toJSONObject(this);
    }

    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    // There's no nice predicates
    public static interface FieldFilter {
        boolean evaluate(Field a);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        toBytesSink(to, new FieldFilter() {
            @Override
            public boolean evaluate(Field field) {
                return field.isSerialized();
            }
        });
    }

    public void toBytesSink(BytesSink to, FieldFilter p) {
        BinarySerializer serializer = new BinarySerializer(to);

        for (Field field : this) {
            if (p.evaluate(field)) {
                SerializedType value = fields.get(field);
                serializer.add(field, value);
            }
        }
    }

    public static STObject fromJSONObject(JSONObject json) {
        return translate.fromJSONObject(json);
    }

    public FieldsMap stealFields() {
        /*Steal the fields map*/
        FieldsMap stolen = fields;
        /*Any subsequent usage will blow up*/
        fields = null;
        return stolen;
    }

    public Format getFormat() {
        if (format == null) computeFormat();
        return format;
    }

    private void computeFormat() {
        UInt16 tt = get(UInt16.TransactionType);
        if (tt != null) {
            setFormat(TxFormat.fromNumber(tt));
        }
        UInt16 let = get(UInt16.LedgerEntryType);
        if (let != null) {
            setFormat(SLEFormat.fromNumber(let));
        }
    }

    public void setFormat(Format format) {
        this.format = format;
    }
    public TransactionEngineResult transactionResult() {
        UInt8 uInt8 = get(UInt8.TransactionResult);
        if (uInt8 == null) {
            return null;
        }
        return TransactionEngineResult.fromNumber(uInt8.intValue());
    }
    public LedgerEntryType ledgerEntryType() {
        UInt16 uInt16 = get(UInt16.LedgerEntryType);
        if (uInt16 == null) {
            return null;
        }
        return LedgerEntryType.fromNumber(uInt16);
    }
    public TransactionType transactionType() {
        UInt16 uInt16 = get(UInt16.TransactionType);
        if (uInt16 == null) {
            return null;
        }
        return TransactionType.fromNumber(uInt16);
    }

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
            STObject so = new STObject();
            TypeTranslator<SerializedType> tr;
            SerializedType st;
            Field field;
            Integer sizeHint;

            while (!parser.end()) {
                field = parser.readField();
                if (field == Field.ObjectEndMarker) {
                    break;
                }
                tr = Translators.forField(field);
                sizeHint = field.isVLEncoded() ? parser.readVLLength() : null;
                st = tr.fromParser(parser, sizeHint);
                if (st == null) {
                    throw new IllegalStateException("Parsed " + field + "as null");
                }
                so.put(field, st);
            }

            return STObject.formatted(so);
        }

        @Override
        public Object toJSON(STObject obj) {
            return toJSONObject(obj);
        }

        @Override
        public JSONObject toJSONObject(STObject obj) {
            JSONObject json = new JSONObject();

            for (Field f : obj) {
                try {
                    SerializedType obj1 = obj.get(f);
                    Object object = obj1.toJSON();

                    if (FieldSymbolics.isSymbolicField(f) && object instanceof Number) {
                        object = FieldSymbolics.asString(f, (Number) object);
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
            STObject so = new STObject();

            Iterator keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    Object value   = jsonObject.get(key);
                    Field fieldKey = Field.fromString(key);
                    if (fieldKey == null) {
                        // TODO test for UpperCase key name
                        // warn about possibly unknown field
                        continue;
                    }
                    if (FieldSymbolics.isSymbolicField(fieldKey) && value instanceof String) {
                        value = FieldSymbolics.asInteger(fieldKey, (String) value);
                    }
                    so.put(fieldKey, value);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }            }
            return STObject.formatted(so);
        }
    }

    public int size() {
        return fields.size();
    }

    static public Translator translate = new Translator();

    public static TypedFields.STObjectField stobjectField(final Field f) {
        return new TypedFields.STObjectField() {@Override public Field getField() {return f; } };
    }

    static public TypedFields.STObjectField TransactionMetaData = stobjectField(Field.TransactionMetaData);
    static public TypedFields.STObjectField CreatedNode = stobjectField(Field.CreatedNode);
    static public TypedFields.STObjectField DeletedNode = stobjectField(Field.DeletedNode);
    static public TypedFields.STObjectField ModifiedNode = stobjectField(Field.ModifiedNode);
    static public TypedFields.STObjectField PreviousFields = stobjectField(Field.PreviousFields);
    static public TypedFields.STObjectField FinalFields = stobjectField(Field.FinalFields);
    static public TypedFields.STObjectField NewFields = stobjectField(Field.NewFields);
    static public TypedFields.STObjectField TemplateEntry = stobjectField(Field.TemplateEntry);

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
        put(f, Translators.forField(f).fromInteger(i));
    }

    public <T extends HasField> void put(T hf, String s) {
        put(hf.getField(), s);
    }

    public <T extends HasField> void put(T hf, byte [] bytes) {
        Field f = hf.getField();
        put(f, bytes);
    }

    private void put(Field f, byte[] bytes) {
        // TODO, all!!!
        put(f, Translators.forField(f).fromBytes(bytes));
    }

    public void put(Field f, String s) {
        if (FieldSymbolics.isSymbolicField(f)) {
            put(f, FieldSymbolics.asInteger(f, s));
            return;
        }

        put(f, Translators.forField(f).fromString(s));
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

    public AccountID get(TypedFields.AccountIDField f) {
        return (AccountID) get(f.getField());
    }

    public Amount get(TypedFields.AmountField f) {
        return (Amount) get(f.getField());
    }

    public STArray get(TypedFields.STArrayField f) {
        return (STArray) get(f.getField());
    }

    public Hash128 get(TypedFields.Hash128Field f) {
        return (Hash128) get(f.getField());
    }

    public Hash160 get(TypedFields.Hash160Field f) {
        return (Hash160) get(f.getField());
    }

    public Hash256 get(TypedFields.Hash256Field f) {
        return (Hash256) get(f.getField());
    }

    public STObject get(TypedFields.STObjectField f) {
        return (STObject) get(f.getField());
    }

    public PathSet get(TypedFields.PathSetField f) {
        return (PathSet) get(f.getField());
    }

    public UInt16 get(TypedFields.UInt16Field f) {
        return (UInt16) get(f.getField());
    }

    public UInt32 get(TypedFields.UInt32Field f) {
        return (UInt32) get(f.getField());
    }

    public UInt64 get(TypedFields.UInt64Field f) {
        return (UInt64) get(f.getField());
    }

    public UInt8 get(TypedFields.UInt8Field f) {
        return (UInt8) get(f.getField());
    }

    public Vector256 get(TypedFields.Vector256Field f) {
        return (Vector256) get(f.getField());
    }

    public VariableLength get(TypedFields.VariableLengthField f) {
        return (VariableLength) get(f.getField());
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
