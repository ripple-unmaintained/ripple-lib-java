package com.ripple.core.coretypes;

import com.ripple.core.coretypes.hash.Hash128;
import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.serialized.enums.EngineResult;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.HasField;
import com.ripple.core.fields.Type;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.formats.Format;
import com.ripple.core.formats.LEFormat;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.serialized.*;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.LedgerHashes;
import com.ripple.core.types.known.sle.entries.AccountRoot;
import com.ripple.core.types.known.sle.entries.DirectoryNode;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.sle.entries.RippleState;
import com.ripple.core.types.known.tx.TicketCancel;
import com.ripple.core.types.known.tx.TicketCreate;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.result.AffectedNode;
import com.ripple.core.types.known.tx.result.TransactionMeta;
import com.ripple.core.types.known.tx.txns.*;
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

    public FieldsMap getFields() {
        return fields;
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

    public static STObject formatted(STObject source) {
        if (AffectedNode.isAffectedNode(source)) {
            return new AffectedNode(source);
        }

        if (TransactionMeta.isTransactionMeta(source)) {
            TransactionMeta meta = new TransactionMeta();
            meta.fields = source.fields;
            return meta;
        }

        LedgerEntryType ledgerEntryType = ledgerEntryType(source);
        if (ledgerEntryType != null) {
            return ledgerFormatted(source, ledgerEntryType);
        }

        TransactionType transactionType = transactionType(source);
        if (transactionType != null) {
            return transactionFormatted(source, transactionType);
        }

        return source;
    }

    private static STObject transactionFormatted(STObject source, TransactionType transactionType) {
        STObject constructed = null;
        switch (transactionType) {
            case Invalid:
                break;
            case Payment:
                constructed = new Payment();
                break;
            case Claim:
                break;
            case WalletAdd:
                break;
            case AccountSet:
                constructed = new AccountSet();
                break;
            case PasswordFund:
                break;
            case SetRegularKey:
                break;
            case NickNameSet:
                break;
            case OfferCreate:
                constructed = new OfferCreate();
                break;
            case OfferCancel:
                constructed = new OfferCancel();
                break;
            case Contract:
                break;
            case TicketCreate:
                constructed = new TicketCreate();
                break;
            case TicketCancel:
                constructed = new TicketCancel();
                break;
            case TrustSet:
                constructed = new TrustSet();
                break;
            case Amendment:
                break;
            case SetFee:
                break;

        }
        if (constructed == null) {
            constructed = new Transaction(transactionType);
        }

        constructed.fields = source.fields;
        return constructed;

    }

    private static STObject ledgerFormatted(STObject source, LedgerEntryType ledgerEntryType) {
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
                constructed = new DirectoryNode();
                break;
            case GeneratorMap:
                break;
            case Contract:
                break;
            case LedgerHashes:
                constructed = new LedgerHashes();
                break;
            case EnabledAmendments:
                break;
            case FeeSettings:
                break;
        }
        if (constructed == null) {
            constructed = new LedgerEntry(ledgerEntryType);
        }
        constructed.fields = source.fields;
        return constructed;
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
            setFormat(LEFormat.fromNumber(let));
        }
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public static EngineResult engineResult(STObject obj) {
        return (EngineResult) obj.get(Field.TransactionResult);
    }

    static public LedgerEntryType ledgerEntryType(STObject obj) {
        return (LedgerEntryType) obj.get(Field.LedgerEntryType);
    }

    public static TransactionType transactionType(STObject obj) {
        return (TransactionType) obj.get(Field.TransactionType);
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

            // hint, is how many bytes to parse
            if (hint != null) {
                hint = parser.pos() + hint;
            }

            while (!(parser.end() || hint != null && parser.pos() >= hint)) {
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
                        // TODO test for UpperCase key name && warn about possibly unknown field
                        continue;
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
        put(f, Translators.forField(f).fromBytes(bytes));
    }

    public void put(Field f, SerializedType value) {
        fields.put(f, value);
    }

    public void put(Field f, Object value) {
        TypeTranslator typeTranslator = Translators.forField(f);
        SerializedType st = null;
        try {
            st = typeTranslator.fromValue(value);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't put `" +value+ "` into field `" + f + "`\n" + e.toString());
        }
        fields.put(f, st);
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

    public EngineResult get(TypedFields.EngineResultField f) {
        return (EngineResult) get(f.getField());
    }

    public Vector256 get(TypedFields.Vector256Field f) {
        return (Vector256) get(f.getField());
    }

    public VariableLength get(TypedFields.VariableLengthField f) {
        return (VariableLength) get(f.getField());
    }

    public static class Translators {
        private static TypeTranslator forType(Type type) {
            switch (type) {

                case STObject:      return translate;
                case Amount:        return Amount.translate;
                case UInt16:        return UInt16.translate;
                case UInt32:        return UInt32.translate;
                case UInt64:        return UInt64.translate;
                case Hash128:       return Hash128.translate;
                case Hash256:       return Hash256.translate;
                case VariableLength:return VariableLength.translate;
                case AccountID:     return AccountID.translate;
                case STArray:       return STArray.translate;
                case UInt8:         return UInt8.translate;
                case Hash160:       return Hash160.translate;
                case PathSet:       return PathSet.translate;
                case Vector256:     return Vector256.translate;

                default:            throw new RuntimeException("Unknown type");
            }
        }

        public static TypeTranslator<SerializedType> forField(Field field) {
            if (field.tag == null) {
                switch (field) {
/*
                    case CloseTime:
                    case ParentCloseTime:
                    case SigningTime:
                        break;
*/
                    case LedgerEntryType:
                        field.tag = LedgerEntryType.translate;
                        break;
                    case TransactionType:
                        field.tag = TransactionType.translate;
                        break;
                    case TransactionResult:
                        field.tag = EngineResult.translate;
                        break;
                    default:
                        field.tag = forType(field.getType());
                        break;
                }
            }
            return getCastedTag(field);
        }

        @SuppressWarnings("unchecked")
        private static TypeTranslator<SerializedType> getCastedTag(Field field) {
            return (TypeTranslator<SerializedType>) field.tag;
        }
    }
}
