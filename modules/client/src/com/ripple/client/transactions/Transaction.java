package com.ripple.client.transactions;

import com.ripple.client.Response;
import com.ripple.client.pubsub.IPublisher;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.subscriptions.ServerInfo;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.types.*;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt16;
import com.ripple.core.types.uint.UInt32;
import com.ripple.crypto.ecdsa.IKeyPair;

public class Transaction extends STObject implements Sequenced, IPublisher<Transaction.events> {
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

    public void remove(Class<? extends events> key, ICallback cb) {
        publisher.remove(key, cb);
    }

    // events enumeration
    public static abstract class events<T> extends Publisher.Callback<T> {}
    public static abstract class OnSubmitSuccess extends events<Response> {}
    public static abstract class OnSumbitRequestError extends events<Exception> {}
    public static abstract class OnSubmitError extends events<Response> {}
    public static abstract class OnTransactionValidated extends events<TransactionResult> {}

    public Hash256    hash;
    public byte[]     tx_blob;

    /* This is used to identify a given transaction beyond a sequence or hash that can change
     *  due to something as simple as load_base / load_factor changes.
      * */
    public long id;

    public UInt32 sequence() {
        return get(UInt32.Sequence);
    }

    public Transaction(TransactionType type, long transactionId) {
        setFormat(TxFormat.formats.get(type));
        put(UInt16.TransactionType, type.asInteger());
        id = transactionId;
    }

    public void prepare(IKeyPair keyPair, ServerInfo info, UInt32 Sequence) {
        remove(Field.TxnSignature);

        put(UInt32.Sequence, Sequence);
        put(Amount.Fee, info.transactionFee(get(UInt16.TransactionType)));
        put(VariableLength.SigningPubKey, keyPair.pubBytes());

        byte[] signingBlob = STObject.translate.toWireBytes(this);
        Hash256 signingHash = Hash256.signingHash(signingBlob);
        byte[] signature = keyPair.sign(signingHash.getBytes());

        // We need to re-serialize for submission
        put(VariableLength.TxnSignature, signature);
        put(VariableLength.SigningPubKey, keyPair.pubBytes());

        tx_blob = STObject.translate.toWireBytes(this);
        hash = Hash256.transactionID(tx_blob);
    }

}
