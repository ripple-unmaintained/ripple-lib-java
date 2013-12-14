
package com.ripple.android.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ripple.android.JSON;
import com.ripple.android.R;
import com.ripple.android.client.AndroidClient;
import com.ripple.android.profile.User;
import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.subscriptions.AccountRoot;
import com.ripple.service.RippleService;

public class AccountActivity extends Activity {
    private RippleService mRippleService = new RippleService();

    private TextView mUserNameTextView;

    private TextView mBalanceTextView;

    private User mUser;

    public AccountRetrieveTask mAccountRetrieveTask;

    private AndroidClient rippleClient = mRippleService.getClient();

    public final static String CURRENT_USER = "currentUser";

    private static final String TAG = AccountActivity.class.getName();

    public User getCurrentUser() {
        return mUser;
    }

    public static void launch(Context c, User user) {
        Intent intent = new Intent(c, AccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(CURRENT_USER, user);
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);
        mUserNameTextView = (TextView) findViewById(R.id.account_username);
        mBalanceTextView = (TextView) findViewById(R.id.account_balance);
        mUser = (User) getIntent().getSerializableExtra(CURRENT_USER);
        mUserNameTextView.setText(mUser.getUserName());

        if (mAccountRetrieveTask == null) {
            mAccountRetrieveTask = new AccountRetrieveTask();
            mAccountRetrieveTask.execute(mUser.getMasterSeed());
            threadSafeSetStatus("Retrieving blob!");
        } else {
            threadSafeSetStatus("Waiting for blob to be retrieved!");
        }
    }

    private void registerOnMessage() {
        rippleClient.on(Client.OnMessage.class, new Client.OnMessage() {
            @Override
            public void called(JSONObject jsonObject) {
                final String pretty = JSON.prettyJSON(jsonObject);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        threadSafeSetStatus("pretty!-" + pretty);
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        mAccountRetrieveTask = null;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    Runnable getAccountRunnable = new Runnable() {
        @Override
        public void run() {
            mRippleService.getClient().accountFromSeed(mUser.getMasterSeed()).getAccountRoot()
                    .once(AccountRoot.OnUpdate.class, new AccountRoot.OnUpdate() {
                        @Override
                        public void called(AccountRoot accountRoot) {
                            updateUserAccount(accountRoot);
                        }
                    });
        }
    };

    private class AccountRetrieveTask extends AsyncTask<String, String, Account> {
        /**
         * Thread: ui thread
         */
        @Override
        protected void onPostExecute(Account account) {
            mAccountRetrieveTask = null;
            if (account == null) {
                showMessage("Failed to retrieve blob!");
                return;
            }
            rippleClient.runPrioritized(getAccountRunnable);

        }

        /**
         * Thread: ui thread
         */
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Account doInBackground(String... masterSeed) {
            try {
                return mRippleService.getAccountFromSeed(masterSeed[0]);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void threadSafeSetStatus(final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserAccount(final AccountRoot accountRoot) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (null != accountRoot && null != accountRoot.getBalance()) {
                    mUser.setBalance(accountRoot.getBalance().value());
                    mBalanceTextView.setText(mUser.getBalance().toString());
                }
            }
        });
    }

}
