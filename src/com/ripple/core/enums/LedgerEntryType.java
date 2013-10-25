package com.ripple.core.enums;

import java.util.HashMap;
import java.util.TreeMap;

public enum LedgerEntryType {
    Invalid (-1),
    AccountRoot ('a'),
    DirectoryNode('d'),
    GeneratorMap ('g'),
    RippleState ('r'),
    Nickname ('n'),
    Offer ('o'),
    Contract ('c'),
    LedgerHashes ('h'),
    EnabledFeatures('f'),
    FeeSettings ('s');

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

    public static LedgerEntryType fromNumber(int i) {
        return byCode.get(i);
    }

    public Integer asInteger() {
        return ord;
    }
}
