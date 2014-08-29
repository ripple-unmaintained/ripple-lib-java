
package com.ripple.android.activities;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.ripple.android.JSON;
import com.ripple.android.Logger;
import com.ripple.android.R;
import com.ripple.android.RippleApplication;
import com.ripple.android.client.AndroidClient;
import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.blobvault.BlobVault;
import com.ripple.client.pubsub.CallbackContext;
import com.ripple.client.payments.Alternative;
import com.ripple.client.payments.Alternatives;
import com.ripple.client.payments.PaymentFlow;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Currency;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import static com.ripple.client.Client.log;

public class PaymentAlternatives extends Activity {
    private final Client.OnSendMessage onClientSendMessage = new Client.OnSendMessage() {
        @Override
        public void called(JSONObject jsonObject) {
            logMessage(jsonObject, true);
        }
    };
    AndroidClient client;
    Account account;

    PaymentFlow flow;

    RelativeLayout.LayoutParams statusLayoutParams;
    TextView status;
    TextView messageLog;

    InputMethodManager imm;

    LinearLayout loginForm;
    RelativeLayout paymentForm;

    EditText username;
    EditText password;
    Button retrieveWallet;

    Spinner currencySpinner;
    EditText destinationAmountInput;
    BigDecimal destinationAmountValue;
    Currency destinationCurrency;

    Spinner contacts;
    ArrayAdapter<String> contactsAdapter;
    ArrayList<AccountID> contactsAddresses = new ArrayList<AccountID>(); // parallel
    AccountID destination;

    BlobVault blobVault = new BlobVault("https://blobvault.ripple.com/");
    DownloadBlobTask blobDownloadTask;
    String masterSeed;

    LinearLayout alternativesGroup;

