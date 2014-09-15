package com.ripple.client.subscriptions.ledger;

import com.ripple.client.Client;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.shamap.TransactionTree;

import java.text.MessageFormat;

import static com.ripple.client.subscriptions.ledger.LedgerSubscriber.log;

public class PendingLedger {
    static private final String logMessage = "({0}/{4}) {1}/{2} tx {3}";
    private Object[] logParameters() {
        return new Object[]{
                ledger_index,
                clearedTransactions,
                expectedTxns,
                transactions.hash(),
                status};
    }

    public void setStatus(Status status) {
        this.status = status;
        logStateChange();
    }

    public static enum Status {
        pending,
        checkingHeader,
        fillingIn,
        cleared
    }

    public Status status;
    TransactionTree transactions;
    // set to -1 when we don't know how many to expect
    // this is just useful for debugging purposes
    int expectedTxns = -1;
    int clearedTransactions = 0;

    long ledger_index;
    private Client client;

    public PendingLedger(long ledger_index, Client clientInstance) {
        this.ledger_index = ledger_index;

        transactions = new TransactionTree();
        client = clientInstance;
        status = Status.pending;
    }

    public void notifyTransaction(TransactionResult tr) {
        if (!transactions.hasLeaf(tr.hash)) {
            clearedTransactions++;
            transactions.addTransactionResult(tr);
            client.onTransactionResult(tr);
            logStateChange();
        }
    }

    private void logStateChange() {
        log(logMessage, logParameters());
    }

    public String transactionHash() {
        return transactions.hash().toHex();
    }

    boolean transactionHashEquals(String transaction_hash) {
        return transaction_hash.equals(transactionHash());
    }

    @Override
    public String toString() {
        Object[] arguments = logParameters();
        return MessageFormat.format("PendingLedger: " + logMessage, arguments);
    }
}
