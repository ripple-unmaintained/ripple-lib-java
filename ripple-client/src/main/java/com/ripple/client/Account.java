
package com.ripple.client;

import com.ripple.client.pubsub.Publisher;
import com.ripple.client.subscriptions.TrackedAccountRoot;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.core.coretypes.AccountID;
import com.ripple.crypto.ecdsa.IKeyPair;

/*
 *
 * We want this guy to be able to track accounts we have the secret for or not
 *
 * */
public class Account {
    private final Publisher<events> publisher = new Publisher<events>();
    public TransactionManager transactionManager() {
        return tm;
    }
    public Publisher<events> publisher() {
        return publisher;
    }
    // events enumeration
    public static interface events<T> extends Publisher.Callback<T> {}
    public static interface OnServerInfo extends events {}

    private TrackedAccountRoot accountRoot;
    private TransactionManager tm;
    public IKeyPair keyPair;

    public AccountID id() {
        return id;
    }

    public TrackedAccountRoot getAccountRoot() {
        return accountRoot;
    }

    public void setAccountRoot(TrackedAccountRoot accountRoot) {
        Account.this.accountRoot = accountRoot;
    }

    private AccountID id;

    public Account(AccountID id,
                   IKeyPair keyPair, TrackedAccountRoot root,
                   TransactionManager tm) {
        this.id = id;
        this.accountRoot = root;
        this.tm = tm;
        this.keyPair = keyPair;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
