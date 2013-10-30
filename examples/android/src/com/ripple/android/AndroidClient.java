package com.ripple.android;

import android.os.Handler;
import com.ripple.client.Client;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import org.json.JSONObject;

import static com.ripple.android.Logger.LOG;

class AndroidClient extends Client {
    Handler handler;

    public AndroidClient(Handler handler) {
        super(new JavaWebSocketTransportImpl());
        this.handler = handler;
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
}
