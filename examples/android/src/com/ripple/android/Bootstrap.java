package com.ripple.android;

import android.app.Application;

public class Bootstrap {
    static public AndroidClient client;
    static public void bootstrap(Application application) {
        // This is not tied to activity lifecycles now
        // We can't do this Application.onCreate cause of Dexedrine (that's what this bootstrap is for ;)
        client = new AndroidClient();
        client.connect("wss://ct.ripple.com");
    }
}
