package com.ripple.client;

import com.ripple.core.types.AccountID;
import com.ripple.crypto.ecdsa.KeyPair;

import java.util.HashMap;

public class KeyStore {
    private final HashMap<AccountID, KeyPair> keys = new HashMap<AccountID, KeyPair>();

    public void put(AccountID id, KeyPair keyPair) {
        keys.put(id, keyPair);
    }
    public KeyPair get(AccountID id) {
        return keys.get(id);
    }
}
