package com.ripple.core.types.shamap;
import com.ripple.core.types.known.tx.result.TransactionResult;

public interface TransactionResultVisitor {
    public void onTransaction(TransactionResult tx);
}
