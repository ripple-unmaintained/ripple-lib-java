package com.ripple.java8;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.Payment;

public class SignTransaction {
    public static void main(String[] args) {
        /**
         * See also {@link com.ripple.crypto.ecdsa.Seed}
         * See also {@link com.ripple.crypto.ecdsa.IKeyPair}
         * See also {@link com.ripple.crypto.ecdsa.KeyPair}
         */
        String secret = "ssStiMFzkGefDoTqgk9w9WpYkTepQ";
        // Make a new Payment transaction

        /**
         * We can make these from JSON.
         *
         * See also {@link com.ripple.core.coretypes.STObject#fromJSON}
         * See also {@link com.ripple.core.coretypes.STObject#formatted}
         */
        Payment payment = new Payment();

        // Put `as` AccountID field Account, `Object` o
        payment.as(AccountID.Account,     "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm");
        payment.as(AccountID.Destination, "rPMh7Pi9ct699iZUTWaytJUoHcJ7cgyziK");
        payment.as(Amount.Amount,         "1000000000");
        payment.as(Amount.Fee,            "10000");
        payment.as(UInt32.Sequence,       10);

        SignedTransaction signed = SignedTransaction.sign(payment, secret);

        // `txn` is a shallow copy
        if (signed.txn == payment) throw new AssertionError();

        // The tx_json with SigningPubKey and TxnSignature populated
        print(signed.txn.prettyJSON());
        // The signingHash is what's signed
        print(signed.signingHash);
        // The transaction id
        print(signed.hash);
        // The blob to submit to rippled
        print(signed.tx_blob);
    }

    private static void print(Object fmt, Object... args) {
        System.out.println(String.format(String.valueOf(fmt), args));
    }
}
