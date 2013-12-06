package com.ripple.cli;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.responses.Response;
import com.ripple.client.blobvault.BlobVault;
import com.ripple.client.transactions.ManagedTransaction;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionResult;
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
     *
     * These can be set with operating system environment variables
     */
    public static String PAYWARD_USER = "";
    public static String PAYWARD_PASS = "";

    public static String DESTINATION_ACCOUNT = "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH";
    public static Object SEND_AMOUNT = "1"; /* drop, or millionth of an XRP.
                                               Specify an amount with a decimal (eg. 1.0) to indicate XRP
                                               Sadly "1" != "1.0" in ripple json string format
                                               Be aware :)
                                               */
    static {
        String envPass = System.getenv("PAYWARD_PASS");
        String envUser = System.getenv("PAYWARD_USER");

        if (envUser != null) PAYWARD_USER = envUser;
        if (envPass != null) PAYWARD_PASS = envPass;
        ClientLogger.quiet = false;

        // Uncomment to send a non native SEND_AMOUNT
        // SEND_AMOUNT = Amount.fromString("0.00001/USD/" + DESTINATION_ACCOUNT);
    }

    public static void main(String[] args) throws Exception {
        if (PAYWARD_USER.isEmpty() || PAYWARD_PASS.isEmpty()) {
            LOG("Must configure PAYWARD_USER && PAYWARD_PASS");
        }
        else {
            makeAPayment();
        }
    }

    private static void makeAPayment() throws IOException, InvalidCipherTextException, JSONException, InterruptedException {
        // Create a client instance using the Java WebSocket Transport
        Client client = new Client(new JavaWebSocketTransportImpl());
        // Connect to s1
        client.connect("wss://s1.ripple.com");
        // We want to access a blob from payward
        BlobVault payward = new BlobVault("https://blobvault.payward.com/");
        JSONObject blob = payward.getBlob(PAYWARD_USER, PAYWARD_PASS);
        // The blob has the master seed (the secret is deterministically derived
        // Constructing this will automatically subscribe to the accounts
        Account account = client.accountFromSeed(blob.getString("master_seed"));
        // Make the actual payment
        makePayment(account, DESTINATION_ACCOUNT, SEND_AMOUNT);
    }

    private static void makePayment(Account account, Object destination, Object amt) {
        TransactionManager tm = account.transactionManager();
        ManagedTransaction tx = tm.payment();

        // tx is an STObject subclass, an associative container of Field to
        // SerializedType. Here conversion from Object is done automatically.
        tx.put(AccountID.Destination, destination);
        tx.put(Amount.Amount, amt);

        // The ManagedTransaction publishes events
        tx.once(ManagedTransaction.OnSubmitSuccess.class, new ManagedTransaction.OnSubmitSuccess() {
            @Override
            public void called(Response response) {
                LOG("Submit response: %s", response.engineResult());
            }
        });
        tx.once(ManagedTransaction.OnTransactionValidated.class, new ManagedTransaction.OnTransactionValidated() {
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

        // This doesn't necessarily immediately submit, it may need to wait until the AccountRoot
        // information has been retrieved from the server, to get the Sequence.
        tm.queue(tx);
    }
}
