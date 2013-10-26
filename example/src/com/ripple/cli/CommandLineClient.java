package com.ripple.cli;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.Response;
import com.ripple.client.payward.PayWard;
import com.ripple.client.transactions.Transaction;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.enums.TransactionEngineResult;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.Amount;
import com.ripple.core.types.hash.Hash256;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class CommandLineClient {
    private static HashSet<Hash256> successfullPaymentsHashes = new HashSet<Hash256>();

    public static void LOG(String fmt, Object... args) {
        System.out.printf(fmt + "\n", args);
    }
    public static void main(String[] args) throws Exception {
        makeAPayment();
//        analyzeDump();
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

    private static void analyzeDump() throws IOException, JSONException {
        FileReader reader = new FileReader("dump.json");
        BufferedReader bufferedReader = new BufferedReader(reader);
        AccountID giveAwayAccount = AccountID.fromAddress("rMTzGg7nPPEMJthjgEBfiPZGoAM7MEVa1r");

        Amount giveAwayAmount = Amount.fromString("888.0");

        int successful = 0, created = 0;
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.length() > 0) {
                JSONObject json = new JSONObject(line);
                JSONArray transactions = json.getJSONArray("transactions");
                for (int i = 0; i < transactions.length(); i++) {
                    JSONObject tx = transactions.getJSONObject(i);
                    TransactionResult tr;
                    tr = new TransactionResult(tx, TransactionResult.Source.request_account_tx);

                    if (tr.engineResult      == TransactionEngineResult.tesSUCCESS &&
                            tr.transactionType() == TransactionType.Payment            &&
                            tr.initiatingAccount().equals(giveAwayAccount)             &&
                            tr.transaction.get(Amount.Amount).equals(giveAwayAmount)) {

                        if (tr.createdAccount() != null) created++;
                        successful++;
                    }
                }
            }
        }
        LOG("Successful outbound 888xrp payments: %d, creating: %d, paid out %s",
                successful, created, giveAwayAmount.multiply(successful));
    }
}
