package com.ripple.core.types.known.tx.result;

import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.uint.UInt;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.fields.Field;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.entries.RippleState;

import java.util.Iterator;

public class TransactionMeta extends STObject {
    public static boolean isTransactionMeta(STObject source) {
        return source.has(UInt8.TransactionResult) &&
                source.has(STArray.AffectedNodes);
    }

    public TransactionEngineResult transactionResult() {
        return transactionResult(this);
    }

    public Iterable<AffectedNode> affectedNodes() {
        STArray nodes = get(STArray.AffectedNodes);
        final Iterator<STObject> iterator = nodes.iterator();
        return new Iterable<AffectedNode>() {
            @Override
            public Iterator<AffectedNode> iterator() {
                return iterateAffectedNodes(iterator);
            }
        };
    }

    public static Iterator<AffectedNode> iterateAffectedNodes(final Iterator<STObject> iterator) {
        return new Iterator<AffectedNode>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public AffectedNode next() {
                return (AffectedNode) iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    public UInt32 transactionIndex() {
        return get(UInt32.TransactionIndex);
    }
}
