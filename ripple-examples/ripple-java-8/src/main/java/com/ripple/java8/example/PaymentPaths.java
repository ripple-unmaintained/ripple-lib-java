package com.ripple.java8.example;

import com.ripple.client.Account;
import com.ripple.client.Client;
import com.ripple.client.payments.PaymentFlow;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.Submission;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Currency;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.java8.utils.Func;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ripple.core.coretypes.AccountID.fromAddress;
import static com.ripple.java8.utils.Print.print;
import static com.ripple.java8.utils.Print.printErr;

/**
 *
 * This example shows how to donate a tiny fraction of a dollar to
 * the BitStamp account `1337`, by finding a payment path from the
 * account derived from a passed in secret to BitStamp, then using
 * the transaction manager to submit a transaction.
 *
 */
public class PaymentPaths {
    public static void main(String[] args) {
        // We need a valid seed
        if (args.length != 1 || Func.itThrows(Seed::fromBase58, args[0])) {
            printErr("Must pass valid base58 encoded " +
                    "seed/secret as first arg :)");
            System.exit(1);
        } else {
            // Gratuitous use of Java 8 features such as method
            // references and lambdas
            new Client(new JavaWebSocketTransportImpl())
               .connect("wss://s-east.ripple.com",
                       Func.bind(PaymentPaths::example, args[0])::accept);
        }
    }

    public static void example(Client client, String secret) {
        Account account = client.accountFromSeed(secret);
        PaymentFlow flow = new PaymentFlow(client);
        TransactionManager tm = account.transactionManager();

        // We could get these from user input
        AccountID destination = fromAddress("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");
        BigDecimal slippageFactor = new BigDecimal("1.001");
        BigDecimal amount = new BigDecimal("0.0000001");
        Currency USD = Currency.fromString("USD");
        // We use this not for its atomic properties, but because
        // it's `effectively final` and can be mutated from inside
        // a lambda below.
        AtomicInteger attempts = new AtomicInteger(1);

        flow.source(account)
            .destination(destination)
            .destinationAmountValue(amount)
            .destinationAmountCurrency(USD)
            .onAlternatives((alts) -> {
                if (alts.size() > 0) {
                    // Create a payment, bind the handlers
                    // No more onAlternatives events will be emitted
                    // after createPayment has been invoked.
                    ManagedTxn payment =
                            flow.createPayment(alts.get(0), slippageFactor)
                                    .onError(PaymentPaths::onError)
                                    .onValidated(PaymentPaths::onValidated);

                    // Set the destination tag
                    payment.txn.as(UInt32.DestinationTag, 1337);
                    // Tell the manager to submit it
                    tm.queue(payment);
                } else {
                    printErr("Message {0} had no payment paths", attempts);

                    if (attempts.incrementAndGet() > 3) {
                        printErr("Aborting!");
                        System.exit(1);
                    }
                }
            });
    }

    private static void onValidated(ManagedTxn managed) {
        print("Transaction was successful!");
        print("Result: ");
        print("{0}", managed.result.toJSON().toString(2));
        printSubmissions(managed);
        System.exit(0);
    }

    private static void onError(ManagedTxn managed) {
        print("Transaction failed!");
        printSubmissions(managed);
        System.exit(1);
    }

    private static void printSubmissions(ManagedTxn managed) {
        ArrayList<Submission> submissions = managed.submissions;
        print("{0} Submission[s]:", submissions.size());
        for (Submission submission : submissions) {
            print("Hash: {0} Fee: {1} Ledgers: {2}-{3}",
                    submission.hash,
                    submission.fee,
                    submission.ledgerSequence,
                    submission.lastLedgerSequence);
        }
    }
}
