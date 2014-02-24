package com.ripple.core.types.known.tx;

import com.ripple.core.enums.TransactionType;
import com.ripple.core.fields.Field;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.VariableLength;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.BytesList;
import com.ripple.crypto.ecdsa.IKeyPair;

public class Transaction extends STObject {
    public static final boolean CANONICAL_FLAG_DEPLOYED = false;
    public static final UInt32 CANONICAL_SIGNATURE = new UInt32(0x80000000L);

    public Hash256 hash;
    public Hash256 signingHash;
    public String  tx_blob;

    public Transaction(TransactionType type) {
        setFormat(TxFormat.formats.get(type));
        put(UInt16.TransactionType, type.asInteger());
    }

    public UInt32 sequence() {
        return get(UInt32.Sequence);
    }

    public void prepare(IKeyPair keyPair, Amount fee, UInt32 Sequence, UInt32 lastLedgerSequence) {
        // This won't always be specified
        if (lastLedgerSequence != null) {
            put(UInt32.LastLedgerSequence, lastLedgerSequence);
        }
        put(UInt32.Sequence, Sequence);
        put(Amount.Fee, fee);
        put(VariableLength.SigningPubKey, keyPair.pubBytes());

        if (CANONICAL_FLAG_DEPLOYED) {
            setCanonicalSignatureFlag();
        }

        signingHash = createSigningHash();
        byte[] signature = keyPair.sign(signingHash.bytes());

        // This is included in the final hash
        put(VariableLength.TxnSignature, signature);

        // We can dump this to a list of byte[]
        BytesList to = new BytesList();
        STObject.translate.toBytesSink(this, to);
        // Create the hex
        tx_blob = to.bytesHex();

        // Then the transactionID hash
        Hash256.HalfSha512 halfSha512 = new Hash256.HalfSha512();
        halfSha512.update(Hash256.HASH_PREFIX_TRANSACTION_ID);
        to.updateDigest(halfSha512.digest());

        hash = halfSha512.finish();
    }

    public Hash256 createSigningHash() {
        Hash256.HalfSha512 halfSha512 = new Hash256.HalfSha512();
        halfSha512.update(Hash256.HASH_PREFIX_TX_SIGN);
        toBytesSink(halfSha512, new FieldFilter() {
            @Override
            public boolean evaluate(Field a) {
                return a.isSigningField();
            }
        });
        return halfSha512.finish();
    }


    private void setCanonicalSignatureFlag() {
        UInt32 flags = get(UInt32.Flags);
        if (flags == null) {
            flags = CANONICAL_SIGNATURE;
        } else {
            flags = flags.or(CANONICAL_SIGNATURE);
        }
        put(UInt32.Flags, flags);
    }
}
