package com.ripple.core.types.known.tx.signed;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.MultiSink;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;

import java.util.Arrays;

public class SignedTransaction {
    private SignedTransaction(Transaction of) {
        // TODO: is this just over kill ?
        txn = (Transaction) STObject.translate.fromBytes(of.toBytes());
    }

    // This will eventually be private
    @Deprecated
    public SignedTransaction() {}

    public Transaction txn;
    public Hash256 hash;

    public byte[] signingData;
    public byte[] previousSigningData;
    public String tx_blob;

    public void sign(String base58Secret) {
        sign(Seed.fromBase58(base58Secret).keyPair());
    }

    public static SignedTransaction fromTx(Transaction tx) {
        return new SignedTransaction(tx);
    }

    public void sign(IKeyPair keyPair) {
        prepare(keyPair, null, null, null);
    }

    public void prepare(IKeyPair keyPair,
                        Amount fee,
                        UInt32 Sequence,
                        UInt32 lastLedgerSequence) {

        Blob pubKey = new Blob(keyPair.canonicalPubBytes());

        // This won't always be specified
        if (lastLedgerSequence != null) {
            txn.put(UInt32.LastLedgerSequence, lastLedgerSequence);
        }
        if (Sequence != null) {
            txn.put(UInt32.Sequence, Sequence);
        }
        if (fee != null) {
            txn.put(Amount.Fee, fee);
        }

        txn.signingPubKey(pubKey);

        if (Transaction.CANONICAL_FLAG_DEPLOYED) {
            txn.setCanonicalSignatureFlag();
        }

        txn.checkFormat();
        signingData = txn.signingData();
        if (previousSigningData != null && Arrays.equals(signingData, previousSigningData)) {
            return;
        }
        try {
            txn.txnSignature(new Blob(keyPair.signMessage(signingData)));

            BytesList blob = new BytesList();
            HalfSha512 id = HalfSha512.prefixed256(HashPrefix.transactionID);

            txn.toBytesSink(new MultiSink(blob, id));
            tx_blob = blob.bytesHex();
            hash = id.finish();
        } catch (Exception e) {
            // electric paranoia
            previousSigningData = null;
            throw new RuntimeException(e);
        } /*else {*/
        previousSigningData = signingData;
        // }
    }

    public TransactionType transactionType() {
        return txn.transactionType();
    }
}
