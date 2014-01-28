package com.ripple.client.transactions;

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.pubsub.CallbackContext;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.subscriptions.ServerInfo;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.crypto.ecdsa.IKeyPair;

import java.util.*;

/**

TransactionManager
------------------

The `TransactionManager` is in charge of submitting transactions to the network
in such a way that it's resilient to poor network conditions (busy, dropouts).

There is, in fact, a non 100% success rate for every transaction submission.
This can be due to conditions at the tranport layer, the node submitted to could
be busy, completely dropping a request, or reject due to an insufficient fee,
for the current load.

Therefore client libraries will present an API that in the background
automatically handles resubmitting transactions (where it makes sense to do so)

There are 3 phases to submitting a transaction and verifying it has succeeded.

  1. Submission to a ripple node

    Important to note:

      Transaction has entered the transport layer, and could be resubmitted  by
      3rd parties (helpfully or maliciously)

  2. Response to (stage 1) submission from a ripple node

    At this point the transaction has succeeded in being applied to the local
    node's ledger. It is then distributed amongst the network. It may take more
    than one ledger close before it's finalized.

  3. Transaction finalized notification from ripple node

    The local node will send out to subscribers of a given account, any
    transactions that affect it.

    Recipients will receive notification of transactions as they are validated
    by the network.

Locally, from the client perspective it's only possible to really know that the
first step has failed.

It's possible (and has been observed), that at times there will be no response
or notifications from the node the client is connected to, for a transaction
that will ultimately be successful and distributed to the network.

It's very important to realize that `no response` isn't necessarily an
indication of failure.

Each `Transaction` submitted to the network, in binary form, has the following:

  - A transaction signature `TxnSignature` field
    - what is signed is a hash of the transaction contents in binary form
    - this is ecdsa, which has a random component
      - Signing the EXACT same content multiple times will result in multiple
        unique signatures.

  - a `hash` (sometimes referred to as a transactionID)
    - dubbed a `hash`, due to it being a hash of the signed contents
      of the transaction

      - IMPORTANT: the contents include the RANDOM TxnSignature

      - The hash is used to map transaction notification messages
        to submitted transactions in the pending queue.

    - `Fee`

      This is the amount of drops (1e-6 XRP) destroyed.

      There is a minimum Fee, which scales with the current load on the node
      submitted to.

So:
  - Validated transactions are identified by hash of contents (including sig)
  - May not get response of successful submission/validation
  - May need to change a `Fee` and resubmit for a transaction to be applied

Therefore, as a transaction gets submitted multiple times, if the transaction
binary representation content changes, due to a Fee change, it becomes necessary
to look out for ALL submitted transactionIDs to map incoming transaction
notifications to a given Transaction.

To understand why the `Sequence` field must also be considered.

  - Sequence

    In a new Account, there is an integer field `Sequence`, which is initialized
    with the value `1`.

    Each transaction submitted by the account, must declare a `Sequence` too.
    The transaction sequence must exactly match that stored in the account node.

    Any transaction that is successful increments the Sequence in the account
    node.

    This is to stop replay attacks. Each transaction can be applied only once.

It's possible when multiple clients are submitting transactions for the same
account that there will be contention for Sequence numbers. One client will need
to increment the sequence on a transaction for it to succeed.

How can this be detected? As transaction notifications come in, the hash of each
transaction is compared against that of all the pending transactions. If a hash
matches, the transaction is cleared, otherwise if the Sequence matches any of
the pending transactions, it's assumed that the Sequence has been consumed by
another account.

  >>> the hash of each transaction is compared against that of all the pending
  >>> transactions

Actually it must be compared against **all hashes of any transactions that
entered the transport layer**, not just the most recent one submitted, otherwise
there runs the risk of submitting the same transaction more than once. (The
Sequence will be incremented, as it's assumed that a transaction submitted by
another client consumed the Sequence)

Robust transaction validation
-----------------------------

For various reasons the live transaction validation notification messages could
be missed.

  - Transport layer issues
    - messages missed

  - Simply stalled nodes

Therefore, periodically, when there are pending transactions, the transaction
manger will request a list of transactions from the server, from transaction $n
and forward. $n is determined by looking at the ledger index of the oldest
submission in the pending queue of transactions.

The same logic that handles clearing transactions from the live stream is
applied to each transaction in the transaction list.

The `account_tx` command is used for this purpose.

 */
public class TransactionManager extends Publisher<TransactionManager.events> {
    public static abstract class events<T> extends Publisher.Callback<T> {}
    // This event is emitted with the Sequence of the AccountRoot
    public static abstract class OnValidatedSequence extends events<UInt32> {}

    Client client;
    AccountRoot accountRoot;
    AccountID accountID;
    IKeyPair keyPair;
    AccountTransactionsRequester txnRequester;

