package com.ripple.cli;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.Request;
import com.ripple.client.Response;
import com.ripple.client.enums.Command;
import com.ripple.client.payward.PayWard;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.Transaction;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.Amount;
import com.ripple.core.types.STObject;
import com.ripple.core.types.hash.Hash256;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class CommandLineClient {
    public static void LOG(String fmt, Object... args) {
        System.out.printf(fmt + "\n", args);
    }
    public static void main(String[] args) throws Exception {
        final Client client;
        Client.quiet = true;

        client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");

        JSONObject blob = PayWard.getBlob("niq1", "");
        String masterSeed = blob.getString("master_seed");
        Account account = client.accountFromSeed(masterSeed);

        makePayment(account, "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH", "1");
    }

    private static void logTransactionHistory(final Client client, final Account account) {
        account.root.once(AccountRoot.OnUpdate.class, new AccountRoot.OnUpdate() {
            @Override
            public void called(AccountRoot accountRoot) {
                logTransactionThread(accountRoot.PreviousTxnID, client, account, 0);
            }
        });
    }

    private static Hash256 hash256FromHex(String value) {
        Hash256 hash;
        hash = Hash256.translate.fromString(value);
        return hash;
    }

    private static void logTransactionThread(final Hash256 starting, final Client client, final AccountID forAccount, final int n) {
        Request request = client.newRequest(Command.tx);
        request.json("transaction", starting);

        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.error != null) {
                    LOG("Error with %s", prettyJSON(response.message));
                    return;
                }

                JSONObject cloned = cloneJSON(response.message);

                try {
                    TransactionResult tr = new TransactionResult(response.result,
                            TransactionResult.Source.request_tx_result);
                    Map<AccountID, STObject> affected = tr.affectedRoots();


                    boolean found = false;
                    for (AccountID accountID : affected.keySet()) {
                        if (accountID.equals(forAccount)) {
                            STObject accountMeta = affected.get(forAccount);
                            STObject finalFields = accountMeta.get(STObject.FinalFields),
                                    previousFields = accountMeta.get(STObject.PreviousFields);

                            if (finalFields != null) {
                                found = true;
                                LOG("%s %-4s %s (%s)",
                                        forAccount.equals(tr.initiatingAccount()) ? "->" : "<-",
                                        n,
                                        tr.transactionType(),
                                        starting);

                                if (tr.transactionType() == TransactionType.Payment) {
                                    LOG("Amount is %s", tr.transaction.get(Amount.Amount));
                                }

                                if (previousFields != null) {
                                    for (Field f : previousFields) {
                                        LOG("    %-15s:  %-30s ==> %-20s", f + ":", previousFields.get(f), finalFields.get(f));
                                    }
                                }

                                Hash256 newTip = accountMeta.get(Hash256.PreviousTxnID);
                                logTransactionThread(newTip, client, forAccount, n + 1);
                            }
                        }
                    }
                    if (!found) {
                        LOG("This is the end\n%s", prettyJSON(cloned));
                    }
                } catch (Exception e) {
                    LOG("The request was %s", response.request.cmd);
                    LOG("There was an exception, data was: %s", prettyJSON(cloned));
                    throw new RuntimeException(e);
                }
            }

        });
        request.request();
    }

    private static JSONObject cloneJSON(JSONObject message) {
        return Client.parseJSON(message.toString());
    }

    private static String prettyJSON(JSONObject message) {
        try {
            return message.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
}
