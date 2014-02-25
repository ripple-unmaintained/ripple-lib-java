package com.ripple.core.types.known.tx.result;

import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.fields.Field;

public class TransactionMeta extends STObject{
    public static boolean isTransactionMeta(STObject source) {
        return source.has(UInt8.TransactionResult) &&
               source.has(Field.AffectedNodes);
    }

    public TransactionEngineResult transactionResult() {
        return transactionResult(this);
    }
}