    private ArrayList<ManagedTxn> pending = new ArrayList<ManagedTxn>();

    public TransactionManager(Client client, final AccountRoot accountRoot, AccountID accountID, IKeyPair keyPair) {
        this.client = client;
        this.accountRoot = accountRoot;
        this.accountID = accountID;
        this.keyPair = keyPair;

        // We'd be subscribed yeah ;)
        this.client.on(Client.OnLedgerClosed.class, new Client.OnLedgerClosed() {
            @Override
            public void called(ServerInfo serverInfo) {
                checkAccountTransactions(serverInfo.ledger_index);

                if (!canSubmit() || getPending().isEmpty()) {
                    return;
                }
                ArrayList<ManagedTxn> sorted = pendingSequenceSorted();

                ManagedTxn first = sorted.get(0);
                Submission previous = first.lastSubmission();

                if (previous != null) {
                    long ledgersClosed = serverInfo.ledger_index - previous.ledgerSequence;
                    if (ledgersClosed > 5) {
                        resubmitWithSameSequence(first);
                    }
                }
            }
        });
    }


    Set<Long> seenValidatedSequences = new TreeSet<Long>();
    public long sequence = -1;

    private UInt32 locallyPreemptedSubmissionSequence() {
        long server = accountRoot.Sequence.longValue();
        if (sequence == -1 || server > sequence) {
            sequence = server;
        }
        return new UInt32(sequence++);
    }
    private boolean txnNotFinalizedAndSeenValidatedSequence(ManagedTxn txn) {
        return !txn.isFinalized() &&
               seenValidatedSequences.contains(txn.sequence().longValue());
    }


    public void queue(ManagedTxn tx) {
        queue(tx, locallyPreemptedSubmissionSequence());
    }

    // TODO: data structure that keeps txns in sequence sorted order
    public ArrayList<ManagedTxn> getPending() {
        return pending;
    }

    public ArrayList<ManagedTxn> pendingSequenceSorted() {
        ArrayList<ManagedTxn> queued = new ArrayList<ManagedTxn>(getPending());
        Collections.sort(queued, new Comparator<ManagedTxn>() {
            @Override
            public int compare(ManagedTxn lhs, ManagedTxn rhs) {
                int i = lhs.get(UInt32.Sequence).subtract(rhs.get(UInt32.Sequence)).intValue();
                return i > 0 ? 1 : i == 0 ? 0 : -1;
            }
        });
        return queued;
    }

    public int txnsPending() {
        return getPending().size();
    }

    // TODO, maybe this is an instance configurable strategy parameter
    public static long LEDGERS_BETWEEN_ACCOUNT_TX = 15;
    public static long ACCOUNT_TX_TIMEOUT = 5;
    private long lastTxnRequesterUpdate = 0;
    private long lastLedgerCheckedAccountTxns = 0;
    AccountTransactionsRequester.OnPage onTxnsPage = new AccountTransactionsRequester.OnPage() {
        @Override
        public void onPage(AccountTransactionsRequester.Page page) {
            lastTxnRequesterUpdate = client.serverInfo.ledger_index;

            if (page.hasNext()) {
                page.requestNext();
            } else {
                // We can only know if we got to here
                // TODO check what ledgerMax really means
                lastLedgerCheckedAccountTxns = Math.max(lastLedgerCheckedAccountTxns, page.ledgerMax());
                txnRequester = null;
            }
            // TODO: TODO: check this logic
            // We are assuming moving `forward` from a ledger_index_min or resume marker

            for (TransactionResult tr : page.transactionResults()) {
                notifyTransactionResult(tr);
            }
        }
    };

    private void checkAccountTransactions(int currentLedgerIndex) {
        if (pending.size() == 0) {
            lastLedgerCheckedAccountTxns = 0;
            return;
        }

        long ledgersPassed = currentLedgerIndex - lastLedgerCheckedAccountTxns;

        if ((lastLedgerCheckedAccountTxns == 0 || ledgersPassed >= LEDGERS_BETWEEN_ACCOUNT_TX)) {
            if (lastLedgerCheckedAccountTxns == 0) {
                lastLedgerCheckedAccountTxns = currentLedgerIndex;
                for (ManagedTxn txn : pending) {
                    for (Submission submission : txn.submissions) {
                        lastLedgerCheckedAccountTxns = Math.min(lastLedgerCheckedAccountTxns, submission.ledgerSequence);
                    }
                }
                return; // and wait for next ledger close
            }
            if (txnRequester != null) {
                if ((currentLedgerIndex - lastTxnRequesterUpdate) >= ACCOUNT_TX_TIMEOUT) {
                    txnRequester.abort(); // no more OnPage
                    txnRequester = null; // and wait for next ledger close
                }
                // else keep waiting ;)
            } else {
                lastTxnRequesterUpdate = currentLedgerIndex;
                txnRequester = new AccountTransactionsRequester(client,
                                                                accountID,
                        onTxnsPage,
                                                                /* for good measure */
                                                                lastLedgerCheckedAccountTxns - 5);

                // Very important VVVVV
                txnRequester.setForward(true);
                txnRequester.request();
            }
        }
    }

