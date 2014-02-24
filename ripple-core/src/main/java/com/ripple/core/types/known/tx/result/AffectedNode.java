package com.ripple.core.types.known.tx.result;

import com.ripple.core.coretypes.STObject;
import com.ripple.core.fields.Field;

public class AffectedNode extends STObject {
    public boolean wasPreviousNode() {
        return isDeletedNode() || isModifiedNode();
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

    public static boolean isAffectedNode(STObject source) {
        return (source.size() == 1 && source.has(DeletedNode) ||
                                  source.has(CreatedNode) ||
                                  source.has(ModifiedNode));
    }
}
