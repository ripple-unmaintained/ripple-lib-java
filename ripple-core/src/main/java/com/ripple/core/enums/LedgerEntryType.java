package com.ripple.core.enums;

import java.util.TreeMap;

public enum LedgerEntryType {
    Invalid (-1),
    AccountRoot ('a'),
    DirectoryNode('d'),
    GeneratorMap ('g'),
    RippleState ('r'),
    // Nickname ('n'), // deprecated
    Offer ('o'),
    Contract ('c'),
    LedgerHashes ('h'),
    EnabledAmendments('f'),
    FeeSettings ('s'),
    Ticket('T');

    final int ord;
    LedgerEntryType(int i) {
        ord = i;
    }

    static private TreeMap<Integer, LedgerEntryType> byCode = new TreeMap<Integer, LedgerEntryType>();
    static {
        for (Object a : LedgerEntryType.values()) {
            LedgerEntryType f = (LedgerEntryType) a;
            byCode.put(f.ord, f);
        }
    }

    public static LedgerEntryType fromNumber(Number i) {
        return byCode.get(i.intValue());
    }

    public Integer asInteger() {
        return ord;
    }
}
