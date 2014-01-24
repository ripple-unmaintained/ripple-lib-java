
package com.ripple.client;

import com.ripple.client.pubsub.Publisher;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.wallet.Wallet;
import com.ripple.core.coretypes.AccountID;

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
    public static abstract class events<T> extends Publisher.Callback<T> {}
    public static abstract class OnServerInfo extends events {}

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
        Account.this.accountRoot = accountRoot;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        Account.this.wallet = wallet;
    }

    private AccountID id;

    public Account(AccountID id,
            AccountRoot root,
            Wallet wallet,
            TransactionManager tm) {
        this.id = id;

        this.accountRoot = root;
        this.wallet = wallet;
        this.tm = tm;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
