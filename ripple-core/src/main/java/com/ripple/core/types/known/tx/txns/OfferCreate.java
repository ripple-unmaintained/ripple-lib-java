package com.ripple.core.types.known.tx.txns;

import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;

public class OfferCreate extends Transaction {
    public OfferCreate() {
        super(TransactionType.OfferCreate);
    }
}
