package com.ripple.client.requests;

import com.ripple.client.Client;
import com.ripple.client.enums.Command;
import com.ripple.client.pubsub.Publisher;
import com.ripple.client.responses.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.logging.Logger;

// We can just shift to using delegation
public class Request extends Publisher<Request.events> {
    // com.ripple.client.requests.Request // ??
    public static final Logger logger = Logger.getLogger(Request.class.getName());
    public static final long TIME_OUT = 60000;

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
    public static interface events<T>  extends Publisher.Callback<T> {}
    public static interface OnSuccess  extends events<Response> {}
    public static interface OnError    extends events<Response> {}
    public static interface OnResponse extends events<Response> {}
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
    public long         sendTime;

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
        json.put(key, value);
    }

    public void request() {
        client.nowOrWhenConnected(new Client.OnConnected() {
            @Override
            public void called(final Client client_) {
                client.sendRequest(Request.this);
            }
        });
    }

    public  void bumpSendTime() {
        sendTime = System.currentTimeMillis();
    }

    public JSONObject toJSON() {
        return json();
    }

    public JSONObject jsonRepr() {
        JSONObject repr = new JSONObject();
        if (response != null) {
            repr.put("response", response.message);
        }
        // Copy this
        repr.put("request", new JSONObject(json.toString()));
        return repr;
    }

    public void handleResponse(JSONObject msg) {
        response = new Response(this, msg);

        if (response.succeeded) {
            emit(OnSuccess.class, response);
        } else {
            emit(OnError.class, response);
        }

        emit(OnResponse.class, response);
    }

}
