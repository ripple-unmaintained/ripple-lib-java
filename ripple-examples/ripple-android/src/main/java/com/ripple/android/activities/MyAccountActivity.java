
package com.ripple.android.activities;

import com.ripple.android.profile.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class MyAccountActivity extends Activity {

    public User getCurrentUser() {
        return new User();
    }

    public static void launch(Context c, User user) {
        Intent intent = new Intent(c, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("currentUser", user);
        c.startActivity(intent);
    }

}
