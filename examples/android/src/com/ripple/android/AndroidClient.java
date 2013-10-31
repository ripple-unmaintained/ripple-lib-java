package com.ripple.android;

import android.os.Handler;
import android.os.HandlerThread;
import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import org.json.JSONObject;

import static com.ripple.android.Logger.LOG;

class AndroidClient extends Client {
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

    void runPrioritized(Runnable runnable) {
        handler.postAtFrontOfQueue(
                runnable
        );
    }

    public void run(Runnable runnable) {
        handler.post(runnable);
    }
}
