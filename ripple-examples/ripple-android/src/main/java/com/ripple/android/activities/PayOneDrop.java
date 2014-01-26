
package com.ripple.android.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.ripple.client.transactions.ManagedTxn;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ripple.android.JSON;
import com.ripple.android.Logger;
import com.ripple.android.R;
import com.ripple.android.RippleApplication;
import com.ripple.android.client.AndroidClient;
import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.blobvault.BlobVault;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionResult;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;

public class PayOneDrop extends Activity {
    AndroidClient client;

    Account account;

    TextView status;

    TextView messageLog;

    RelativeLayout.LayoutParams statusLayoutParams;

    EditText username;

    EditText password;

    Button retrieveWallet;

    Button payOneDrop;

    Spinner contacts;

    LinearLayout loginForm;

    RelativeLayout paymentForm;

    ArrayAdapter<String> contactsAdapter;

    ArrayList<AccountID> contactsAddresses = new ArrayList<AccountID>(); // parallel
                                                                         // array

    BlobVault blobVault = new BlobVault("https://blobvault.payward.com/");

    DownloadBlobTask blobDownloadTask;

    String masterSeed;

    /**
     * Thread: ui thread
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_one_drop);
        setupClient();
        setupViews();
        showOnlyLogin();

        tryLoadDevCredentialsFromAssets();

    }

    @SuppressWarnings("unused")
    private void tryLoadDevCredentialsFromAssets() {
        try {
            String fileName = "dev-credentials.json";
            String s = assetFileText(fileName);
            JSONObject credentials = new JSONObject(s);

            String user = credentials.getString("username");
            String pass = credentials.getString("password");
            autoLogin(user, pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String assetFileText(String fileName) throws IOException {
        InputStream open = getAssets().open(fileName);
        InputStreamReader streamReader = new InputStreamReader(open);
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder builder = new StringBuilder();

        String text;
        while ((text = reader.readLine()) != null) {
            builder.append(text);
        }
        return builder.toString();
    }

    /**
     * Thread: ui thread
     */
    private void autoLogin(String user, String pass) {
        username.setText(user);
        password.setText(pass);
        retrieveWallet.performClick();
    }

    /**
     * Thread: ui thread
     */
    private void setupClient() {
        client = ((RippleApplication) getApplication()).getClient();
        account = null;
    }

