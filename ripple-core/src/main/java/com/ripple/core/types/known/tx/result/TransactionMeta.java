package com.ripple.core.types.known.tx.result;

import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.serialized.enums.EngineResult;
import com.ripple.core.types.known.sle.LedgerEntry;

import java.util.Iterator;

public class TransactionMeta extends STObject {
    public static boolean isTransactionMeta(STObject source) {
        return source.has(UInt8.TransactionResult) &&
                source.has(STArray.AffectedNodes);
    }

    public EngineResult engineResult() {
        return engineResult(this);
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

    public void walkPrevious(LedgerEntry.OnLedgerEntry cb) {
        for (AffectedNode affectedNode : affectedNodes()) {
            if (affectedNode.wasPreviousNode()) {
                cb.onObject(affectedNode.nodeAsPrevious());
            }
        }
    }
    public void walkFinal(LedgerEntry.OnLedgerEntry cb) {
        for (AffectedNode affectedNode : affectedNodes()) {
            if (affectedNode.isFinalNode()) {
                cb.onObject(affectedNode.nodeAsFinal());
            }
        }
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
