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
                Logger.LOG("onLooperPrepared! %s", Thread.currentThread());
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

    @Override
    public void sendMessage(JSONObject msg) {
        LOG("sending: ", JSON.prettyJSON(msg));
        super.sendMessage(msg);
    }

    /**
     * This is to ensure we run everything on the ui thread (as per activity lifecycle
     * handlers onCreate and OnClickListener handlers)
     */
    @Override
    public void onMessage(final JSONObject msg) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                LOG("received: ", JSON.prettyJSON(msg));
                AndroidClient.super.onMessage(msg);
            }
        });
    }

    void runImmediately(Runnable getAccount) {
        handler.postAtFrontOfQueue(
                getAccount
        );
    }
}
