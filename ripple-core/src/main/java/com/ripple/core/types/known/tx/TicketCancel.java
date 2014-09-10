package com.ripple.core.types.known.tx;

import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.enums.TransactionType;

public class TicketCancel extends Transaction {
    public TicketCancel() {
        super(TransactionType.TicketCancel);
    }
    public Hash256 ticketID() {
        return get(Hash256.TicketID);
    }
    public void ticketID(Hash256 id) {
        put(Hash256.TicketID, id);
    }
}
