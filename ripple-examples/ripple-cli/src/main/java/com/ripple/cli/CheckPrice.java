package com.ripple.cli;

import com.ripple.client.Client;
import com.ripple.client.ClientLogger;
import com.ripple.client.requests.Request;
import com.ripple.client.responses.Response;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.known.sle.Offer;
import com.ripple.core.types.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;

public class CheckPrice {
    public static void main(String[] args) throws JSONException {
        ClientLogger.quiet = true;
        Client client = new Client(new JavaWebSocketTransportImpl());
        client.connect("wss://s1.ripple.com");

        showPairInfo(client, BITSTAMP_USD, XRP);
        showPairInfo(client, BITSTAMP_USD, BITSTAMP_BTC);
        showPairInfo(client, BITSTAMP_AUD, BITSTAMP_BTC);

    }
    public static final AccountID BITSTAMP = AccountID.fromAddress("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");
    public static final Issue BITSTAMP_USD = BITSTAMP.issue("USD");
    public static final Issue BITSTAMP_BTC = BITSTAMP.issue("BTC");
    public static final Issue BITSTAMP_AUD = BITSTAMP.issue("AUD");

    public static final Issue XRP = Issue.XRP;

    /**
     * This class encapsulates requesting order book info for a currency pair,
     * and calculating `ask`, `bid` and `spread`. It makes two `request_books`
     * calls.
     */
    public static class OrderBookPair {
        public static interface BookEvents {
            public void onUpdate(OrderBookPair book);

        }

        private Client client;
        private final BookEvents callback;
        public Issue first, second;
        public STArray asks, bids;
        public Amount ask, bid, spread;

        public OrderBookPair(Client client, Issue first, Issue second, BookEvents callback) {
            this.client = client;
            this.first = first;
            this.second = second;
            this.callback = callback;
        }

        private void calculateStats() {
            Offer firstAsk = (Offer) asks.get(0);
            Offer firstBid = (Offer) bids.get(0);

            BigDecimal askQuality = firstAsk.askQuality();
            BigDecimal bidQuality = firstBid.bidQuality();

            Amount secondOne = firstAsk.paysOne();

            ask    = secondOne.multiply(askQuality);
            bid    = secondOne.multiply(bidQuality);

            spread = ask.subtract(bid).abs();
        }

        private void requestUpdate() {
            for (int i = 0; i < 2; i++) {
                final boolean getAsks = i == 0,
                              getBids = !getAsks;

                Issue getIssue  = getAsks ? first  : second,
                      payIssue  = getAsks ? second : first;

                Request request = client.requestBookOffers(getIssue, payIssue);
                request.once(Request.OnResponse.class, new Request.OnResponse() {
                    @Override
                    public void called(Response response) {
                        if (response.succeeded) {
                            JSONArray offersJSON = response.result.optJSONArray("offers");
                            STArray offers = STArray.translate.fromJSONArray(offersJSON);
                            if (getBids) bids = offers;
                            else         asks = offers;

                            if (retrievedBothBooks()) {
                                if (!isEmpty()) {
                                    calculateStats();
                                }
                                callback.onUpdate(OrderBookPair.this);
                            }
                        } else {
                            System.out.println("There was an error: " + response.message);
                        }
                    }
                });
                request.request();
            }

        }

        public String currencyPair() {
            return String.format("%s/%s", first.currency(), second.currency());
        }

        public boolean retrievedBothBooks() {
            return asks != null && bids != null;
        }
        public boolean isEmpty() {
            return !retrievedBothBooks() || asks.isEmpty() || bids.isEmpty();
        }
    }

    private static void showPairInfo(Client client, Issue first, Issue second) {
        new OrderBookPair(client, first, second, new OrderBookPair.BookEvents() {
            @Override
            public void onUpdate(OrderBookPair book) {
                printSeparatorBanner();

                if (!book.isEmpty()) {
                    System.out.printf("%s Ask: %s, Bid: %s, Spread %s%n",
                            book.currencyPair(),
                            book.ask.toText(),
                            book.bid.toText(),
                            book.spread.toText());
                System.out.printf("%nAsk offers%n");
                for (STObject offer : book.asks) {
                    showOfferInfo((Offer) offer);
                }

                } else {
                    System.out.printf("%s No info!%n", book.currencyPair());
                }
            }
        }).requestUpdate();
    }

    private static void showOfferInfo(Offer offer) {
        BigDecimal payForOne = offer.askQuality();
        Amount getsOne = offer.getsOne();
        Amount paysOne = offer.paysOne();

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