package com.ripple.client.transport;

import org.json.JSONObject;

import java.net.URI;
import java.net.URL;

public interface WebSocketTransport {
    public abstract void setHandler(TransportEventHandler events);
    public abstract void sendMessage(JSONObject msg);
    public abstract void connect(URI url);
    public abstract void disconnect();
}
