package com.ripple.cli;

import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.types.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;

public class CheckPrice {
    public static final AccountID BITSTAMP = AccountID.fromAddress("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");
    public static final Issue BITSTAMP_USD = BITSTAMP.issue("USD");
    public static final Issue BITSTAMP_BTC = BITSTAMP.issue("BTC");
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
        Amount takerPays     = offer.get(Amount.TakerPays);
        Amount takerGets     = offer.get(Amount.TakerGets);

        // The quality is a high precision BigDecimal
        BigDecimal payForOne = takerPays.computeQuality(takerGets);

        // The real native unit is a drop, one million of which are an XRP
        // We want `one` unit at XRP scale (1e6 drops), or if it's an IOU,
        // just `one`
        Amount getsOne = takerGets.oneAtXRPScale();
        Amount paysOne = takerPays.oneAtXRPScale();

        printSeparatorBanner();
        // Multiply and divide will round/scale to the required bounds
        print("%40s == %s\n", paysOne.multiply(payForOne).toText(), getsOne.toText());
        print("%40s == %s\n", getsOne.divide(payForOne).toText(),  paysOne.toText());
    }

    private static void print(String fmt, Object... args) {
        System.out.printf(fmt, args);
    }

    private static void printSeparatorBanner() {
        for (int i = 0; i < 80; i++) System.out.print("-");
        System.out.println();
    }
}
