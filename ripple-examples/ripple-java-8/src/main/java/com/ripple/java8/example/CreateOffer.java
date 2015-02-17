package com.ripple.java8.example;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.txns.OfferCreate;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.java8.utils.Func;

import static com.ripple.java8.utils.Print.print;
import static com.ripple.java8.utils.Print.printErr;

/**
 * This example creates an offer to sell an account's
 * own issue.
 */
public class CreateOffer {
    public static void main(String[] args) {
        // We need a valid seed
        if (args.length != 1 || Func.itThrows(Seed::fromBase58, args[0])) {
            printErr("Must pass valid base58 encoded " +
                    "seed/secret as first arg :)");
            System.exit(1);
        } else {
            new Client(new JavaWebSocketTransportImpl())
                    .connect("wss://s-east.ripple.com", (c) ->
                        new CreateOffer(c, args[0]));
        }
    }

    public CreateOffer (Client client, String seed) {
        Account account = client.accountFromSeed(seed);
        TransactionManager tm = account.transactionManager();

        OfferCreate offer = new OfferCreate();

        offer.as(Amount.TakerPays, "1000000")
             .as(Amount.TakerGets, "1/USD/" + account.id());

        tm.queue(tm.manage(offer)
            .onValidated(this::onValidated)
                .onError(this::onError));
    }

    private void onValidated(ManagedTxn managed) {
        TransactionResult tr = managed.result;
        print("Result:\n{0}", tr.toJSON().toString(2));
        print("Transaction result was: {0}", tr.engineResult);
        System.exit(0);
    }

    private void onError(ManagedTxn managed) {
        printErr("Transaction failed!");
        managed.submissions.forEach(sub ->
                printErr("{0}", sub.hash) );
        System.exit(1);
    }
}
