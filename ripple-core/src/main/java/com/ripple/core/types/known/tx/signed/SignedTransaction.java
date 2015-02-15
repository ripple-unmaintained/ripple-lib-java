package com.ripple.core.types.known.tx.signed;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.VariableLength;
import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.MultiSink;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.crypto.ecdsa.IKeyPair;

public class SignedTransaction {
    public SignedTransaction(Transaction txn) {
        this.txn = txn;
    }

    @Deprecated
    public SignedTransaction() {}

    public Transaction txn;
    public Hash256 hash;
    public Hash256 signingHash;
    public Hash256 previousSigningHash;
    public String tx_blob;

    public void prepare(IKeyPair keyPair,
                        Amount fee,
                        UInt32 Sequence,
                        UInt32 lastLedgerSequence) {

        VariableLength pubKey = new VariableLength(keyPair.pubBytes());

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

        txn.put(VariableLength.SigningPubKey, pubKey);

        if (Transaction.CANONICAL_FLAG_DEPLOYED) {
            txn.setCanonicalSignatureFlag();
        }

        signingHash = txn.signingHash();
        if (previousSigningHash != null && signingHash.equals(previousSigningHash)) {
            return;
        }
        try {
            VariableLength signature = new VariableLength(keyPair.sign(signingHash.bytes()));
            txn.put(VariableLength.TxnSignature, signature);

            BytesList blob = new BytesList();
            HalfSha512 id = HalfSha512.prefixed256(HashPrefix.transactionID);

            txn.toBytesSink(new MultiSink(blob, id));
            tx_blob = blob.bytesHex();
            hash = id.finish();
        } catch (Exception e) {
            // electric paranoia
            previousSigningHash = null;
            throw new RuntimeException(e);
        } /*else {*/
            previousSigningHash = signingHash;
        // }
    }

    public TransactionType transactionType() {
        return txn.transactionType();
    }
}
