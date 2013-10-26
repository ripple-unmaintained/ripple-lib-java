package com.ripple.cli;

import com.ripple.cli.log.Log;
import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.Response;
import com.ripple.client.payward.PayWard;
import com.ripple.client.transactions.Transaction;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.Amount;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MakePayment {
    public static void main(String[] args) throws Exception {
        makeAPayment();
    }

    private static void makeAPayment() throws IOException, InvalidCipherTextException, JSONException {
        Client client = new Client(new JavaWebSocketTransportImpl());
        JSONObject blob = PayWard.getBlob("niq1", "");
        String masterSeed = blob.getString("master_seed");
        Account account = client.accountFromSeed(masterSeed);
        makePayment(account, "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH", "1");
    }

    private static void makePayment(Account account, Object destination, String amt) {
        TransactionManager tm = account.transactionManager();
        Transaction tx = tm.payment();

        tx.put(AccountID.Destination, destination);
        tx.put(Amount.Amount, amt);

        tx.once(Transaction.OnSubmitSuccess.class, new Transaction.OnSubmitSuccess() {
            @Override
            public void called(Response response) {
                Log.LOG("Submit response: %s", response.engineResult());
            }
        });

        tx.once(Transaction.OnTransactionValidated.class, new Transaction.OnTransactionValidated() {
            @Override
            public void called(TransactionResult result) {
                Log.LOG("Transaction finalized on ledger: %s", result.ledgerIndex);
                try {
                    Log.LOG("Transaction message:\n%s", result.message.toString(4));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        tm.queue(tx);
    }

}
