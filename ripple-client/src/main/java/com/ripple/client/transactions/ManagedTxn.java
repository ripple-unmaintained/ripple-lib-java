package com.ripple.client.transactions;

import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.subscriptions.ServerInfo;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.crypto.ecdsa.IKeyPair;

import java.util.ArrayList;
import java.util.TreeSet;

// TODO: Should OfferCreate/Payment & co extend Transaction or ManagedTxn?
// TODO: Should ManagedTxn instead HAVE instead of be?
public class ManagedTxn extends Transaction {
    // events enumeration
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
            return transactionType().toString();
        }
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public static abstract class events<T> extends Publisher.Callback<T> {}
    public static abstract class OnSubmitSuccess extends events<Response> {}
    public static abstract class OnSubmitFailure extends events<Response> {}
    public static abstract class OnSubmitError extends events<Response> {}
    public static abstract class OnTransactionValidated extends events<TransactionResult> {}
//    public static abstract class OnSumbitRequestError extends events<Exception> {}

    public ManagedTxn(TransactionType type) {
        super(type);
    }
    private final Publisher<events> publisher = new Publisher<events>();
//    private final MyTransaction publisher = new MyTransaction();
    private boolean finalized = false;

    public boolean responseWasToLastSubmission(Response res) {
        Request req = lastSubmission().request;
        return res.request == req;
    }

    @Override
    public void prepare(IKeyPair keyPair, Amount fee, UInt32 Sequence) {
        Amount previousFee = get(Amount.Fee);
        UInt32 previousSequence = get(UInt32.Sequence);

        if ( (previousFee == null) ||
             (previousSequence == null) ||
             !previousFee.equals(fee) ||
             !previousSequence.equals(Sequence) ) {
            super.prepare(keyPair, fee, Sequence);
        }
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

    public void trackSubmitRequest(Request submitRequest, ServerInfo serverInfo) {
        Submission submission = new Submission(submitRequest, sequence(), hash, serverInfo.ledger_index, get(Amount.Fee));
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
        return get(UInt32.Sequence);
    }
}
