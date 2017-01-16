package com.ripple.client.transport.impl;

import com.ripple.client.transport.TransportEventHandler;
import com.ripple.client.transport.WebSocketTransport;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URI;

class WS extends WebSocketClient {

    WeakReference<TransportEventHandler> h;

    public WS(URI serverURI) {
        super(serverURI, new Draft_17());
    }

    public void muteEventHandler() {
        h.clear();
    }

    public void setEventHandler(TransportEventHandler eventHandler) {
        h = new WeakReference<TransportEventHandler>(eventHandler);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        TransportEventHandler handler = h.get();
        if (handler != null) {
            handler.onConnected();
        }
    }

    @Override
    public void onMessage(String message) {
        TransportEventHandler handler = h.get();
        if (handler != null) {
            handler.onMessage(new JSONObject(message));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        TransportEventHandler handler = h.get();
        if (handler != null) {
            handler.onDisconnected(false);
        }
    }

    @Override
    public void onError(Exception ex) {
        TransportEventHandler handler = h.get();
        if (handler != null) {
            handler.onError(ex);
        }
    }
}

public class JavaWebSocketTransportImpl implements WebSocketTransport {

    WeakReference<TransportEventHandler> handler;
    WS client = null;

    @Override
    public void setHandler(TransportEventHandler events) {
        handler = new WeakReference<TransportEventHandler>(events);
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
        TransportEventHandler curHandler = handler.get();
        if (curHandler == null) {
            throw new RuntimeException("must call setEventHandler() before connect(...)");
        }
        disconnect();
        client = new WS(uri);

        client.setEventHandler(curHandler);
        curHandler.onConnecting(1);
        client.connect();
    }

    @Override
    public void disconnect() {
        if (client != null) {
            TransportEventHandler handler = this.handler.get();
            // Before we mute the handler, call disconnect
            if (handler != null) {
                handler.onDisconnected(false);
            }
            client.muteEventHandler();
            client.close();
            client = null;
        }
    }
}
