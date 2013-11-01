package com.ripple.android.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.ripple.android.client.AndroidClient;
import com.ripple.android.Bootstrap;
import com.ripple.android.R;
import com.ripple.client.Account;
import com.ripple.client.Response;
import com.ripple.client.blobvault.BlobVault;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.Transaction;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transactions.TransactionMessage.TransactionResult;
import com.ripple.core.types.AccountID;
import com.ripple.core.types.Amount;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PayOneDrop extends Activity {
    AndroidClient client;
    Account account;

    TextView status;
    EditText username;
    EditText password;
    Button submit;
    Spinner contacts;
    ArrayAdapter<String> contactsAdapter;
    ArrayList<AccountID> contactsAddresses = new ArrayList<AccountID>(); // parrallel array

    View[] loginViews;
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

        tryLoadDevcredentialsFromAssets();
    }

    private void tryLoadDevcredentialsFromAssets() {
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
        submit.performClick();
    }

    /**
     * Thread: ui thread
     */
    private void setupClient() {
        client = Bootstrap.client;
        account = null;
    }

    /**
     * Thread: ui thread
     */
    private void setupViews() {
        status = (TextView) findViewById(R.id.status);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        submit = (Button) findViewById(R.id.submit);
        contacts = (Spinner) findViewById(R.id.contacts);
        contactsAdapter = new ArrayAdapter<String>(this, R.layout.contacts_text_view);
        contacts.setAdapter(contactsAdapter);
        loginViews = new View[]{username, password, submit};

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean weHaveAnAccount = account != null;

                if (weHaveAnAccount) {
                    if (!account.root.primed()) {
                        threadSafeSetStatus("Awaiting account_info");
                    } else {
                        payOneDrop(account);
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
     * @param runnable the Runnable to execute on the pay_one_drop thread, blocking calling while it runs
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
                showOnlyLogin();
                // TODO, need to clean up this account, remove from Client store and unbind all handlers
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
    private void setSubmitToPay() {
        submit.setVisibility(View.VISIBLE);
        contacts.setVisibility(View.VISIBLE);
        submit.setText(getString(R.string.pay_one_drop));
    }

    /**
     * Thread: ui thread
     */
    private void setViewsVisibility(int visibility, View... views) {
        for (View view : views) view.setVisibility(visibility);
    }


    /**
     * Thread: ui thread
     */
    private void showOnlyLogin() {
        setViewsVisibility(View.VISIBLE, loginViews);
        setViewsVisibility(View.GONE, contacts);
        submit.setText(getString(R.string.login_text));
    }

    /**
     * Thread: ui thread
     */
    private void hideAllButStatus() {
        setViewsVisibility(View.GONE, loginViews);
        setViewsVisibility(View.GONE, contacts);
    }

    /**
     * Thread: ui thread
     */
    private void payOneDrop(final Account account) {
        client.run(new Runnable() {
            @Override
            public void run() {
                makePayment(account, contactsAddresses.get(contacts.getSelectedItemPosition()), "1");
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
            return String.format("(awaiting %d)", awaiting);
        }
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
            client.runPrioritized(getAccount);
            setSubmitToPay();
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
                addContact(contact.getString("name"),
                        contact.getString("address"));
            }
        }
        catch (JSONException e) {
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