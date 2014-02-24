package com.ripple.client.transactions;

import com.ripple.client.pubsub.CallbackContext;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.pubsub.Publisher;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.signed.SignedTransaction;

import java.util.ArrayList;
import java.util.TreeSet;

public class ManagedTxn extends SignedTransaction {
    public static abstract class events<T> extends Publisher.Callback<T> {}
    public static abstract class OnSubmitSuccess extends events<Response> {}
    public static abstract class OnSubmitFailure extends events<Response> {}
    public static abstract class OnSubmitError extends events<Response> {}
    public static abstract class OnTransactionValidated extends events<TransactionResult> {}

    public <T extends events> boolean removeListener(Class<T> key, Publisher.ICallback cb) {
        return publisher.removeListener(key, cb);
    }

    public <T extends events> int emit(Class<T> key, Object... args) {
        return publisher.emit(key, args);
    }

    public <T extends events> void once(Class<T> key, CallbackContext executor, T cb) {
        publisher.once(key, executor, cb);
    }

    public <T extends events> void once(Class<T> key, T cb) {
        publisher.once(key, cb);
    }

    public <T extends events> void on(Class<T> key, CallbackContext executor, T cb) {
        publisher.on(key, executor, cb);
    }

    public <T extends events> void on(Class<T> key, T cb) {
        publisher.on(key, cb);
    }

    public Publisher<events> publisher() {
        return publisher;
    }

    private boolean isSequencePlug;
    public boolean isSequencePlug() {
        return isSequencePlug;
    }
    public void setSequencePlug(boolean isNoop) {
        this.isSequencePlug = isNoop;
        setDescription("SequencePlug");
    }

    private String description;
    public String description() {
        if (description == null) {
            return txn.transactionType().toString();
        }
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


    public ManagedTxn(Transaction txn) {
        this.txn = txn;
    }
    private final Publisher<events> publisher = new Publisher<events>();
//    private final MyTransaction publisher = new MyTransaction();
    private boolean finalized = false;

    public boolean responseWasToLastSubmission(Response res) {
        Request req = lastSubmission().request;
        return res.request == req;
    }


    public boolean finalizedOrResponseIsToPriorSubmission(Response res) {
        return isFinalized() || !responseWasToLastSubmission(res);
    }

    ArrayList<Submission> submissions = new ArrayList<Submission>();

    public Submission lastSubmission() {
        if (submissions.isEmpty()) {
            return null;
        } else {
            return submissions.get(submissions.size() - 1);
        }
    }
    private TreeSet<Hash256> submittedIDs = new TreeSet<Hash256>();

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized() {
        finalized = true;
    }

    public void trackSubmitRequest(Request submitRequest, long ledger_index) {
        Submission submission = new Submission(submitRequest,
                                               sequence(),
                                               hash,
                                               ledger_index,
                                               txn.get(Amount.Fee),
                                               txn.get(UInt32.LastLedgerSequence));
        submissions.add(submission);
        trackSubmittedID();
    }

    public void trackSubmittedID() {
        submittedIDs.add(hash);
    }

    boolean wasSubmittedWith(Hash256 hash) {
        return submittedIDs.contains(hash);
    }

    public UInt32 sequence() {
        return txn.sequence();
    }
}
