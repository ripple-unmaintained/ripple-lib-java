package com.ripple.client.requests;

import com.ripple.client.Client;
import com.ripple.client.responses.Response;
import com.ripple.client.enums.Command;
import com.ripple.client.pubsub.Publisher;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

// We can just shift to using delegation
public class Request extends Publisher<Request.events> {
    public void json(JSONObject jsonObject) {
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            String next = (String) keys.next();
            json(next, jsonObject.opt(next));
        }
    }

    // Base events class and aliases
    public static abstract class events<T>  extends Publisher.Callback<T> {}
    public abstract static class OnSuccess  extends events<Response> {}
    public abstract static class OnError    extends events<Response> {}
    public abstract static class OnResponse extends events<Response> {}

    Client client;
    public Command           cmd;
    public Response     response;
    private JSONObject      json;
    public int                id;

    public Request(Command command, int assignedId, Client client) {
        this.client = client;
        cmd         = command;
        id          = assignedId;
        json        = new JSONObject();

        json("command", cmd.toString());
        json("id",      assignedId);
    }

    public JSONObject json() {
        return json;
    }

    public void json(String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void request() {
        Client.OnConnected onConnected = new Client.OnConnected() {
            @Override
            public void called(Client client) {
                client.requests.put(id, Request.this);
                client.sendMessage(toJSON());
            }
        };

        if (client.connected) {
            onConnected.called(client);
        }  else {
            client.once(Client.OnConnected.class, onConnected);
        }
    }

    private JSONObject toJSON() {
        return json();
    }

    public static class ResponseError {

    }

    public void handleResponse(JSONObject msg) {
        try {
            response = new Response(this, msg);
        } catch (Exception e) {
            try {
                System.out.println(msg.toString(4));
            } catch (JSONException ignored) {
            }
            throw new RuntimeException(e);
        }

        if (response.succeeded) {
            emit(OnSuccess.class, response);
        } else {
            emit(OnError.class, response);
        }

        emit(OnResponse.class, response);
    }
}
