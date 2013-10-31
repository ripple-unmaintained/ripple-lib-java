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

    BlobVault blobVault = new BlobVault("https://blobvault.payward.com/");
    DownloadBlobTask blobDownloadTask;
    String masterSeed;

    /**
     * Thread: uiThread
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupClient();
        setupViews();
        showLogin();
    }


    /**
     * Thread: uiThread
     */
    private void autoLogin(String user, String pass) {
        username.setText(user);
        password.setText(pass);
        submit.performClick();
    }

    /**
     * Thread: uiThread
     */
    private void setupClient() {
        client = Bootstrap.client;
        account = null;
    }

    /**
     * Thread: uiThread
     */
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
                        threadSafeSetStatus("Awaiting account_info");
                    } else {
                        payNiqOneDrop(account);
                    }
                } else {
                    if (!loginFieldsValid()) {
                        threadSafeSetStatus("Must enter username and password");
                    } else if (blobDownloadTask == null) {
                        blobDownloadTask = new DownloadBlobTask();
                        blobDownloadTask.execute(username.getText().toString(),
                                                 password.getText().toString());
                        threadSafeSetStatus("Retrieving blob!");
                    } else {
                        threadSafeSetStatus("Waiting for blob to be retrieved!");
                    }
                }
            }
        });
    }

    /**
     * Thread: Client thread
     */
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

    /**
     * Thread: client thread
     */
    private void handleUnfundedAccount() {
        waitForUiThread(new Runnable() {
            @Override
            public void run() {
                threadSafeSetStatus("Account unfunded");
                showLogin();
                // TODO, need to clean up this account, remove from Client store and unbind all handlers
                account = null;
            }
        });
    }

    /**
     * Thread: uiThread
     */
    private boolean loginFieldsValid() {
        return username.length() > 0 && password.length() > 0;
    }

    /**
     * Thread: uiThread
     */
    private void setSubmitToPay() {
        submit.setVisibility(View.VISIBLE);
        submit.setText(getString(R.string.pay_niq_one_drop));
    }

    /**
     * Thread: uiThread
     */
    private void setViewsVisibility(int visibility, View... views) {
        for (View view : views) view.setVisibility(visibility);
    }


    /**
     * Thread: uiThread
     */
    private void showLogin() {
        setViewsVisibility(View.VISIBLE, loginViews);
        submit.setText(getString(R.string.login_text));
    }

    /**
     * Thread: uiThread
     */
    private void hideLogin() {
        setViewsVisibility(View.GONE, loginViews);
    }

    /**
     * Thread: uiThread
     */
    private void payNiqOneDrop(final Account account) {
        threadSafeSetStatus("Transaction queued " + awaitingTransactionsParenthetical(account));
        client.runImmediately(new Runnable() {
            @Override
            public void run() {
                makePayment(account, "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH", "1");
            }
        });
    }

    /**
     * Thread: Client thread
     */
    private void makePayment(final Account account, Object destination, Object amt) {
        TransactionManager tm = account.transactionManager();
        Transaction tx = tm.payment();

        tx.put(AccountID.Destination, destination);
        tx.put(Amount.Amount, amt);

        tx.once(Transaction.OnSubmitSuccess.class, new Transaction.OnSubmitSuccess() {
            @Override
            public void called(Response response) {
                threadSafeSetStatus("Transaction submitted " + awaitingTransactionsParenthetical(account));
            }
        });
        tx.once(Transaction.OnSubmitError.class, new Transaction.OnSubmitError() {
            @Override
            public void called(Response response) {
                threadSafeSetStatus("Transaction submission failed" + awaitingTransactionsParenthetical(account));
            }
        });
        tx.once(Transaction.OnTransactionValidated.class, new Transaction.OnTransactionValidated() {
            @Override
            public void called(TransactionResult result) {
                threadSafeSetStatus("Transaction finalized " + awaitingTransactionsParenthetical(account));
            }
        });
        tm.queue(tx);
    }

    /**
     * Thread: any (doesn't REALLY matter ?)
     */
    private String awaitingTransactionsParenthetical(Account account) {
        return String.format("(awaiting %d)", account.transactionManager().awaiting());
    }

    /**
     * Thread: any
     */
    private void threadSafeSetStatus(final String str) {
        runOnUiThread( new Runnable() {
            public void run() {
                status.setText(str);
            }
        });
    }

    /**
     * Thread: client thread
     */
    Runnable getAccount = new Runnable() {
        @Override
        public void run() {
            account = client.accountFromSeed(masterSeed);
            account.root.once(AccountRoot.OnUpdate.class, new AccountRoot.OnUpdate() {
                @Override
                public void called(AccountRoot accountRoot) {
                    if (accountIsUnfunded()) {
                        handleUnfundedAccount();
                    }
                }
            });
        }
    };

    /**
     * Thread: any
     */
    private class DownloadBlobTask extends AsyncTask<String, String, JSONObject> {
        /**
         * Thread: uiThread
         */
        @Override
        protected void onPostExecute(final JSONObject blob) {
            blobDownloadTask = null;
            if (blob == null) {
                threadSafeSetStatus("Failed to retrieve blob!");
                showLogin();
                return;
            }
            threadSafeSetStatus("Retrieved blob!");

            try {
                masterSeed = blob.getString("master_seed");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            client.runImmediately(getAccount);
            setSubmitToPay();
        }

        /**
         * Thread: uiThread
         */
        @Override
        protected void onPreExecute() {
            hideLogin();
        }

        /**
         * Thread: own
         */
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