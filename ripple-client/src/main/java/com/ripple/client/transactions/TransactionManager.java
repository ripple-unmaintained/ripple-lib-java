package com.ripple.client.transactions;

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
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

public class TransactionManager extends Publisher<TransactionManager.events> {
    // TODO, every n transactions request tx and

    Client client;
    AccountRoot accountRoot;
    AccountID accountID;
    IKeyPair keyPair;
    AccountTransactionsRequester txns;

    public long sequence = -1;
    Set<Long> seenValidatedSequences = new TreeSet<Long>();
    // TODO, maybe this is an instance configurable strategy parameter
    public static long LEDGERS_BETWEEN_ACCOUNT_TX = 15;
    public static long ACCOUNT_TX_TIMEOUT = 5;

    public void queue(ManagedTxn tx) {
        queue(tx, null);
    }

    public ArrayList<ManagedTxn> getQueue() {
        return queued;
    }

    public ArrayList<ManagedTxn> sequenceSortedQueue() {
        ArrayList<ManagedTxn> queued = new ArrayList<ManagedTxn>(getQueue());
        Collections.sort(queued, new Comparator<ManagedTxn>() {
            @Override
            public int compare(ManagedTxn lhs, ManagedTxn rhs) {
                int i = lhs.get(UInt32.Sequence).subtract(rhs.get(UInt32.Sequence)).intValue();
                return i > 0 ? 1 : i == 0 ? 0 : -1;
            }
        });
        return queued;
    }

    public static abstract class events<T> extends Publisher.Callback<T> {}
    // This event is emitted with the Sequence of the AccountRoot
    public static abstract class OnValidatedSequence extends events<UInt32> {}

    private ArrayList<ManagedTxn> queued = new ArrayList<ManagedTxn>();
    private long lastLedgerEmptyOrChecked = 0;
    private long lastLedgerPage = 0;

    public int awaiting() {
        return getQueue().size();
    }

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

                if (!canSubmit() || getQueue().isEmpty()) {
                    return;
                }
                ArrayList<ManagedTxn> sorted = sequenceSortedQueue();

                ManagedTxn first = sorted.get(0);
                Submission previous = first.lastSubmission();
                long ledgersClosed = serverInfo.ledger_index - previous.ledgerSequence;