    public void queue(final ManagedTxn txn, final UInt32 sequence) {
        getPending().add(txn);
        makeSubmitRequest(txn, sequence);
    }

    private boolean canSubmit() {
        return client.connected  &&
               client.serverInfo.primed() &&
               client.serverInfo.load_factor < 768 &&
               accountRoot.primed();
    }

    private void makeSubmitRequest(final ManagedTxn txn, final UInt32 sequence) {
        if (canSubmit()) {
            doSubmitRequest(txn, sequence);
        }
        else {
            // If we have submitted again, before this gets to execute
            // we should just bail out early, and not submit again.
            final int n = txn.submissions.size();
            client.on(Client.OnStateChange.class, new CallbackContext() {
                @Override
                public boolean shouldExecute() {
                    return canSubmit() && !shouldRemove();
                }

                @Override
                public boolean shouldRemove() {
                    // The next state change should cause this to remove
                    return txn.isFinalized() || n != txn.submissions.size();
                }
            }, new Client.OnStateChange() {
                @Override
                public void called(Client client) {
                    doSubmitRequest(txn, sequence);
                }
            });
        }
    }

    private Request doSubmitRequest(final ManagedTxn txn, UInt32 sequence) {
        // Comput the fee for the current load_factor
        Amount fee = client.serverInfo.transactionFee(txn);
        // Inside prepare we check if Fee and Sequence are the same, and if so
        // we don't recreated tx_blob, or resign ;)
        txn.prepare(keyPair, fee, sequence);

        final Request req = client.newRequest(Command.submit);
        // tx_blob is a hex string, right o' the bat
        req.json("tx_blob", txn.tx_blob);

        req.once(Request.OnSuccess.class, new Request.OnSuccess() {
            @Override
            public void called(Response response) {
                handleSubmitSuccess(txn, response);
            }
        });

        req.once(Request.OnError.class, new Request.OnError() {
            @Override
            public void called(Response response) {
                handleSubmitError(txn, response);
            }
        });

        // Keep track of the submission, including the hash submitted
        // to the network, and the ledger_index at that point in time.
        txn.trackSubmitRequest(req, client.serverInfo);
        req.request();
        return req;
    }

    public void handleSubmitError(ManagedTxn txn, Response res) {
//        resubmitFirstTransactionWithTakenSequence(transaction.sequence());
        if (txn.finalizedOrResponseIsToPriorSubmission(res)) {
            return;
        }
        switch (res.rpcerr) {
            case noNetwork:
                resubmitWithSameSequence(txn);
                break;
            default:
                // TODO, what other cases should this retry?
                finalizeTxnAndRemoveFromQueue(txn);
                txn.publisher().emit(ManagedTxn.OnSubmitError.class, res);
                break;
        }
    }

    public void handleSubmitSuccess(final ManagedTxn txn, final Response res) {
        if (txn.finalizedOrResponseIsToPriorSubmission(res)) {
            return;
        }
        TransactionEngineResult ter = res.engineResult();
        final UInt32 submitSequence = res.getSubmitSequence();
        switch (ter) {
            case tesSUCCESS:
                txn.publisher().emit(ManagedTxn.OnSubmitSuccess.class, res);
                return;

            case tefPAST_SEQ:
                resubmitWithNewSequence(txn);
                break;
            case terPRE_SEQ:
                on(OnValidatedSequence.class, new OnValidatedSequence() {
                    @Override
                    public void called(UInt32 sequence) {
                        if (txn.finalizedOrResponseIsToPriorSubmission(res)) {
                            removeListener(OnValidatedSequence.class, this);
                        }
                        if (sequence.equals(submitSequence)) {
                            // resubmit:
                            resubmit(txn, submitSequence);
                        }
                    }
                });
                break;
            case telINSUF_FEE_P:
                resubmit(txn, submitSequence);
                break;
            case tefALREADY:
                // We only get this if we are submitting with the correct transactionID
                // Do nothing, the transaction has already been submitted
                break;
            default:
                // In which cases do we patch ?
                switch (ter.resultClass()) {
                    case tecCLAIMED:
                        // Sequence was consumed, do nothing
                        finalizeTxnAndRemoveFromQueue(txn);
                        txn.publisher().emit(ManagedTxn.OnSubmitFailure.class, res);
                        break;
                    // What about this craziness /??
                    case temMALFORMED:
                    case tefFAILURE:
                    case telLOCAL_ERROR:
                    case terRETRY:
                        finalizeTxnAndRemoveFromQueue(txn);
                        if (getPending().isEmpty()) {
                            sequence--;
                        } else {
                            // Plug a Sequence gap and pre-emptively resubmit some
                            // rather than waiting for `OnValidatedSequence` which will take
                            // quite some ledgers
                            queueSequencePlugTxn(submitSequence);
                            resubmitGreaterThan(submitSequence);
                        }
                        txn.publisher().emit(ManagedTxn.OnSubmitFailure.class, res);
                        // look for
//                        pending.add(transaction);
                        // This is kind of nasty ..
                        break;

                }
                // TODO: Disposition not final ??!?!? ... ????
//                if (tr.resultClass() == TransactionEngineResult.Class.telLOCAL_ERROR) {
//                    pending.add(transaction);
//                }
                break;
        }
    }

