package com.ripple.core.coretypes.hash.prefixes;

import com.ripple.core.coretypes.uint.UInt16;

public enum LedgerSpace implements Prefix {
    account('a'),
    dirNode('d'),
    generator('g'),

    ripple('r'),
    offer('o'),  // Entry for an offer.
    ownerDir('O'),  // Directory of things owned by an account.
    bookDir('B'),  // Directory of order books.
    contract('c'),
    skipList('s'),
    amendment('f'),
    fee('e'),
    ticket('T'),

    // no longer used
    nickname('n'),;

    UInt16 uInt16;
    public byte[] bytes;

    @Override
    public byte[] bytes() {
        return bytes;
    }

    LedgerSpace(char c) {
        uInt16 = new UInt16((int) c);
        bytes = uInt16.toByteArray();
    }
}
