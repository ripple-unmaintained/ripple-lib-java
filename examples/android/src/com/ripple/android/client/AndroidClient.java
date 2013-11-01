package com.ripple.android.client;

import android.os.Handler;
import android.os.HandlerThread;
import com.ripple.android.Logger;
import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import org.json.JSONObject;

public class AndroidClient extends Client {
    Handler handler;

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

    /**
     * After calling this method, all subsequent interaction with the api
     * should be called via posting Runnable() run blocks to the HandlerThread
     * Essentially, all ripple-lib-java api interaction should happen on
     * the one thread.
     *
     * @see #onMessage(org.json.JSONObject)
     */
    @Override
    public void connect(final String uri) {
        HandlerThread handlerThread = new HandlerThread("android client thread") {
            @Override
            protected void onLooperPrepared() {
                handler = new Handler(getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidClient.super.connect(uri);
                    }
                });
            }
        };
        handlerThread.start();
    }

    /**
     * This is to ensure we run everything on the one HandlerThread
     */
    @Override
    public void onMessage(final JSONObject msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                AndroidClient.super.onMessage(msg);
            }
        });
    }

    public void runPrioritized(Runnable runnable) {
        handler.postAtFrontOfQueue(
                runnable
        );
    }

    public void run(Runnable runnable) {
        handler.post(runnable);
    }
}
