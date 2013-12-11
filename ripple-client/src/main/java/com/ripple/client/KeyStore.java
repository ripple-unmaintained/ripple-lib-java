package com.ripple.client;

import com.ripple.core.types.AccountID;
import com.ripple.crypto.ecdsa.IKeyPair;

import java.util.HashMap;

public class KeyStore {
    private final HashMap<AccountID, IKeyPair> keys = new HashMap<AccountID, IKeyPair>();

    public void put(AccountID id, IKeyPair keyPair) {
        keys.put(id, keyPair);
    }
    public IKeyPair get(AccountID id) {
        return keys.get(id);
    }
}
