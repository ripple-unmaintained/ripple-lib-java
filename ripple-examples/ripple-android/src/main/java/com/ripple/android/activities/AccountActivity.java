
package com.ripple.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.ripple.android.R;
import com.ripple.android.profile.User;
import com.ripple.service.RippleService;

public class AccountActivity extends Activity {
    private RippleService mRippleService = new RippleService();

    private TextView mUserNameTextView;

    private TextView mBalanceTextView;

    private User mUser;

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
        mBalanceTextView.setText(String.valueOf(mUser.getBalance()));
    }
}
