package com.ripple.client.transactions;

import com.ripple.client.Client;
import com.ripple.client.Request;
import com.ripple.client.Response;
import com.ripple.client.enums.Command;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.hash.Hash256;
import com.ripple.core.types.uint.UInt32;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.encodings.common.B16;
import org.json.JSONObject;

import java.util.ArrayList;

public class TransactionManager {
    Client client;
    AccountRoot accountRoot;
    AccountID accountID;
    IKeyPair keyPair;
    public long sequence;
    public long transactionID;

    ArrayList<Transaction> submitted = new ArrayList<Transaction>();


    public TransactionManager(Client client, AccountRoot accountRoot, AccountID accountID, IKeyPair keyPair) {
        this.client = client;
        this.accountRoot = accountRoot;
        this.accountID = accountID;
        this.keyPair = keyPair;
    }

    public void queue(final Transaction transaction) {
        if (canSubmit()) {
            makeSubmitRequest(transaction);
        } else {
            // We wait for basically any old event n see if we are primed after each, angular styles
            client.on(Client.OnMessage.class, new Client.OnMessage() {
                @Override
                public void called(JSONObject jsonObject) {
                    if (canSubmit()) {
                        client.remove(Client.OnMessage.class, this);
                        makeSubmitRequest(transaction);
                    }
                }
            });
        }
    }

    private boolean canSubmit() {
        return client.serverInfo.primed() && accountRoot.primed();
    }

    private Request makeSubmitRequest(final Transaction transaction) {
        transaction.prepare(keyPair, client.serverInfo, accountRoot.Sequence);

        final Request req = client.newRequest(Command.submit);
        req.json("tx_blob", B16.toString(transaction.tx_blob));

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

    public void handleSubmitError(Transaction transaction, Response response) {
        invalidateSequence(transaction.sequence());
    }

    public void handleSubmitSuccess(Transaction transaction, Response res) {
        TransactionEngineResult tr = res.engineResult();
        switch (tr.resultClass()) {
            case telLOCAL_ERROR:
            case temMALFORMED:
            case tefFAILURE:
                break;

            case terRETRY:
            case tesSUCCESS:
                submitted.add(transaction);
                transaction.emit(Transaction.OnSubmitSuccess.class, res);
            case tecCLAIMED:
                break;
        }
    }

    private Hash256 getTxnHash(Response res) {
        return Hash256.translate.fromString(res.result.optJSONObject("tx_json").optString("hash"));
    }

    private void invalidateSequence(UInt32 sequence) {

    }

    public Transaction payment() {
        return transaction(TransactionType.Payment);
    }

    private Transaction transaction(TransactionType tt) {
        Transaction tx = new Transaction(tt, transactionID++);
        tx.put(AccountID.Account, accountID);
        return tx;
    }

    public void onTransactionResultMessage(TransactionResult tm) {
        Transaction tx = submittedTransaction(tm.hash);
        if (tx != null) {
            tx.emit(Transaction.OnTransactionValidated.class, tm);
        } else {
            Client.log("Can't find transaction");
        }
    }

    private Transaction submittedTransaction(Hash256 hash) {
        for (Transaction transaction : submitted) {
            if (transaction.hash.equals(hash)) {
                return transaction;
            } else {
                Client.log("hash: %s != transaction.hash: %s", hash, transaction.hash);
            }
        }
        return null;
    }
}
