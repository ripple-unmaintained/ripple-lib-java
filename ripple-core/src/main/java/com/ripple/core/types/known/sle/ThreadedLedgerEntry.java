package com.ripple.core.types.known.sle;


import com.ripple.core.types.known.tx.result.TransactionResult;

// this class has a PreviousTxnID and PreviousTxnLgrSeq
abstract public class ThreadedLedgerEntry extends LedgerEntry {
    public void updateFromTransactionResult(TransactionResult tr) {

    }
}