    PaymentFlow.OnAlternatives onAlternatives = new PaymentFlow.OnAlternatives() {
        @Override
        public void called(final Alternatives alternatives) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAlternatives(alternatives);
                }
            });
        }
    };

    PaymentFlow.OnAlternativesStale onAlternativesStale = new PaymentFlow.OnAlternativesStale() {
        @Override
        public void called(final Alternatives alternatives) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tearDownAlternatives(alternatives);
                }
            });
        }
    };
    private Client.OnMessage onClientMessage = new Client.OnMessage() {
        @Override
        public void called(JSONObject jsonObject) {
            logMessage(jsonObject, false);
        }
    };

    /**
     * Thread: ui thread
     */
    private void showAlternatives(Alternatives alternatives) {

        // We only want to update alternatives that have changed
        // This is a whole lot of work to make sure that it's not so glitchy
        HashMap<Alternative, Button> same = new HashMap<Alternative, Button>();
        for (Alternative alternative : alternatives) {
            // Alternatives of a given equality are recycled ;)
            same.put(alternative, (Button) alternativesGroup.findViewWithTag(alternative));
        }
        // Hrmm ???
        alternativesGroup.removeAllViews();

        // This can be noisy, TODO: pulsing status
        if (account.transactionManager().txnsPending() == 0) {
            if (alternatives.size() == 0) {
                threadSafeSetStatus("No payment paths found! (yet)");
            } else {
                threadSafeSetStatus(String.format("Found %d alternatives", alternatives.size()));
            }
        }

        for (final Alternative alternative : alternatives) {
            Button button = same.get(alternative);

            if (button != null) {
                log(Level.INFO, "Reusing existing button for alternative");
            }

            if (button == null) {
                // We can't recycle, so we'll create a new one
                button = new Button(this);
                button.setText(alternative.sourceAmount.toText());
                button.setTag(alternative);
                final String contactName = contactsAdapter.getItem(contacts.getSelectedItemPosition());

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideKeyBoard();
                        client.run(new Runnable() {
                            @Override
                            public void run() {
                                // TODO, doing a send max here is retarded ;)
                                BigDecimal sendMaxMultiplier = new BigDecimal("1.01");
                                ManagedTxn payment = flow.createPayment(alternative, sendMaxMultiplier);
                                Amount destAmount = payment.txn.get(Amount.Amount);

                                String path;
                                if (!alternative.directXRP()) {
                                    path = String.format("%s>%s",
                                            alternative.sourceAmount.toText(),
                                            destAmount.currencyString());
                                } else {
                                    path = destAmount.toText();
                                }

                                payment.setDescription(String.format("%s>%s",
                                        path,
                                        contactName));

                                setTransactionStatusHandlers(account, payment);
                                account.transactionManager().queue(payment);
                                threadSafeSetStatus("Transaction queued " + awaitingTransactionsParenthetical(account));
                            }
                        });
                    }
                });
            }
            alternativesGroup.addView(button);
        }
        alternativesGroup.setVisibility(View.VISIBLE);
    }

    /**
     * Thread: ui thread
     */
    private void tearDownAlternatives(Alternatives alternatives) {
        alternativesGroup.setVisibility(View.GONE);
        threadSafeSetStatus("Path find canceled");
    }


    private boolean activityDestroyed = false;
    /**
     * Thread: ui thread
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_iou);
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

    @Override
    protected void onNewIntent(Intent intent) {
        log(Level.INFO, intent.getAction());
        super.onNewIntent(intent);

        Uri data = intent.getData();
        if (data != null) {
            if (data.getPath().matches("^/+contact.*")) {
                String to = data.getQueryParameter("to");
                AccountID contact = AccountID.fromAddress(to);
                // We haven't bootstrapped yet
                if (contactsAdapter.isEmpty()) {
                    if (!contactsAddresses.contains(contact)) {
                        contactsAddresses.add(contact);
                    }
                }
                // We've already bootstrapped contacts from the blobvault
                else {
                    int position;
                    position = contactsAddresses.indexOf(contact);
                    if (position == -1) {
                        unshiftContact(to.substring(0, 16) + "...", to);
                        contacts.setSelection(0, true);
                    } else {
                        contacts.setSelection(position, true);
                    }
                }
            }
        }
    }

    /**
     * Thread: ui thread
     */
    private void setupClient() {
        client = ((RippleApplication) getApplication()).getClient();
        flow = new PaymentFlow(client);
        account = null;
    }

    public void setDestinationAmount() {
        try {
            destinationAmountValue = new BigDecimal(getInputText(destinationAmountInput));
            normalizeDestinationAmount();
        } catch (Exception e) {
            destinationAmountValue = null;
        }
        client.run(setFlowAmount);
    }

    /**
     * Thread: ui thread
     */
    private void setupViews() {
        imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
        status = (TextView) findViewById(R.id.status);
        messageLog = (TextView) findViewById(R.id.messageLog);
        statusLayoutParams = (RelativeLayout.LayoutParams) status.getLayoutParams();

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        alternativesGroup = (LinearLayout) findViewById(R.id.alternatives);

        retrieveWallet = (Button) findViewById(R.id.retrieve_wallet);

        contacts = (Spinner) findViewById(R.id.contacts);
        contactsAdapter = new ArrayAdapter<String>(this, R.layout.contacts_text_view);
        contacts.setAdapter(contactsAdapter);

        loginForm = (LinearLayout) findViewById(R.id.loginForm);
        paymentForm = (RelativeLayout) findViewById(R.id.payment_form);

        currencySpinner = (Spinner) findViewById(R.id.currencies);
        destinationAmountInput = (EditText) findViewById(R.id.amountInput);
        destinationAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setDestinationAmount();
            }
        });

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSelectedCurrency();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        contacts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSelectedDestination();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        client.on(Client.OnMessage.class,  activityLifeCycled, onClientMessage);
        client.on(Client.OnSendMessage.class, activityLifeCycled, onClientSendMessage);

        retrieveWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loginFieldsValid()) {
                    threadSafeSetStatus("Must enter username and password");
                } else if (activeBlobRequest()) {
                    threadSafeSetStatus("Waiting for blob to be retrieved!");
                } else {
                    initiateBlobRequest(getInputText(username), getInputText(password));
                }
            }

        });
    }

    private void initiateBlobRequest(String username, String password) {
        blobDownloadTask = new DownloadBlobTask();
        blobDownloadTask.execute(username, password);
        threadSafeSetStatus("Retrieving blob!");
        hideKeyBoard();
    }

    private String getInputText(EditText username1) {
        return username1.getText().toString();
    }

    private boolean activeBlobRequest() {
        return blobDownloadTask != null;
    }

    public void logMessage(JSONObject jsonObject, final boolean sending) {
        final String pretty = JSON.prettyJSON(jsonObject);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMessageColor(sending ? R.color.sendingBlue : R.color.receivingRed);
                messageLog.setText(pretty);
            }
        });
    }

    public void setMessageColor(int color) {
        messageLog.setTextColor(getResources().getColor(color));
    }

    /**
     * Thread: UI thread
     */
    public void setSelectedCurrency() {
        String selectedCurrency = (String) currencySpinner.getSelectedItem();
        destinationCurrency = Currency.fromString(selectedCurrency);
        normalizeDestinationAmount();

        if (flow != null) {
            client.run(setFlowCurrency);
        }
    }

    private void normalizeDestinationAmount() {
        if (destinationCurrency != null && destinationAmountValue != null) {
            BigDecimal normalized = Amount.roundValue(destinationAmountValue, destinationCurrency.isNative());

            if (destinationAmountValue.stripTrailingZeros().compareTo(normalized.stripTrailingZeros()) != 0) {
                String text = normalized.stripTrailingZeros().toPlainString();
                destinationAmountInput.setText(text);
                destinationAmountInput.setSelection(text.length());
            }
        }
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

    @Override
    protected void onDestroy() {
        unsubscribeFromClientEvents();
        activityDestroyed = true;
        super.onDestroy();

    }

    // TODO really need a way of creating Publisher contexts to automatically
    // clean up
    // What about the transaction handling methods ??
    private void unsubscribeFromClientEvents() {
//        client.removeListener(Client.OnMessage.class, onClientMessage);
//        client.removeListener(Client.OnSendMessage.class, onClientSendMessage);
        if (flow != null) {
            flow.abort();
            flow.unsubscribeFromClientEvents();
        }
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
    private AccountID selectedContact() {
        return contactsAddresses.get(contacts.getSelectedItemPosition());
    }

    private CallbackContext activityLifeCycled = new CallbackContext() {
        @Override
        public boolean shouldExecute() {
            return !activityDestroyed;
        }

        @Override
        public boolean shouldRemove() {
            return activityDestroyed;
        }
    };
    private void setTransactionStatusHandlers(final Account account, ManagedTxn tx) {
        tx.once(ManagedTxn.OnSubmitSuccess.class, activityLifeCycled,
            new ManagedTxn.OnSubmitSuccess() {
                @Override
                public void called(Response response) {
                    flow.makePathFindRequestIfNoneAlready();
                    threadSafeSetStatus("Transaction submitted "
                            + awaitingTransactionsParenthetical(account));
                }
        });

        tx.once(ManagedTxn.OnSubmitFailure.class, activityLifeCycled,
            new ManagedTxn.OnSubmitFailure() {
                @Override
                public void called(Response response) {
                    flow.makePathFindRequestIfNoneAlready();
                    threadSafeSetStatus("Transaction submission failed (" + response.engineResult() + ")"
                            + awaitingTransactionsParenthetical(account));
                }
        });

        tx.once(ManagedTxn.OnSubmitError.class, activityLifeCycled,
            new ManagedTxn.OnSubmitError() {
                @Override
                public void called(Response response) {
                    flow.makePathFindRequestIfNoneAlready();
                    threadSafeSetStatus("Transaction submission error (" + response.rpcerr + ")"
                            + awaitingTransactionsParenthetical(account));
                }
        });

        tx.once(ManagedTxn.OnTransactionValidated.class, activityLifeCycled,
            new ManagedTxn.OnTransactionValidated() {
                @Override
                public void called(TransactionResult result) {
                    flow.makePathFindRequestIfNoneAlready();
                    threadSafeSetStatus("Transaction finalized "
                            + awaitingTransactionsParenthetical(account));
                }
            });
    }

    /**
     * Thread: client thread
     */
    private String awaitingTransactionsParenthetical(Account account) {
        int awaiting = account.transactionManager().txnsPending();
        if (awaiting == 0) {
            return "";
        } else {
            ArrayList<ManagedTxn> queued = account.transactionManager().pendingSequenceSorted();
            String s = "";

            int n = queued.size();
            for (ManagedTxn txn : queued) {
                s += txn.description();
                if (--n != 0) s += "\n";
            }

            return String.format("(awaiting:\n %s)", s);
        }
    }


    private void hideKeyBoard() {
        imm.hideSoftInputFromWindow(paymentForm.getWindowToken(), 0);
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
                    else {
                        flow.setSource(account);

                        flow.on(PaymentFlow.OnAlternatives.class,      activityLifeCycled, onAlternatives);
                        flow.on(PaymentFlow.OnAlternativesStale.class, activityLifeCycled, onAlternativesStale);
                        flow.on(PaymentFlow.OnPathFind.class,          activityLifeCycled, new PaymentFlow.OnPathFind() {
                            @Override
                            public void called(Request request) {
                                threadSafeSetStatus("Searching for alternatives");
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setSelectedCurrency();
                                setSelectedDestination();
                                destinationAmountInput.requestFocus();
                                String intitialValue = "0.0001"; // TODO
                                destinationAmountInput.setText(intitialValue);
                                destinationAmountInput.setSelection(intitialValue.length());

                                destinationAmountInput.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        imm.showSoftInput(destinationAmountInput, 0);
                                    }
                                }, 10);
                            }
                        });
                    }
                }
            });

        }
    };

    private void setSelectedDestination() {
        destination = selectedContact();
        if (flow != null) {
            client.run(setFlowDestination);
        }
    }

    /**
     * Thread: client thread
     */
    Runnable setFlowDestination = new Runnable() {
        @Override
        public void run() {
            flow.setDestination(destination);
        }
    };

    /**
     * Thread: client thread
     */
    Runnable setFlowCurrency = new Runnable() {
        @Override
        public void run() {
            flow.setDestinationAmountCurrency(destinationCurrency);
        }
    };

    /**
     * Thread: client thread
     */
    Runnable setFlowAmount = new Runnable() {
        @Override
        public void run() {
            flow.setDestinationAmountValue(destinationAmountValue);
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
            // null means there's no download active
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
            // There's hard coded assumptions that there will be at least one contact
            // In fact this demo has no way of adding contacts so it's not completely
            // unreasonable.

            for (AccountID addy : contactsAddresses) {
                String address = addy.toString();
                pushContact(address.substring(0, 16) + "...", address);
            }

            pushContact("Niq", "rP1coskQzayaQ9geMdJgAV5f3tNZcHghzH");
            for (int i = 0; i < rawContacts.length(); i++) {
                JSONObject contact = rawContacts.getJSONObject(i);
                pushContact(contact.getString("name"), contact.getString("address"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Thread: ui thread
     */
    private void pushContact(String niq, String address) {
        // parallel arrays for key/value
        contactsAdapter.add(niq);
        contactsAddresses.add(AccountID.fromString(address));
    }

    /**
     * Thread: ui thread
     */
    private void unshiftContact(String niq, String address) {
        // parallel arrays for key/value
        contactsAdapter.insert(niq, 0);
        contactsAddresses.add(0, AccountID.fromString(address));
    }

}
