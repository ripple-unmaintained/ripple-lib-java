package com.ripple.client.transactions;

import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.enums.Command;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.Amount;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt32;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.encodings.common.B16;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class TransactionManager {
    Client client;
    AccountRoot accountRoot;
    AccountID accountID;
    IKeyPair keyPair;
    public long sequence = -1;
    public long transactionID;

    ArrayList<ManagedTxn> submitted = new ArrayList<ManagedTxn>();
    ArrayList<ManagedTxn> queued = new ArrayList<ManagedTxn>();

    public int awaiting() {
        return queued.size() + submitted.size();
    }

    public TransactionManager(Client client, AccountRoot accountRoot, AccountID accountID, IKeyPair keyPair) {
        this.client = client;
        this.accountRoot = accountRoot;
        this.accountID = accountID;
        this.keyPair = keyPair;
    }

    public void queue(final ManagedTxn transaction) {
        queued.add(transaction);

        if (canSubmit()) {
            makeSubmitRequest(transaction);
        } else {
            // We wait for basically any old event n see if we are primed after each, angular styles
            client.on(Client.OnMessage.class, new Client.OnMessage() {
                @Override
                public void called(JSONObject jsonObject) {
                    if (canSubmit()) {
                        client.removeListener(Client.OnMessage.class, this);
                        makeSubmitRequest(transaction);
                    }
                }
            });
        }
    }

    private boolean canSubmit() {
        return client.serverInfo.primed() && accountRoot.primed();
    }

    private Request makeSubmitRequest(final ManagedTxn transaction) {
        Amount fee = client.serverInfo.transactionFee(transaction);
        transaction.prepare(keyPair, fee, getSubmissionSequence());

        final Request req = client.newRequest(Command.submit);
        req.json("tx_blob", B16.toString(transaction.tx_blob));
        if (!transaction.get(Amount.Amount).isNative()) {
            req.json("build_path", true);
        }

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

        req.request();
        return req;
    }

    /*
    * The $10,000 question is when does sequence get decremented?
    * */
    private UInt32 getSubmissionSequence() {
        long server = accountRoot.Sequence.longValue();
        if (sequence == -1 || server > sequence ) {
            sequence = server;
        }
        return new UInt32(sequence++);
    }

    public void handleSubmitError(ManagedTxn transaction, Response response) {
        invalidateSequence(transaction.sequence());
    }

    public void handleSubmitSuccess(ManagedTxn transaction, Response res) {
        queued.remove(transaction); // TODO: re-queue

        TransactionEngineResult tr = res.engineResult();
        switch (tr.resultClass()) {
            case tesSUCCESS:
                submitted.add(transaction);
                transaction.emit(ManagedTxn.OnSubmitSuccess.class, res);
                return;

            case telLOCAL_ERROR:
                // TODO, this could actually resolve ...
                // Resubmitting exactly the same transaction probably wont hurt
                // For the moment we are just going to make sure to watch for it
                // closing
                submitted.add(transaction);
            case temMALFORMED:
            case tefFAILURE:
            case terRETRY:
            case tecCLAIMED:
                transaction.emit(ManagedTxn.OnSubmitError.class, res);
                break;
        }
    }

    private void invalidateSequence(UInt32 sequence) {

    }

    public ManagedTxn payment() {
        return transaction(TransactionType.Payment);
    }

    private ManagedTxn transaction(TransactionType tt) {
        ManagedTxn tx = new ManagedTxn(tt, transactionID++);
        tx.put(AccountID.Account, accountID);
        return tx;
    }

    public void onTransactionResultMessage(TransactionResult tm) {
        ManagedTxn tx = submittedTransaction(tm.hash);
        if (tx != null) {
            tx.emit(ManagedTxn.OnTransactionValidated.class, tm);
        } else {
            ClientLogger.log("Can't find transaction");
        }
    }

    private ManagedTxn submittedTransaction(Hash256 hash) {
        Iterator<ManagedTxn> iterator = submitted.iterator();

        while (iterator.hasNext()) {
            ManagedTxn transaction = iterator.next();
            if (transaction.hash.equals(hash)) {
                iterator.remove();
                return transaction;
            }
//            else {
                // Client.log("hash: %s != transaction.hash: %s", hash, transaction.hash);
//            }
        }
        return null;
    }
}
