
package com.ripple.cli;

import org.json.JSONException;
import org.json.JSONObject;

import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.Request;
import com.ripple.client.Response;
import com.ripple.client.enums.Command;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

public class CheckPrice {
    public static void main(String[] args) throws JSONException {
        ClientLogger.quiet = true;

        Client client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");
        Request request = client.newRequest(Command.book_offers);

        request.json("taker_gets", new JSONObject("{\"currency\" : \"XRP\"}"));
        request.json("taker_pays", new JSONObject(
                "{\"currency\" : \"CNY\", \"issuer\" : \"razqQKzJRdB4UxFPWf5NEpEG3WMkmwgcXA\"}"));

        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.succeeded) {
                    JSONObject result = response.result;
                    System.out.println(result);
                } else {
                    System.out.println("Yeah, nup");
                }
            }
        });
        request.request();
    }
}
