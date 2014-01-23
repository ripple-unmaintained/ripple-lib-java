
package com.ripple.android.activities;

import android.app.Activity;
import android.app.Service;
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
import com.ripple.client.payments.Alternative;
import com.ripple.client.payments.Alternatives;
import com.ripple.client.payments.PaymentFlow;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionResult;
import com.ripple.core.coretypes.AccountID;
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

public class PaymentAlternatives extends Activity {
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
//    Button payOneDrop;

    Spinner currencySpinner;
    EditText destinationAmountInput;
    BigDecimal destinationAmount;
    Currency destinationCurrency;

    Spinner contacts;
    ArrayAdapter<String> contactsAdapter;
    ArrayList<AccountID> contactsAddresses = new ArrayList<AccountID>(); // parallel
    AccountID destination;

    BlobVault blobVault = new BlobVault("https://blobvault.payward.com/");
    DownloadBlobTask blobDownloadTask;
    String masterSeed;


//    Alternatives alternatives;
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

    /**
     * Thread: ui thread
     */
    private void showAlternatives(Alternatives alternatives) {
        alternativesGroup.setVisibility(View.VISIBLE);
        alternativesGroup.removeAllViews();

        if (alternatives.size() == 0) {
            threadSafeSetStatus("No payment paths found! (yet)");
        } else {
            threadSafeSetStatus(String.format("Found %d alternatives", alternatives.size()));
        }

        for (final Alternative alternative : alternatives) {
            Button button = new Button(this);
            button.setText(alternative.sourceAmount.toText());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideKeyBoard();
                    client.run(new Runnable() {
                        @Override
                        public void run() {
                            ManagedTxn payment = flow.createPayment(alternative, new BigDecimal("1.01"));
                            setTransactionStatusHandlers(account, payment);
                            account.transactionManager().queue(payment);
                            threadSafeSetStatus("Transaction queued " + awaitingTransactionsParenthetical(account));
                        }
                    });
                }
            });
            alternativesGroup.addView(button);
        }
    }

    /**
     * Thread: ui thread
     */
    private void tearDownAlternatives(Alternatives alternatives) {
        alternativesGroup.setVisibility(View.GONE);
        alternativesGroup.removeAllViews();
        threadSafeSetStatus("Path find canceled");
    }


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

    /**
     * Thread: ui thread
     */
    private void setupClient() {
        client = ((RippleApplication) getApplication()).getClient();
        account = null;
    }

    public void setDestinationAmount() {
        try {
            destinationAmount = new BigDecimal(destinationAmountInput.getText().toString());
        } catch (Exception e) {
            destinationAmount = null;
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
//        payOneDrop = (Button) findViewById(R.id.pay_one_drop);

        contacts = (Spinner) findViewById(R.id.contacts);
        contactsAdapter = new ArrayAdapter<String>(this, R.layout.contacts_text_view);
        contacts.setAdapter(contactsAdapter);

        loginForm = (LinearLayout) findViewById(R.id.loginForm);
        paymentForm = (RelativeLayout) findViewById(R.id.payment_form);

        currencySpinner = (Spinner) findViewById(R.id.currencies);
        destinationAmountInput = (EditText) findViewById(R.id.amountInput);


//        destinationAmountInputClicked = false;
//        destinationAmountInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    destinationAmountInputClicked = false;
//                }
//            }
//        });
//        destinationAmountInput.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                client.run(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!destinationAmountInputClicked) {
//                            flow.makePathFindRequestIfCan();
//                            destinationAmountInputClicked = true;
//                        }
//                    }
//                });
//            }
//        });
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

//        currencySpinner.onSele


        // TODO, perhaps we should use a broadcast receiver?
        client.on(Client.OnMessage.class, new Client.OnMessage() {
            @Override
            public void called(JSONObject jsonObject) {
                logMessage(jsonObject, false);
            }
        });
        client.on(Client.OnSendMessage.class, new Client.OnSendMessage() {
            @Override
            public void called(JSONObject jsonObject) {
                logMessage(jsonObject, true);
            }
        });

//        payOneDrop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!account.getAccountRoot().primed()) {
//                    threadSafeSetStatus("Awaiting account_info");
//                } else {
//                    payOneDrop(account);
//                }
//            }
//        });

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
                    hideKeyBoard();
                } else {
                    threadSafeSetStatus("Waiting for blob to be retrieved!");
                }
            }
        });
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
        client.run(setFlowCurrency);
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

//    /**
//     * Thread: ui thread
//     */
//    private void payOneDrop(final Account account) {
//        final AccountID destination = selectedContact();
//
//        client.run(new Runnable() {
//            @Override
//            public void run() {
//                makePayment(account, destination, "1");
//            }
//        });
//    }

    /**
     * Thread: ui thread
     */
    private AccountID selectedContact() {
        return contactsAddresses.get(contacts.getSelectedItemPosition());
    }

    private void setTransactionStatusHandlers(final Account account, ManagedTxn tx) {
        tx.publisher().once(ManagedTxn.OnSubmitSuccess.class, new ManagedTxn.OnSubmitSuccess() {
            @Override
            public void called(Response response) {
                threadSafeSetStatus("Transaction submitted "
                        + awaitingTransactionsParenthetical(account));
            }
        });
        tx.publisher().once(ManagedTxn.OnSubmitError.class, new ManagedTxn.OnSubmitError() {
            @Override
            public void called(Response response) {
                threadSafeSetStatus("Transaction submission failed (" + response.engineResult() + ")"
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
                        flow = new PaymentFlow(client);
                        flow.setSource(account.id());

                        flow.on(PaymentFlow.OnAlternatives.class, onAlternatives);
                        flow.on(PaymentFlow.OnAlternativesStale.class, onAlternativesStale);
                        flow.on(PaymentFlow.OnPathFind.class, new PaymentFlow.OnPathFind() {
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
        client.run(setFlowDestination);
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

    Runnable setFlowCurrency = new Runnable() {
        @Override
        public void run() {
            flow.setDestinationAmountCurrency(destinationCurrency);
        }
    };
    Runnable setFlowAmount = new Runnable() {
        @Override
        public void run() {
            flow.setDestinationAmountValue(destinationAmount);
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
