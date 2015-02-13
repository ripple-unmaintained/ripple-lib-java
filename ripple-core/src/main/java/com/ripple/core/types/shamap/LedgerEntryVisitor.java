package com.ripple.core.types.shamap;

import com.ripple.core.types.known.sle.LedgerEntry;

public interface LedgerEntryVisitor {
    public void onEntry(LedgerEntry entry);
}
