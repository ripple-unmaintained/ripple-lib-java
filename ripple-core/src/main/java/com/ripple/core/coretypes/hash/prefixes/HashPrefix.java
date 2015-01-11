package com.ripple.core.coretypes.hash.prefixes;

import com.ripple.core.coretypes.uint.UInt32;

public enum HashPrefix implements Prefix {
    transactionID(0x54584E00L),
    // transaction plus metadata
    txNode(0x534E4400L),
    // account state
    leafNode(0x4D4C4E00L),
    // inner node in tree
    innerNode(0x4D494E00L),
    // ledger master data for signing
    ledgerMaster(0x4C575200L),
    // inner transaction to sign
    txSign(0x53545800L),
    // validation for signing
    validation(0x56414C00L),
    // proposal for signing
    proposal(0x50525000L);

    public UInt32 uInt32;
    public byte[] bytes;

    @Override
    public byte[] bytes() {
        return bytes;
    }

    HashPrefix(long i) {
        uInt32 = new UInt32(i);
        bytes = uInt32.toByteArray();
    }
}
