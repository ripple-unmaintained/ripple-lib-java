
package com.ripple.android.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.ripple.android.R;
import com.ripple.client.blobvault.BlobVault;

public class LoginActivity extends Activity {
    protected static final String TAG = LoginActivity.class.getName();

    private BlobVault blobVault = new BlobVault("https://blobvault.payward.com/");

    private Button mLoginBtn;

    private EditText mUsername;

    private EditText mPassword;

    public LoginTask mLoginTask;

    public static void launch(Context c) {
        Intent intent = new Intent(c, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.login_activity);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mUsername = (EditText) findViewById(R.id.name);
        mPassword = (EditText) findViewById(R.id.password);
        mLoginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                checkLoginFields();
            }

            private void checkLoginFields() {
                if (loginFieldsValid()) {
                    showMessage("Login field emptys");
                } else {
                    if (mLoginTask == null) {
                        mLoginTask = new LoginTask();
                    }
                    mLoginTask.execute(mUsername.getText().toString(), mPassword.getText()
                            .toString());
                }
            }

            private boolean loginFieldsValid() {
                return Strings.isNullOrEmpty(mUsername.getText().toString())
                        || Strings.isNullOrEmpty(mPassword.getText().toString());
            }
        });
        super.onCreate(savedInstanceState);
    }

    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Thread: any
     */
    private class LoginTask extends AsyncTask<String, String, JSONObject> {
        /**
         * Thread: ui thread
         */
        @Override
        protected void onPostExecute(final JSONObject blob) {
            mLoginTask = null;
            if (blob == null) {
                showMessage("Failed to retrieve blob!");
                return;
            }
            showMessage("Retrieved blob!");
//            MyAccountActivity.launch(this,blob);
        }

        /**
         * Thread: ui thread
         */
        @Override
        protected void onPreExecute() {
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
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

}
