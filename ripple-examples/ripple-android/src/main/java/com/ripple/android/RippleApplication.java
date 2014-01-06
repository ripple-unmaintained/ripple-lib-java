
package com.ripple.android;

import com.ripple.android.client.AndroidClient;

import android.app.Application;

public class RippleApplication extends Application {
    public AndroidClient client;

    public AndroidClient getClient() {
        return client;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = new AndroidClient();
        client.connect("wss://s1.ripple.com");
    }

}
