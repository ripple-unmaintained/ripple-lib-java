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

    public static class OrderBook {
        public static interface BookEvents {
            public void onUpdate(OrderBook book);
        }

        private final BookEvents callback;
        private Client client;
        public Issue first,
                     second;

        public STArray asks = null,
                       bids = null;

        public Amount ask = null,
                      bid = null,
                      spread = null;

        public OrderBook(Client client, Issue first, Issue second, BookEvents callback) {
            this.client = client;
            this.first = first;
            this.second = second;
            this.callback = callback;
        }

        public boolean haveBothBooks() {
            return asks != null && bids != null;
        }

        private void requestUpdate() {
            for (int i = 0; i < 2; i++) {
                final boolean getBids = i == 1;

                Issue firstIssue  = i == 0 ? first  : second,
                      secondIssue = i == 0 ? second : first;

                Request request = client.requestBookOffers(secondIssue, firstIssue);
                request.once(Request.OnResponse.class, new Request.OnResponse() {
                    @Override
                    public void called(Response response) {
                        if (response.succeeded) {
                            JSONArray offersJSON = response.result.optJSONArray("offers");
                            STArray offers = STArray.translate.fromJSONArray(offersJSON);
                            if (getBids) bids = offers;
                            else         asks = offers;

                            if (haveBothBooks()) {
                                calculateStats();
                                callback.onUpdate(OrderBook.this);
                            }
                        } else {
                            System.out.println("There was an error: " + response.message);
                        }
                    }
                });
                request.request();
            }

        }

        private void calculateStats() {
            STObject firstAsk = asks.get(0);
            STObject firstBid = bids.get(0);

            BigDecimal askQuality = firstAsk.get(Amount.TakerPays)
                                            .computeQuality(
                                                    firstAsk.get(Amount.TakerGets));
            BigDecimal bidQuality = firstBid.get(Amount.TakerGets)
                                            .computeQuality(
                                                    firstBid.get(Amount.TakerPays));

            Amount secondOne = firstAsk.get(Amount.TakerPays).oneAtXRPScale();

            ask = secondOne.multiply(askQuality);
            bid = secondOne.multiply(bidQuality);
            spread = ask.subtract(bid).abs();
        }
    }

    public static void main(String[] args) throws JSONException {
        ClientLogger.quiet = true;
        Client client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");

        OrderBook book = new OrderBook(client, BITSTAMP_USD, XRP, new OrderBook.BookEvents() {
            @Override
            public void onUpdate(OrderBook book) {
                System.out.printf("First: %s, Second: %s %n", book.first, book.second);
                System.out.printf("Ask: %s, Bid: %s, Spread %s%n",  book.ask.toText(),
                                                                  book.bid.toText(),
                                                                  book.spread.toText());

                System.out.printf("%nAsk offers%n");
                for (STObject offer : book.asks) showOfferInfo(offer);
            }
        });

        book.requestUpdate();
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
