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

    public Transaction(TransactionType type) {
        setFormat(TxFormat.formats.get(type));
        put(UInt16.TransactionType, type.asInteger());
    }

    public UInt32 sequence() {
        return get(UInt32.Sequence);
    }

    public Hash256 signingHash() {
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

    public void setCanonicalSignatureFlag() {
        UInt32 flags = get(UInt32.Flags);
        if (flags == null) {
            flags = CANONICAL_SIGNATURE;
        } else {
            flags = flags.or(CANONICAL_SIGNATURE);
        }
        put(UInt32.Flags, flags);
    }
}
