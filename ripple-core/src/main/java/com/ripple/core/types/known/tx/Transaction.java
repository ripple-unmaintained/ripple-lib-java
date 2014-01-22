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
import com.ripple.crypto.ecdsa.IKeyPair;

public class Transaction extends STObject {
    public Hash256    hash;

    public byte[]     tx_blob;

    public Transaction(TransactionType type) {
        setFormat(TxFormat.formats.get(type));
        put(UInt16.TransactionType, type.asInteger());
    }

//    public UInt32 sequence() {
//        return get(UInt32.Sequence);
//    }

    public void prepare(IKeyPair keyPair, Amount fee, UInt32 Sequence) {
        remove(Field.TxnSignature);

        put(UInt32.Sequence, Sequence);
        put(Amount.Fee, fee);
        put(VariableLength.SigningPubKey, keyPair.pubBytes());

        byte[] signingBlob = STObject.translate.toWireBytes(this);
        Hash256 signingHash = Hash256.signingHash(signingBlob);
        byte[] signature = keyPair.sign(signingHash.bytes());

        // This is included in the final hash
        put(VariableLength.TxnSignature, signature);
        put(VariableLength.SigningPubKey, keyPair.pubBytes());

        tx_blob = STObject.translate.toWireBytes(this);
        hash = Hash256.transactionID(tx_blob);
    }
}
