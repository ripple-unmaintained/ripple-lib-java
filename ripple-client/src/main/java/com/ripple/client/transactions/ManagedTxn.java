package com.ripple.client.transactions;

import com.ripple.client.responses.Response;
import com.ripple.client.pubsub.IPublisher;
import com.ripple.client.pubsub.Publisher;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.known.tx.Transaction;

public class ManagedTxn extends Transaction implements Sequenced, IPublisher<ManagedTxn.events> {
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

    // events enumeration
    public static abstract class events<T> extends Publisher.Callback<T> {}
    public static abstract class OnSubmitSuccess extends events<Response> {}
    public static abstract class OnSubmitError extends events<Response> {}
    public static abstract class OnTransactionValidated extends events<TransactionResult> {}
    public static abstract class OnSumbitRequestError extends events<Exception> {}

    public ManagedTxn(TransactionType type, long transactionId) {
        super(transactionId, type);
    }

}
