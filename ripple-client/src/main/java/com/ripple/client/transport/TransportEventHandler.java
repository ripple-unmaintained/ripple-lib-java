package com.ripple.client.transport;

import org.json.JSONObject;

public interface TransportEventHandler {
    void onMessage(JSONObject msg);
    void onConnecting(int attempt);
    void onDisconnected(boolean willReconnect);
    void onError(Exception error);
    void onConnected();

    static public TransportEventHandler Dummy = new TransportEventHandler() {
        @Override
        public void onMessage(JSONObject msg) {
        }

        @Override
        public void onConnecting(int attempt) {
        }

        @Override
        public void onDisconnected(boolean willReconnect) {
        }

        @Override
        public void onError(Exception error) {
        }

        @Override
        public void onConnected() {
        }
    };
}
