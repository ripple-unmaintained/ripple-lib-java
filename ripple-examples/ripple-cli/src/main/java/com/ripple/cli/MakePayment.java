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
    /**
     * These refer to the blobs storing the contacts and wallet secret at payward
     * That is where the account information for the https://ripple.com/client
     * is by default stored.
     */
    public static String PAYWARD_USER = "niq1";
    public static String PAYWARD_PASS = "";

    public static String DESTINATION_ACCOUNT = "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH";
    public static Object SEND_AMOUNT = "1"; /* drop, or millionth of an XRP.
                                               Specify an amount with a decimal (eg. 1.0) to indicate XRP
                                               Sadly "1" != "1.0" in ripple json string format
                                               Be aware :)
                                               */
    static {
        ClientLogger.quiet = false;
        // Uncomment to send a non native SEND_AMOUNT
        // SEND_AMOUNT = Amount.fromString("0.00001/USD/" + DESTINATION_ACCOUNT);
    }

    public static void main(String[] args) throws Exception {
        makeAPayment();
    }

    private static void makeAPayment() throws IOException, InvalidCipherTextException, JSONException, InterruptedException {
        if (PAYWARD_USER.isEmpty() || PAYWARD_PASS.isEmpty()) {
            LOG("Must configure PAYWARD_USER && PAYWARD_PASS");
        } else {
            Client client = new Client(new JavaWebSocketTransportImpl());
            client.connect("wss://s1.ripple.com");
            BlobVault blobVault = new BlobVault("https://blobvault.payward.com/");
            JSONObject blob = blobVault.getBlob(PAYWARD_USER, PAYWARD_PASS);
            Account account = client.accountFromSeed(blob.getString("master_seed"));
            makePayment(account, DESTINATION_ACCOUNT, SEND_AMOUNT);
        }

    }

    private static void makePayment(Account account, Object destination, Object amt) {
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
