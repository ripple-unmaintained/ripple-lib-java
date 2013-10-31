package com.ripple.android;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.ripple.client.Account;
import com.ripple.client.Response;
import com.ripple.client.blobvault.BlobVault;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.Transaction;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.Amount;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PayOneDrop extends Activity {
    AndroidClient client;
    Account account;

    TextView status;
    EditText username;
    EditText password;
    Button submit;

    View[] loginViews;
    Map<String, AccountID> contacts = new HashMap<String, AccountID>();

    BlobVault blobVault = new BlobVault("https://blobvault.payward.com/");
    DownloadBlobTask blobDownloadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupClient();
        setupViews();
        showLogin();
    }

    private void autoLogin(String user, String pass) {
        username.setText(user);
        password.setText(pass);
        submit.performClick();
    }

    private void setupClient() {
        client = Bootstrap.client;
        account = null;
    }

    private void setupViews() {
        status = (TextView) findViewById(R.id.status);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        submit = (Button) findViewById(R.id.submit);

        loginViews = new View[]{username, password, submit};

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean weHaveAnAccount = account != null;

                if (weHaveAnAccount) {
                    if (!account.root.primed()) {
                        setStatus("Awaiting account_info");
                    } else {
                        payNiqOneDrop(account);
                    }
                } else {
                    if (!loginFieldsValid()) {
                        setStatus("Must enter username and password");
                    } else if (blobDownloadTask == null) {
                        blobDownloadTask = new DownloadBlobTask();
                        blobDownloadTask.execute(username.getText().toString(),
                                                 password.getText().toString());
                        setStatus("Retrieving blob!");
                    } else {
                        setStatus("Waiting for blob to be retrieved!");
                    }
                }
            }
        });
    }

    private boolean accountIsUnfunded() {
        return account.root.Balance.isZero();
    }

    public Object lock = new Object();

    /**
     * This must NOT be called from the UI thread
     * @param runnable the Runnable to execute on the main thread, blocking calling while it runs
     */
    public void waitForUiThread(final Runnable runnable) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    lock.notify();
                }
            }
        } );
        try {
            lock.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleUnfundedAccount() {
        waitForUiThread(new Runnable() {
            @Override
            public void run() {
                setStatus("Account unfunded");
                showLogin();
                // TODO, need to clean up this account, remove from Client store and unbind all handlers
                account = null;
            }
        });
    }

    private boolean loginFieldsValid() {
        return username.length() > 0 && password.length() > 0;
    }

    private void setSubmitToPay() {
        submit.setVisibility(View.VISIBLE);
        submit.setText(getString(R.string.pay_niq_one_drop));
    }

    private void setViewsVisibility(int visibility, View... views) {
        for (View view : views) view.setVisibility(visibility);
    }

    private void showLogin() {
        setViewsVisibility(View.VISIBLE, loginViews);
        submit.setText(getString(R.string.login_text));
    }

    private void hideLogin() {
        setViewsVisibility(View.GONE, loginViews);
    }

    private void payNiqOneDrop(final Account account) {
        setStatus("Transaction queued " + awaitingTransactionsParenthetical(account));
        client.runImmediately(new Runnable() {
            @Override
            public void run() {
                makePayment(account, "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH", "1");
            }
        });
    }

    private void makePayment(final Account account, Object destination, Object amt) {
        TransactionManager tm = account.transactionManager();
        Transaction tx = tm.payment();

        tx.put(AccountID.Destination, destination);
        tx.put(Amount.Amount, amt);

        tx.once(Transaction.OnSubmitSuccess.class, new Transaction.OnSubmitSuccess() {
            @Override
            public void called(Response response) {
                setStatus("Transaction submitted " + awaitingTransactionsParenthetical(account));
            }
        });
        tx.once(Transaction.OnSubmitError.class, new Transaction.OnSubmitError() {
            @Override
            public void called(Response response) {
                setStatus("Transaction submission failed" + awaitingTransactionsParenthetical(account));
            }
        });
        tx.once(Transaction.OnTransactionValidated.class, new Transaction.OnTransactionValidated() {
            @Override
            public void called(TransactionResult result) {
                setStatus("Transaction finalized " + awaitingTransactionsParenthetical(account));
            }
        });
        tm.queue(tx);
    }

    private String awaitingTransactionsParenthetical(Account account) {
        return String.format("(awaiting %d)", account.transactionManager().awaiting());
    }

    private void setStatus(final String str) {
        runOnUiThread( new Runnable() {
            public void run() {
                status.setText(str);
            }
        });
    }

    private class DownloadBlobTask extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPostExecute(final JSONObject blob) {
            blobDownloadTask = null;
            if (blob == null) {
                setStatus("Failed to retrieve blob!");
                showLogin();
                return;
            }
            setStatus("Retrieved blob!");

            Runnable getAccount = new Runnable() {
                @Override
                public void run() {
                    try {
                        account = client.accountFromSeed(blob.getString("master_seed"));
                        account.root.once(AccountRoot.OnUpdate.class, new AccountRoot.OnUpdate() {
                            @Override
                            public void called(AccountRoot accountRoot) {
                                if (accountIsUnfunded()) {
                                    handleUnfundedAccount();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            client.runImmediately(getAccount);
            setSubmitToPay();
        }

        @Override
        protected void onPreExecute() {
            hideLogin();
        }

        @Override
        protected JSONObject doInBackground(String... credentials) {
            try {
                String username = credentials[0];
                String password = credentials[1];
                return blobVault.getBlob(username, password);
            } catch (Exception e) {
                return null;
            }
        }
    }

}