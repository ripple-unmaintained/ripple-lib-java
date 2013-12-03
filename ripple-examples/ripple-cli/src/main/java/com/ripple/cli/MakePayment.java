package com.ripple.cli;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.Response;
import com.ripple.client.blobvault.BlobVault;
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

import static com.ripple.cli.log.Log.LOG;

public class MakePayment {
    public static void main(String[] args) throws Exception {
        ClientLogger.quiet = false;
        makeAPayment();
    }

    private static void makeAPayment() throws IOException, InvalidCipherTextException, JSONException, InterruptedException {
        Client client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");
        BlobVault blobVault = new BlobVault("https://blobvault.payward.com/");
        JSONObject blob = blobVault.getBlob("niq1", "");
        Account account = client.accountFromSeed(blob.getString("master_seed"));
        makePayment(account, "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH", "1");
    }

    private static void makePayment(Account account, Object destination, String amt) {
        TransactionManager tm = account.transactionManager();
        Transaction        tx = tm.payment();

        tx.put(AccountID.Destination, destination);
        tx.put(Amount.Amount, amt);

        tx.once(Transaction.OnSubmitSuccess.class, new Transaction.OnSubmitSuccess() {
            @Override
            public void called(Response response) {
                LOG("Submit response: %s", response.engineResult());
            }
        });
        tx.once(Transaction.OnTransactionValidated.class, new Transaction.OnTransactionValidated() {
            @Override
            public void called(TransactionResult result) {
                LOG("Transaction finalized on ledger: %s", result.ledgerIndex);
                try {
                    LOG("Transaction message:\n%s", result.message.toString(4));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        tm.queue(tx);
    }
}
