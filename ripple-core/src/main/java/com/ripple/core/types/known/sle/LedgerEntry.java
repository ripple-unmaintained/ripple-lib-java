package com.ripple.core.types.known.sle;

import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.fields.Field;
import com.ripple.core.formats.SLEFormat;

public class LedgerEntry extends STObject {
    public LedgerEntry(LedgerEntryType type) {
        setFormat(SLEFormat.formats.get(type));
        put(UInt16.LedgerEntryType, type.asInteger());
    }

    public LedgerEntryType ledgerEntryType() {return ledgerEntryType(this);}
    public Hash256 index() { return get(Hash256.index); }
    public UInt32 flags() {return get(UInt32.Flags);}
    public Hash256 ledgerIndex() {return get(Hash256.LedgerIndex);}

    public void ledgerEntryType(UInt16 val) {put(Field.LedgerEntryType, val);}
    public void ledgerEntryType(LedgerEntryType val) {put(Field.LedgerEntryType, val.asInteger());}
    public void flags(UInt32 val) {put(Field.Flags, val);}
    public void ledgerIndex(Hash256 val) {put(Field.LedgerIndex, val);}
}
