package com.ripple.android.client;

import com.ripple.android.Logger;
import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

public class AndroidClient extends Client {
    static {
        ClientLogger.logger = new com.ripple.client.Logger() {
            @Override
            public void log(String fmt, Object... args) {
                Logger.LOG(fmt, args);
            }
        };
    }
    public AndroidClient() {
        super(new JavaWebSocketTransportImpl());
    }
}
