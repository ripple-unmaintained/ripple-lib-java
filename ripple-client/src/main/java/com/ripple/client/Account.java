
package com.ripple.client;

import com.ripple.client.pubsub.IPublisher;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.wallet.Wallet;
import com.ripple.core.types.AccountID;

/*
 *
 * We want this guy to be able to track accounts we have the secret for or not
 *
 * */
public class Account extends AccountID implements IPublisher<Account.events> {
    private final Publisher<events> publisher = new Publisher<events>();

    public <T extends events> void on(Class<T> key, T cb) {
        publisher.on(key, cb);
    }

    public <T extends events> void once(final Class<T> key, final T cb) {
        publisher.once(key, cb);
    }

    public <T extends events> int emit(Class<T> key, Object... args) {
        return publisher.emit(key, args);
    }

    public void removeListener(Class<? extends events> key, ICallback cb) {
        publisher.removeListener(key, cb);
    }

    public TransactionManager transactionManager() {
        return tm;
    }

    // events enumeration
    public static abstract class events<T> extends Publisher.Callback<T> {
    }

    public static abstract class OnServerInfo extends events {
    }

    private AccountRoot accountRoot;
    private Wallet wallet;
    private TransactionManager tm;

    public AccountID id() {
        return id;
    }

    public AccountRoot getAccountRoot() {
        return accountRoot;
    }

    public void setAccountRoot(AccountRoot accountRoot) {
        this.accountRoot = accountRoot;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    private AccountID id;

    private Account() {
        super();
    }

    public Account(AccountID id,
            AccountRoot root,
            Wallet wallet,
            TransactionManager tm) {
        cloneFields(id);

        this.accountRoot = root;
        this.wallet = wallet;
        this.tm = tm;
    }

    private void cloneFields(AccountID id) {
        this.addressBytes = id.bytes();
        this.address = id.address;
        this.masterSeed = id.masterSeed;
        this.keyPair = id.getKeyPair();
    }
}
