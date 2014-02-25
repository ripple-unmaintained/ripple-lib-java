package com.ripple.core.types.known.sle.entries;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.fields.Field;
import com.ripple.core.types.known.sle.ThreadedLedgerEntry;

public class RippleState extends ThreadedLedgerEntry {
    public RippleState() {
        super(LedgerEntryType.RippleState);
    }

    public UInt32 highQualityIn() {return get(UInt32.HighQualityIn);}
    public UInt32 highQualityOut() {return get(UInt32.HighQualityOut);}
    public UInt32 lowQualityIn() {return get(UInt32.LowQualityIn);}
    public UInt32 lowQualityOut() {return get(UInt32.LowQualityOut);}
    public UInt64 lowNode() {return get(UInt64.LowNode);}
    public UInt64 highNode() {return get(UInt64.HighNode);}
    public Amount balance() {return get(Amount.Balance);}
    public Amount lowLimit() {return get(Amount.LowLimit);}
    public Amount highLimit() {return get(Amount.HighLimit);}
    public void highQualityIn(UInt32 val) {put(Field.HighQualityIn, val);}
    public void highQualityOut(UInt32 val) {put(Field.HighQualityOut, val);}
    public void lowQualityIn(UInt32 val) {put(Field.LowQualityIn, val);}
    public void lowQualityOut(UInt32 val) {put(Field.LowQualityOut, val);}
    public void lowNode(UInt64 val) {put(Field.LowNode, val);}
    public void highNode(UInt64 val) {put(Field.HighNode, val);}
    public void balance(Amount val) {put(Field.Balance, val);}
    public void lowLimit(Amount val) {put(Field.LowLimit, val);}
    public void highLimit(Amount val) {put(Field.HighLimit, val);}
}
