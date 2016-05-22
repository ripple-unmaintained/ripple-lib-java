package com.ripple.core.types.known.tx.result;

import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.fields.Field;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.types.known.sle.LedgerEntry;

// TODO: fix up this nonsense
public class AffectedNode extends STObject {
    public final Field field;
    public final STObject nested;

    public AffectedNode(STObject source) {
        fields = source.getFields();
        field = getField();
        nested = nestedObject();
    }

    public boolean isOffer() {
        return ledgerEntryType() == LedgerEntryType.Offer;
    }

    public boolean isAccountRoot() {
        return ledgerEntryType() == LedgerEntryType.AccountRoot;
    }

    public boolean isRippleState() {
        return ledgerEntryType() == LedgerEntryType.RippleState;
    }

    public boolean isDirectoryNode() {
        return ledgerEntryType() == LedgerEntryType.DirectoryNode;
    }

    public boolean wasPreviousNode() {
        return isDeletedNode() || isModifiedNode();
    }

    public boolean isFinalNode() {
        return true;
    }

    public boolean isCreatedNode() {
        return field == Field.CreatedNode;
    }

    public boolean isDeletedNode() {
        return field == Field.DeletedNode;
    }

    public boolean isModifiedNode() {
        return field == Field.ModifiedNode;
    }

    public Field getField() {
        return fields.firstKey();
    }

    public Hash256 ledgerIndex() {
        return nested.get(Hash256.LedgerIndex);
    }

    public LedgerEntryType ledgerEntryType() {
        return ledgerEntryType(nested);
    }

    private STObject nestedObject() {
        return (STObject) get(getField());
    }

    public LedgerEntry nodeAsPrevious() {
        return (LedgerEntry) rebuildFromMeta(true);
    }

    public LedgerEntry nodeAsFinal() {
        return (LedgerEntry) rebuildFromMeta(false);
    }

    private STObject rebuildFromMeta(boolean layerPrevious) {
        STObject mixed = new STObject();
        boolean created = isCreatedNode();

        Field wrapperField = created ? Field.CreatedNode :
                isDeletedNode() ? Field.DeletedNode :
                        Field.ModifiedNode;

        STObject wrapped = (STObject) get(wrapperField);

        Field finalFields = created ? Field.NewFields :
                Field.FinalFields;

        if (!wrapped.has(finalFields)) {
            STObject source = new STObject(wrapped.getFields());
            source.put(Hash256.index, wrapped.get(Hash256.LedgerIndex));
            return STObject.formatted(source);
        }

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
                    SerializedType value = wrapped.get(field);

                    if (field == Field.LedgerIndex) {
                        field = Field.index;
                    }
                    mixed.put(field, value);

            }
        }
        return STObject.formatted(mixed);
    }

    public static boolean isAffectedNode(STObject source) {
        return (source.size() == 1 && (
                source.has(DeletedNode) ||
                source.has(CreatedNode) ||
                source.has(ModifiedNode)));
    }
}
