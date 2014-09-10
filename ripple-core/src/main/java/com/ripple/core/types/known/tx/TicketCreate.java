package com.ripple.core.types.known.tx;

import com.ripple.core.enums.TransactionType;

public class TicketCreate extends Transaction {
    public TicketCreate() {
        super(TransactionType.TicketCreate);
    }
}