    /**
     * Thread: ui thread
     */
    private void setupViews() {
        status = (TextView) findViewById(R.id.status);
        messageLog = (TextView) findViewById(R.id.messageLog);
        statusLayoutParams = (RelativeLayout.LayoutParams) status.getLayoutParams();

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        retrieveWallet = (Button) findViewById(R.id.retrieve_wallet);
        payOneDrop = (Button) findViewById(R.id.pay_one_drop);

        contacts = (Spinner) findViewById(R.id.contacts);
        contactsAdapter = new ArrayAdapter<String>(this, R.layout.contacts_text_view);
        contacts.setAdapter(contactsAdapter);

        loginForm = (LinearLayout) findViewById(R.id.loginForm);
        paymentForm = (RelativeLayout) findViewById(R.id.payment_form);

        // TODO, perhaps we should use a broadcast receiver?
        client.on(Client.OnMessage.class, new Client.OnMessage() {
            @Override
            public void called(JSONObject jsonObject) {
                final String pretty = JSON.prettyJSON(jsonObject);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageLog.setText(pretty);
                    }
                });
            }
        });

        payOneDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!account.getAccountRoot().primed()) {
                    threadSafeSetStatus("Awaiting account_info");
                } else {
                    payOneDrop(account);
                }
            }
        });

        retrieveWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loginFieldsValid()) {
                    threadSafeSetStatus("Must enter username and password");
                } else if (blobDownloadTask == null) {
                    blobDownloadTask = new DownloadBlobTask();
                    blobDownloadTask.execute(username.getText().toString(), password.getText()
                            .toString());
                    threadSafeSetStatus("Retrieving blob!");
                } else {
                    threadSafeSetStatus("Waiting for blob to be retrieved!");
                }
            }
        });
    }

    /**
     * Thread: Client thread
     */
    private boolean accountIsUnfunded() {
        return account.getAccountRoot().getBalance().isZero();
    }

    public final Object lock = new Object();

    /**
     * This must NOT be called from the UI thread
     * 
     * @param runnable the Runnable to execute on the pay_one_drop thread,
     *            blocking calling while it runs
     */
    public void waitForUiThread(final Runnable runnable) {
        if (runningFromUiThread()) {
            throw new RuntimeException("Don't call `waitForUiThread` from ui thread!");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }
        });
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean runningFromUiThread() {
        return Thread.currentThread() == getMainLooper().getThread();
    }

    /**
     * Thread: client thread
     */
    private void handleUnfundedAccount() {
        waitForUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.LOG("Account is unfunded");
                threadSafeSetStatus("Account unfunded");
                showOnlyLogin();
                // TODO, need to clean up this account, removeListener from
                // Client store and unbind all handlers
                account = null;
            }
        });
    }

    /**
     * Thread: ui thread
     */
    private boolean loginFieldsValid() {
        return username.length() > 0 && password.length() > 0;
    }

    /**
     * Thread: ui thread
     */
    private void showPaymentForm() {
        statusLayoutParams.addRule(RelativeLayout.ABOVE, 0);
        paymentForm.setVisibility(View.VISIBLE);
        // retrieveWallet.setVisibility(View.VISIBLE);
        // contacts.setVisibility(View.VISIBLE);
        // retrieveWallet.setText(getString(R.string.pay_one_drop));
    }

    /**
     * Thread: ui thread
     */
    private void setViewsVisibility(int visibility, View... views) {
        for (View view : views)
            view.setVisibility(visibility);
    }

    /**
     * Thread: ui thread
     */
    private void showOnlyLogin() {
        statusLayoutParams.addRule(RelativeLayout.ABOVE, R.id.loginForm);
        // status.setLayoutParams();

        setViewsVisibility(View.VISIBLE, loginForm);
        setViewsVisibility(View.GONE, paymentForm);
    }

    /**
     * Thread: ui thread
     */
    private void hideAllButStatus() {
        setViewsVisibility(View.GONE, loginForm);
        setViewsVisibility(View.GONE, paymentForm);
    }

    /**
     * Thread: ui thread
     */
    private void payOneDrop(final Account account) {
        final AccountID destination = selectedContact();

        client.run(new Runnable() {
            @Override
            public void run() {
                makePayment(account, destination, "1");
            }
        });
    }

    /**
     * Thread: ui thread
     */
    private AccountID selectedContact() {
        return contactsAddresses.get(contacts.getSelectedItemPosition());
    }

    /**
     * Thread: Client thread
     */
    private void makePayment(final Account account, Object destination, Object amt) {
        TransactionManager tm = account.transactionManager();
        ManagedTxn tx = tm.payment();

        tx.put(AccountID.Destination, destination);
        tx.put(Amount.Amount, amt);

        tx.publisher().once(ManagedTxn.OnSubmitSuccess.class, new ManagedTxn.OnSubmitSuccess() {
            @Override
            public void called(Response response) {
                threadSafeSetStatus("Transaction submitted "
                        + awaitingTransactionsParenthetical(account));
            }
        });
        tx.publisher().once(ManagedTxn.OnSubmitFailure.class, new ManagedTxn.OnSubmitFailure() {
            @Override
            public void called(Response response) {
                threadSafeSetStatus("Transaction submission failed"
                        + awaitingTransactionsParenthetical(account));
            }
        });
        tx.publisher().once(ManagedTxn.OnTransactionValidated.class,
                new ManagedTxn.OnTransactionValidated() {
                    @Override
                    public void called(TransactionResult result) {
                        threadSafeSetStatus("Transaction finalized "
                                + awaitingTransactionsParenthetical(account));
                    }
                });
        tm.queue(tx);
        threadSafeSetStatus("Transaction queued " + awaitingTransactionsParenthetical(account));
    }

    /**
     * Thread: client thread
     */
    private String awaitingTransactionsParenthetical(Account account) {
        int awaiting = account.transactionManager().awaiting();
        if (awaiting == 0) {
            return "";
        } else {
            ArrayList<ManagedTxn> queued = account.transactionManager().sequenceSortedQueue();
            String s = "";

            for (ManagedTxn fields : queued) {
                s = s + fields.transactionType() + ",";
            }

            return String.format("(awaiting %s %d)", s, awaiting);
        }
    }

    /**
     * Thread: any
     */
    private void threadSafeSetStatus(final String str) {
        runOnUiThread(new Runnable() {
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
            account.getAccountRoot().once(AccountRoot.OnUpdate.class, new AccountRoot.OnUpdate() {
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
         * Thread: ui thread
         */
        @Override
        protected void onPostExecute(final JSONObject blob) {
            blobDownloadTask = null;
            if (blob == null) {
                threadSafeSetStatus("Failed to retrieve blob!");
                showOnlyLogin();
                return;
            }
            threadSafeSetStatus("Retrieved blob!");

            try {
                masterSeed = blob.getString("master_seed");
                populateContactsSpinner(blob.optJSONArray("contacts"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            client.run(getAccount);
            showPaymentForm();
        }

        /**
         * Thread: ui thread
         */
        @Override
        protected void onPreExecute() {
            hideAllButStatus();
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

    /**
     * Thread: ui thread
     */
    private void populateContactsSpinner(JSONArray rawContacts) {
        contactsAdapter.clear();

        try {
            addContact("Niq", "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH");
            for (int i = 0; i < rawContacts.length(); i++) {
                JSONObject contact = rawContacts.getJSONObject(i);
                addContact(contact.getString("name"), contact.getString("address"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Thread: ui thread
     */
    private void addContact(String niq, String address) {
        contactsAdapter.add(niq);
        contactsAddresses.add(AccountID.fromString(address));
    }

}
