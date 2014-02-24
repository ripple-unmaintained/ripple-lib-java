package com.ripple.core.types.known.tx.txns;

import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;

public class Payment extends Transaction {
    public Payment() {
        super(TransactionType.Payment);
    }
}
