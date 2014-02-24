package com.ripple.core.types.known.tx.signed;

import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.VariableLength;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.enums.TransactionType;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.crypto.ecdsa.IKeyPair;

public class SignedTransaction {
    public Transaction txn;
    public Hash256 hash;
    public Hash256 signingHash;
    public Hash256 previousSigningHash;
    public String  tx_blob;

    public void prepare(IKeyPair keyPair, Amount fee, UInt32 Sequence, UInt32 lastLedgerSequence) {
        // This won't always be specified
        if (lastLedgerSequence != null) {
            txn.put(UInt32.LastLedgerSequence, lastLedgerSequence);
        }
        txn.put(UInt32.Sequence, Sequence);
        txn.put(Amount.Fee, fee);
        txn.put(VariableLength.SigningPubKey, keyPair.pubBytes());

        if (Transaction.CANONICAL_FLAG_DEPLOYED) {
            txn.setCanonicalSignatureFlag();
        }

        signingHash = txn.signingHash();
        if (previousSigningHash != null && signingHash.equals(previousSigningHash)) {
            return;
        }
        previousSigningHash = null;

        try {
            byte[] signature = keyPair.sign(signingHash.bytes());

            // This is included in the final hash
            txn.put(VariableLength.TxnSignature, signature);

            // We can dump this to a list of byte[]
            BytesList to = new BytesList();
            STObject.translate.toBytesSink(txn, to);
            // Create the hex
            tx_blob = to.bytesHex();

            // Then the transactionID hash
            Hash256.HalfSha512 halfSha512 = new Hash256.HalfSha512();
            halfSha512.update(Hash256.HASH_PREFIX_TRANSACTION_ID);
            to.updateDigest(halfSha512.digest());
            hash = halfSha512.finish();
        } finally {
            previousSigningHash = signingHash;
        }
    }

    public TransactionType transactionType() {
        return txn.transactionType();
    }
}
