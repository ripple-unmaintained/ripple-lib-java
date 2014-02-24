package com.ripple.cli;

import static com.ripple.cli.log.Log.LOG;

import java.io.IOException;

import com.ripple.client.pubsub.Publisher;
import com.ripple.client.transactions.ManagedTxn;
import org.json.JSONException;
import org.json.JSONObject;
import org.ripple.bouncycastle.crypto.InvalidCipherTextException;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.blobvault.BlobVault;
import com.ripple.client.responses.Response;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionResult;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;

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
            final Client client = new Client(new JavaWebSocketTransportImpl());
            client.run(new Client.ThrowingRunnable() {
                @Override
                public void throwingRun() throws Exception {
                    client.connect("wss://s1.ripple.com");
                    makePayment(client);
                }
            });
        }
    }

    private static void makePayment(Client client) throws IOException, InvalidCipherTextException, JSONException {
        BlobVault payward = new BlobVault("https://blobvault.ripple.com/");
        JSONObject blob = payward.getBlob(PAYWARD_USER, PAYWARD_PASS);
        // The blob has the master seed (the secret is deterministically derived
        // Constructing this will automatically subscribe to the accounts
        Account account = client.accountFromSeed(blob.getString("master_seed"));
        // Make the actual payment
        makePayment(account, DESTINATION_ACCOUNT, SEND_AMOUNT);
    }


    private static void makePayment(Account account, Object destination, Object amt) {
        TransactionManager tm = account.transactionManager();
        ManagedTxn tx = tm.payment();

        // Tx is an STObject subclass, an associative container of Field to
        // SerializedType. Here conversion from Object is done automatically.
        // TODO: rename translate
        tx.put(AccountID.Destination, destination);
        tx.put(Amount.Amount, amt);

        // The ManagedTxn publishes events
        Publisher<ManagedTxn.events> txEvents = tx.publisher();

        txEvents.once(ManagedTxn.OnSubmitSuccess.class, new ManagedTxn.OnSubmitSuccess() {
            @Override
            public void called(Response response) {
                LOG("Submit success response: %s", response.engineResult());
            }
        });
        txEvents.once(ManagedTxn.OnSubmitFailure.class, new ManagedTxn.OnSubmitFailure() {
            @Override
            public void called(Response response) {
                LOG("Submit failure response: %s", response.engineResult());
            }
        });
        txEvents.once(ManagedTxn.OnSubmitError.class, new ManagedTxn.OnSubmitError() {
            @Override
            public void called(Response response) {
                LOG("Submit error response: %s", response.rpcerr);
            }
        });
        txEvents.once(ManagedTxn.OnTransactionValidated.class, new ManagedTxn.OnTransactionValidated() {
            @Override
            public void called(TransactionResult result) {
                LOG("Transaction finalized on ledger: %s", result.ledgerIndex);
                LOG("Transaction message:\n%s", prettyJSON(result.message));

            }
        });
        tm.queue(tx);

    }
    private static String prettyJSON(JSONObject message) {
        try {
            return message.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
