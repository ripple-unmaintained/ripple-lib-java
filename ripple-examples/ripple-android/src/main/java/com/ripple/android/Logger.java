package com.ripple.android;

import android.util.Log;

public class Logger {
    private static final String LOG_TAG = "RippleApplication";

    public static void LOG(String s, Object... args) {
        Log.d(LOG_TAG, String.format(s, args));
    }
}
