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

    public static interface Builder<T> {
        void beforeRequest(Request request);
        T buildTypedResponse(Response response);
    }

    // Base events class and aliases
    public static interface events<T>   extends Publisher.Callback<T> {}
    public static interface OnSuccess   extends events<Response> {}
    public static interface OnError     extends events<Response> {}
    public static interface OnResponse  extends events<Response> {}
    public static interface OnTimeout   extends events<Response> {}

    public static abstract class Manager<T> {
        abstract public void cb(Response response, T t) throws JSONException;
        public boolean retryOnUnsuccessful(Response r) {
            return false;
        }
        public void beforeRequest(Request r) {}
    }

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
            public void called(final Client client) {
                client.requests.put(id, Request.this);
                client.sendMessage(toJSON());
                // TODO: use an LRU map or something
                client.schedule(60000 * 10, new Runnable() {
                    @Override
                    public void run() {
                        client.requests.remove(id);
                    }
                });
                client.schedule(60000, new Runnable() {
                    @Override
                    public void run() {
                        if (response == null) {
                            emit(OnTimeout.class, null);
                        }
                    }
                });

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
