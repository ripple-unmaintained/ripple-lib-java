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
    public Hash256    hash;
    public String     tx_blob;

    public Transaction(TransactionType type) {
        setFormat(TxFormat.formats.get(type));
        put(UInt16.TransactionType, type.asInteger());
    }

    public UInt32 sequence() {
        return get(UInt32.Sequence);
    }

    public void prepare(IKeyPair keyPair, Amount fee, UInt32 Sequence, UInt32 lastLedgerSequence) {
        remove(Field.TxnSignature);

        // This won't always be specified
        if (lastLedgerSequence != null) {
            put(UInt32.LastLedgerSequence, lastLedgerSequence);
        }
        put(UInt32.Sequence, Sequence);
        put(Amount.Fee, fee);
        put(VariableLength.SigningPubKey, keyPair.pubBytes());

        byte[] signingBlob = STObject.translate.toBytes(this);
        Hash256 signingHash = Hash256.signingHash(signingBlob);
        byte[] signature = keyPair.sign(signingHash.bytes());

        // This is included in the final hash
        put(VariableLength.TxnSignature, signature);
        put(VariableLength.SigningPubKey, keyPair.pubBytes());

        BytesList to = new BytesList();
        STObject.translate.toBytesList(this, to);
        tx_blob = to.bytesHex();

        Hash256.HalfSha512 halfSha512 = new Hash256.HalfSha512();
        halfSha512.update(Hash256.HASH_PREFIX_TRANSACTION_ID);
        to.updateDigest(halfSha512.digest());

        hash = halfSha512.finish();
    }
}
