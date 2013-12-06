package com.ripple.cli;

import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.types.Issue;
import org.json.JSONException;
import org.json.JSONObject;

public class CheckPrice {
    public static final Issue BITSTAMP_USD = Issue.fromString("USD/rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");

    public static void main(String[] args) throws JSONException {
        ClientLogger.quiet = false;
        Client client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");

        Request request = client.requestBookOffers(Issue.XRP, BITSTAMP_USD);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.succeeded) {
                    System.out.println(prettyJSON(response.result));
                } else {
                    System.out.println("There was an error: " + response.message);
                }
            }
        });
        request.request();
    }

    private static String prettyJSON(JSONObject object)  {
        try {
            return object.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