                if (ledgersClosed > 5) {
                    resubmitWithSameSequence(first);
                }
            }
        });
    }

    AccountTransactionsRequester.OnPage onPage = new AccountTransactionsRequester.OnPage() {
        @Override
        public void onPage(AccountTransactionsRequester.Page page) {
            lastLedgerPage = client.serverInfo.ledger_index;

            if (page.hasNext()) {
                page.requestNext();
            } else {
                // We can only know if we got to here
                // TODO check what ledgerMax really means
                lastLedgerEmptyOrChecked = Math.max(lastLedgerEmptyOrChecked, page.ledgerMax());
                txns = null;
            }
            // TODO: TODO: check this logic
            // We are assuming moving `forward` from a ledger_index_min or resume marker

            for (TransactionResult tr : page.transactionResults()) {
                notifyTransactionResult(tr);
            }
        }
    };

    private void checkAccountTransactions(int ledger_index) {
        if (queued.size() == 0) {
            lastLedgerEmptyOrChecked = 0;
            return;
        }

        long ledgersPassed = ledger_index - lastLedgerEmptyOrChecked;

        if ((lastLedgerEmptyOrChecked == 0 || ledgersPassed >= LEDGERS_BETWEEN_ACCOUNT_TX)) {
            if (lastLedgerEmptyOrChecked == 0) {
                lastLedgerEmptyOrChecked = ledger_index;
                for (ManagedTxn txn : queued) {
                    for (Submission submission : txn.submissions) {
                        lastLedgerEmptyOrChecked = Math.min(lastLedgerEmptyOrChecked, submission.ledgerSequence);
                    }
                }
                return; // and wait for next ledger close
            }
            if (txns != null) {
                if ((ledger_index - lastLedgerPage) >= ACCOUNT_TX_TIMEOUT) {
                    txns.abort(); // no more OnPage
                    txns = null; // and wait for next ledger close
                }
                // else keep waiting ;)
            } else {
                lastLedgerPage = ledger_index;
                txns = new AccountTransactionsRequester(client, accountID, onPage, lastLedgerEmptyOrChecked - 5 /* for good measure */);

                // Very important VVVVV
                txns.setForward(true);
                txns.request();
            }
        }
    }

    public void queue(final ManagedTxn transaction, final UInt32 sequence) {
        getQueue().add(transaction);

        if (canSubmit()) {
            makeSubmitRequest(transaction, sequence);
        } else {
            // We wait for basically any old event n see if we are primed after each, angular styles
            client.on(Client.OnStateChange.class, new Client.OnStateChange() {
                @Override
                public void called(Client client) {
                    if (canSubmit()) {
                        client.removeListener(Client.OnStateChange.class, this);
                        makeSubmitRequest(transaction, sequence);
                    }
                }
            });
        }
    }

    private boolean canSubmit() {
        return client.serverInfo.primed() &&
               client.serverInfo.load_factor < 512 &&
               client.connected &&
               accountRoot.primed();
    }

    private Request makeSubmitRequest(final ManagedTxn transaction, UInt32 sequence) {
        Amount fee = client.serverInfo.transactionFee(transaction);
        // TODO: inside prepare, check if Fee and Sequence are the same and don't resign ;)
        transaction.prepare(keyPair, fee, sequence == null ? getSubmissionSequence() : sequence);
        final Request req = client.newRequest(Command.submit);
        req.json("tx_blob", transaction.tx_blob);

        req.once(Request.OnSuccess.class, new Request.OnSuccess() {
            @Override
            public void called(Response response) {
                handleSubmitSuccess(transaction, response);
            }
        });

        req.once(Request.OnError.class, new Request.OnError() {
            @Override
            public void called(Response response) {
                handleSubmitError(transaction, response);
            }
        });

        transaction.trackSubmitRequest(req, client.serverInfo);
        req.request();
        return req;
    }

    private UInt32 getSubmissionSequence() {
        long server = accountRoot.Sequence.longValue();
        if (sequence == -1 || server > sequence) {
            sequence = server;
        }
        return new UInt32(sequence++);
    }

    public void handleSubmitError(ManagedTxn transaction, Response res) {
//        resubmitFirstTransactionWithTakenSequence(transaction.sequence());
        if (transaction.finalizedOrHandlerForPriorSubmission(res)) {
            return;
        }
        switch (res.rpcerr) {
            case noNetwork:
                resubmitWithSameSequence(transaction);
                break;
            default:
                // TODO, what other cases should this retry?
                finalizeTxnAndRemoveFromQueue(transaction);
                transaction.publisher().emit(ManagedTxn.OnSubmitError.class, res);
                break;
        }
    }

    public void handleSubmitSuccess(final ManagedTxn transaction, final Response res) {
        if (transaction.finalizedOrHandlerForPriorSubmission(res)) {
            return;
        }
        TransactionEngineResult tr = res.engineResult();
        final UInt32 submitSequence = res.getSubmitSequence();
        switch (tr) {
            case tesSUCCESS:
                transaction.publisher().emit(ManagedTxn.OnSubmitSuccess.class, res);
                return;

            case tefPAST_SEQ:
                resubmitWithNewSequence(transaction);
                break;
            case terPRE_SEQ:
                on(OnValidatedSequence.class, new OnValidatedSequence() {
                    @Override
                    public void called(UInt32 sequence) {
                        if (transaction.finalizedOrHandlerForPriorSubmission(res)) {
                            removeListener(OnValidatedSequence.class, this);
                        }
                        if (sequence.equals(submitSequence)) {
                            // resubmit:
                            resubmit(transaction, submitSequence);
                        }
                    }
                });
                break;
            case telINSUF_FEE_P:
                resubmit(transaction, submitSequence);
                break;
            case tefALREADY:
                // We only get this if we are submitting with the correct transactionID
                // Do nothing, the transaction has already been submitted
                break;
            default:
                // In which cases do we patch ?
                switch (tr.resultClass()) {
                    case tecCLAIMED:
                        // Sequence was consumed, do nothing
                        finalizeTxnAndRemoveFromQueue(transaction);
                        transaction.publisher().emit(ManagedTxn.OnSubmitFailure.class, res);
                        break;
                    // What about this craziness /??
                    case temMALFORMED:
                    case tefFAILURE:
                    case telLOCAL_ERROR:
                    case terRETRY:
                        finalizeTxnAndRemoveFromQueue(transaction);
                        if (getQueue().isEmpty()) {
                            sequence--;
                        } else {
                            // Plug a Sequence gap and pre-emptively resubmit some
                            // rather than waiting for `OnValidatedSequence` which will take
                            // quite some ledgers
                            noopTransaction(submitSequence);
                            resubmitGreaterThan(submitSequence);
                        }
                        transaction.publisher().emit(ManagedTxn.OnSubmitFailure.class, res);
                        // look for
//                        queued.add(transaction);
                        // This is kind of nasty ..
                        break;

                }
                // TODO: Disposition not final ??!?!? ... ????
//                if (tr.resultClass() == TransactionEngineResult.Class.telLOCAL_ERROR) {
//                    queued.add(transaction);
//                }
                break;
        }
    }

    private void resubmitGreaterThan(UInt32 submitSequence) {
        for (ManagedTxn txn : getQueue()) {
            if (txn.sequence().compareTo(submitSequence) == 1) {
                resubmitWithSameSequence(txn);
            }
        }
    }

    private void noopTransaction(UInt32 sequence) {
        ManagedTxn plug = transaction(TransactionType.AccountSet);
        plug.setSequencePlug(true);
        queue(plug, sequence);
    }

    public void finalizeTxnAndRemoveFromQueue(ManagedTxn transaction) {
        transaction.setFinalized();
        getQueue().remove(transaction);
    }

    private void resubmitFirstTransactionWithTakenSequence(UInt32 sequence) {
        for (ManagedTxn txn : getQueue()) {
            if (txn.sequence().compareTo(sequence) == 0) {
                resubmitWithNewSequence(txn);
                break;
            }
        }
    }
    private void resubmitWithNewSequence(final ManagedTxn txn) {
        // We only EVER resubmit a txn with a new sequence if we actually seen it
        // this is the method that handles that,
        if (txn.isSequencePlug()) {
            // A sequence plug's sole purpose is to plug a sequence
            // The sequence has already been plugged.
            // So:
            return; // without further ado.
        }


        // ONLY ONLY ONLY if we've actually seen the Sequence
        if (seenValidatedSequences.contains(txn.sequence().longValue())) {
            resubmit(txn, getSubmissionSequence());
        } else {
            // TODO: Should we empty seenValidatedSequences when our queue is empty
            // TODO: Could this stall out for ages?
            // We should perhaps look for gaps in n, n+1, n+2 in the seenValidatedSequences
            // if we see some, it's an indication that we should check account_tx from ledgers
            // lowest in gap.
            // TODO: maybe seenValidatedSequences should be a Map<Sequence, Ledger>
            on(OnValidatedSequence.class, new OnValidatedSequence() {
                @Override
                public void called(UInt32 uInt32) {
                    if (seenValidatedSequences.contains(txn.sequence().longValue())) {
                        // TODO an auto remover ;)
                        removeListener(OnValidatedSequence.class, this);
                        resubmit(txn, getSubmissionSequence());
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
        if (previouslySubmitted == null) {
            throw new IllegalStateException("We don't want to pass null " +
                    "further else it would increment");
        }
        makeSubmitRequest(txn, previouslySubmitted);
    }

    public ManagedTxn payment() {
        return transaction(TransactionType.Payment);
    }

    private ManagedTxn transaction(TransactionType tt) {
        ManagedTxn tx = new ManagedTxn(tt);
        tx.put(AccountID.Account, accountID);
        return tx;
    }

    public void notifyTransactionResult(TransactionResult tm) {
        if (!tm.validated) {
            return;
        }
        UInt32 txnSequence = tm.transaction.get(UInt32.Sequence);
        seenValidatedSequences.add(txnSequence.longValue());

        ManagedTxn tx = submittedTransaction(tm.hash);
        if (tx != null) {
            tm.submittedTransaction = tx;
            finalizeTxnAndRemoveFromQueue(tx);
            tx.publisher().emit(ManagedTxn.OnTransactionValidated.class, tm);
        } else {
            resubmitFirstTransactionWithTakenSequence(txnSequence);
            emit(OnValidatedSequence.class, txnSequence.add(new UInt32(1)));
        }
    }

    private ManagedTxn submittedTransaction(Hash256 hash) {
        Iterator<ManagedTxn> iterator = getQueue().iterator();

        while (iterator.hasNext()) {
            ManagedTxn transaction = iterator.next();
            if (transaction.wasSubmittedWith(hash)) {
                iterator.remove();
                return transaction;
            }
        }
        return null;
    }
}
