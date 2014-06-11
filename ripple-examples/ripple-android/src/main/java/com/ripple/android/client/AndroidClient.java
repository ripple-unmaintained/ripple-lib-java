package com.ripple.android.client;

import com.ripple.android.logging.AndroidHandler;
import com.ripple.client.Client;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class AndroidClient extends Client {
    static {
        Logger logger = Client.logger;
        AndroidHandler handler = new AndroidHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }
    public AndroidClient() {
        super(new JavaWebSocketTransportImpl());
    }
}
