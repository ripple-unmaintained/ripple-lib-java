package com.ripple.client.transactions;

import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.pubsub.IPublisher;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.subscriptions.ServerInfo;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.known.tx.Transaction;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt32;

import java.util.ArrayList;
import java.util.TreeSet;

public class ManagedTxn extends Transaction implements IPublisher<ManagedTxn.events> {
    private final Publisher<events> publisher = new Publisher<events>();
    private boolean finalized = false;

    public static class Submission {
        Request submitRequest;
        Response submitResponse;
        UInt32 submitSequence;
        Hash256 submitHash;
        long submitLedgerSequence;

        public Submission(Request request, UInt32 sequence, Hash256 hash, long ledgerSequence) {
            submitRequest = request;
            submitSequence = sequence;
            submitHash = hash;
            submitLedgerSequence = ledgerSequence;
        }
    }
    ArrayList<Submission> submissions = new ArrayList<Submission>();

    private TreeSet<UInt32> submittedSequences = new TreeSet<UInt32>();
    private TreeSet<Hash256> submittedIDs = new TreeSet<Hash256>();

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized() {
        finalized = true;
    }

    public void trackSubmitRequest(Request submitRequest, ServerInfo serverInfo) {
        Submission submission = new Submission(submitRequest, sequence(), hash, serverInfo.ledger_index);
        submissions.add(submission);
        trackSubmittedID();
        trackSubmittedSequence();
    }

    public void trackSubmittedID() {
        submittedIDs.add(hash);
    }

    public void trackSubmittedSequence() {
        submittedSequences.add(get(UInt32.Sequence));
    }

    boolean wasSubmittedWith(Hash256 hash) {
        return submittedIDs.contains(hash);
    }

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

    public UInt32 sequence() {
        return get(UInt32.Sequence);
    }

    // events enumeration
    public static abstract class events<T> extends Publisher.Callback<T> {
    }

    public static abstract class OnSubmitSuccess extends events<Response> {
    }

    public static abstract class OnSubmitError extends events<Response> {
    }

    public static abstract class OnTransactionValidated extends events<TransactionResult> {
    }

    public static abstract class OnSumbitRequestError extends events<Exception> {
    }

    public ManagedTxn(TransactionType type, long transactionId) {
        super(transactionId, type);
    }

}
