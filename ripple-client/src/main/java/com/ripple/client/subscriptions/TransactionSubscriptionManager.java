package com.ripple.client.subscriptions;
import com.ripple.core.types.known.tx.result.TransactionResult;

public interface TransactionSubscriptionManager {
    public void notifyTransactionResult(TransactionResult tr);
}
