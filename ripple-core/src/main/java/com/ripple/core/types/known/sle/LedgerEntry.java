package com.ripple.core.types.known.sle;

import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.enums.LedgerEntryType;

abstract public class LedgerEntry extends STObject {
    public LedgerEntryType ledgerEntryType() {
        return ledgerEntryType(this);
    }
}
