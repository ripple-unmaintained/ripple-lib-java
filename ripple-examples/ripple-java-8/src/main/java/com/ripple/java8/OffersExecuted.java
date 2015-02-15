package com.ripple.java8;

import com.ripple.client.Client;
import com.ripple.client.subscriptions.SubscriptionManager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.tx.result.TransactionResult;

public class OffersExecuted {
    public static void main(String[] args) {
        new Client(new JavaWebSocketTransportImpl())
            .connect("wss://s-east.ripple.com", OffersExecuted::onceConnected);
    }

    private static void onceConnected(Client c) {
        c.subscriptions.addStream(SubscriptionManager.Stream.transactions);
        c.on(Client.OnValidatedTransaction.class, (tr) -> {
           tr.meta.affectedNodes().forEach((an) -> {
               if (an.ledgerEntryType() == LedgerEntryType.Offer) {
                   if (an.wasPreviousNode()) {
                       Offer asFinal = ((Offer) an.nodeAsFinal());
                       printTrade(tr, ((Offer) an.nodeAsPrevious()), asFinal);
                   }
               }
           });
        });
    }

    private static void printTrade(TransactionResult tr,
                                   Offer before,
                                   Offer after) {
        STObject executed = after.executed(before);
        Amount takerPaid = executed.get(Amount.TakerPays);
        Amount takerGot = executed.get(Amount.TakerGets);

        if (!takerGot.isZero()) {
            put("In tx: %s Offer owner %s was paid: %s, gave: %s ",
                    tr.hash, before.account(), takerPaid, takerGot);
        }
    }

    private static void put(String fmt, Object... args) {
        System.out.println(String.format(fmt, args));
    }
}
