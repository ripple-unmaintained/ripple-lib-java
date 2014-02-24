package com.ripple.core.types.known.tx.txns;

import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;

public class OfferCancel extends Transaction {
    public OfferCancel() {
        super(TransactionType.OfferCancel);
    }
}
