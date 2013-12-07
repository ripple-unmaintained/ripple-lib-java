package com.ripple.cli;

import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.types.Amount;
import com.ripple.core.types.Issue;
import com.ripple.core.types.STArray;
import com.ripple.core.types.STObject;
import org.json.JSONArray;
import org.json.JSONException;

public class CheckPrice {
    public static final Issue BITSTAMP_USD = Issue.fromString("USD/rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");
    public static final Issue XRP = Issue.XRP;

    public static void main(String[] args) throws JSONException {
        ClientLogger.quiet = true;
        Client client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");

        Request request = client.requestBookOffers(BITSTAMP_USD, XRP);
        request.once(Request.OnResponse.class, new Request.OnResponse() {
            @Override
            public void called(Response response) {
                if (response.succeeded) {
                    JSONArray offersJSON = response.result.optJSONArray("offers");
                    STArray offers = STArray.translate.fromJSONArray(offersJSON);
                    for (STObject offer : offers) showOfferInfo(offer);
                } else {
                    System.out.println("There was an error: " + response.message);
                }
            }
        });
        request.request();
    }

    private static void showOfferInfo(STObject offer) {
        Amount takerPays = offer.get(Amount.TakerPays);
        Amount takerGets = offer.get(Amount.TakerGets);
        Amount quality = takerPays.computeQuality(takerGets);

        printSeparatorBanner();

        Amount getsOne = takerGets.oneAtXRPScale();
        Amount paysOne = takerPays.oneAtXRPScale();

        System.out.printf("%60s == %s\n", quality,                 getsOne);
        System.out.printf("%60s == %s\n", getsOne.divide(quality), paysOne);

    }

    private static void printSeparatorBanner() {
        for (int i = 0; i < 120; i++) System.out.print("-");
        System.out.println();
    }
}
