package com.ripple.core.enums;

import java.util.TreeMap;

public enum TransactionType {
    Invalid (-1),
    Payment (0),
    Claim (1), // open
    WalletAdd (2),
    AccountSet (3),
    PasswordFund (4), // open
    SetRegularKey(5),
    NickNameSet (6), // open
    OfferCreate (7),
    OfferCancel (8),
    Contract (9),
    RemoveContract(10),  // can we use the same msg as offer cancel
    TrustSet (20),
    EnableFeature(100),
    SetFee(101);

    public int asInteger() {
        return ord;
    }

    final int ord;
    TransactionType(int i) {
       ord = i;
    }

    static private TreeMap<Integer, TransactionType> byCode = new TreeMap<Integer, TransactionType>();
    static {
        for (Object a : TransactionType.values()) {
            TransactionType f = (TransactionType) a;
            byCode.put(f.ord, f);
        }
    }

    static public TransactionType fromNumber(Number i) {
        return byCode.get(i.intValue());
    }


}
