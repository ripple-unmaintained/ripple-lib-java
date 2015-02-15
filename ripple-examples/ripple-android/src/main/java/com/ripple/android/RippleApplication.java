
package com.ripple.android;

import android.app.Application;
import com.ripple.android.client.AndroidClient;

public class RippleApplication extends Application {
    public AndroidClient client;
    public AndroidClient getClient() {
        return client;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = new AndroidClient();
        client.connect("wss://s-east.ripple.com");
    }

}
