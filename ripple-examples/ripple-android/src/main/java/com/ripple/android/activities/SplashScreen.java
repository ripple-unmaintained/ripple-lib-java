
package com.ripple.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.ripple.android.R;

public class SplashScreen extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent intent;
                // intent = new Intent();
                // intent.setClass(SplashScreen.this, PayOneDrop.class);
                // startActivity(intent);
                // finish();
                LoginActivity.launch(SplashScreen.this);
            }
        }, 2000);
    }
}
