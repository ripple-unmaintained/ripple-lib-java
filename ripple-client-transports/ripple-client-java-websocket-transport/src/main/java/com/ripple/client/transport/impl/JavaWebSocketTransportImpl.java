package com.ripple.client.transport.impl;

import com.ripple.client.transport.TransportEventHandler;
import com.ripple.client.transport.WebSocketTransport;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

class WS extends WebSocketClient {
    TransportEventHandler h;

    public WS(URI serverURI) {
        super(serverURI, new Draft_17());
    }

    public void muteEventHandler() {
        h = TransportEventHandler.Dummy;
    }

    public void setEventHandler(TransportEventHandler eventHandler) {
        h = eventHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        h.onConnected();
    }

    @Override
    public void onMessage(String message) {
        try {
            h.onMessage(new JSONObject(message));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        h.onDisconnected(false);
    }

    @Override
    public void onError(Exception ex) {
        h.onError(ex);
    }
}
public class JavaWebSocketTransportImpl implements WebSocketTransport {
    TransportEventHandler handler;
    WS client = null;

    @Override
    public void setHandler(TransportEventHandler events) {
        handler = events;
        if (client != null) {
            client.setEventHandler(events);
        }
    }

    @Override
    public void sendMessage(JSONObject msg) {
        client.send(msg.toString());
    }

    @Override
    public void connect(URI uri) {
        if (handler == null) {
            throw new RuntimeException("must call setEventHandler() before connect(...)");
        }
        disconnect();
        client = new WS(uri);
        client.setEventHandler(handler);
        handler.onConnecting(1);
        client.connect();
    }

    @Override
    public void disconnect() {
        if (client != null) {
            client.muteEventHandler();
            client = null;
        }
    }
}