    private void resubmitGreaterThan(UInt32 submitSequence) {
        for (ManagedTxn txn : getPending()) {
            if (txn.sequence().compareTo(submitSequence) == 1) {
                resubmitWithSameSequence(txn);
            }
        }
    }

    private void queueSequencePlugTxn(UInt32 sequence) {
        ManagedTxn plug = transaction(TransactionType.AccountSet);
        plug.setSequencePlug(true);
        queue(plug, sequence);
    }

    public void finalizeTxnAndRemoveFromQueue(ManagedTxn transaction) {
        transaction.setFinalized();
        getPending().remove(transaction);
    }

    private void resubmitFirstTransactionWithTakenSequence(UInt32 sequence) {
        for (ManagedTxn txn : getPending()) {
            if (txn.sequence().compareTo(sequence) == 0) {
                resubmitWithNewSequence(txn);
                break;
            }
        }
    }
    // We only EVER resubmit a txn with a new sequence if we actually seen it
    // this is the method that handles that,
    private void resubmitWithNewSequence(final ManagedTxn txn) {
        if (txn.isSequencePlug()) {
            // A sequence plug's sole purpose is to plug a sequence
            // The sequence has already been plugged.
            // So:
            return; // without further ado.
        }

        // ONLY ONLY ONLY if we've actually seen the Sequence
        if (txnNotFinalizedAndSeenValidatedSequence(txn)) {
            resubmit(txn, locallyPreemptedSubmissionSequence());
        } else {
            // requesting account_tx now and then (as we do) should take care of this
            on(OnValidatedSequence.class,
                new CallbackContext() {
                    @Override
                    public boolean shouldExecute() {
                        return !txn.isFinalized();
                    }

                    @Override
                    public boolean shouldRemove() {
                        return txn.isFinalized();
                    }
                },
                new OnValidatedSequence() {
                    @Override
                    public void called(UInt32 uInt32) {
                        // Again, just to be safe.
                        if (txnNotFinalizedAndSeenValidatedSequence(txn)) {
                            resubmit(txn, locallyPreemptedSubmissionSequence());
                        }
                    }
            });
        }
    }

    private void resubmit(ManagedTxn txn, UInt32 sequence) {
        makeSubmitRequest(txn, sequence);
    }

    private void resubmitWithSameSequence(ManagedTxn txn) {
        UInt32 previouslySubmitted = txn.sequence();
        makeSubmitRequest(txn, previouslySubmitted);
    }

    public ManagedTxn payment() {
        return transaction(TransactionType.Payment);
    }

    private ManagedTxn transaction(TransactionType tt) {
        ManagedTxn txn = new ManagedTxn(tt);
        txn.put(AccountID.Account, accountID);
        return txn;
    }

    public void notifyTransactionResult(TransactionResult tr) {
        if (!tr.validated) {
            return;
        }
        UInt32 txnSequence = tr.transaction.get(UInt32.Sequence);
        seenValidatedSequences.add(txnSequence.longValue());

        ManagedTxn txn = submittedTransactionForHash(tr.hash);
        if (txn != null) {
            // TODO: icky
            // A result doesn't have a ManagedTxn
            // a ManagedTxn has a result
            tr.submittedTransaction = txn;
            finalizeTxnAndRemoveFromQueue(txn);
            txn.publisher().emit(ManagedTxn.OnTransactionValidated.class, tr);
        } else {
            // preempt the terPRE_SEQ
            resubmitFirstTransactionWithTakenSequence(txnSequence);
            emit(OnValidatedSequence.class, txnSequence.add(new UInt32(1)));
        }
    }

    private ManagedTxn submittedTransactionForHash(Hash256 hash) {
        for (ManagedTxn transaction : getPending()) {
            if (transaction.wasSubmittedWith(hash)) {
                return transaction;
            }
        }
        return null;
    }
}
