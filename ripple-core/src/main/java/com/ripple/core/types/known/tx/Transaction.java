package com.ripple.core.types.known.tx;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.enums.TransactionFlag;
import com.ripple.core.fields.Field;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.utils.HashUtils;

public class Transaction extends STObject {
    public static final boolean CANONICAL_FLAG_DEPLOYED = true;
    public static final UInt32 CANONICAL_SIGNATURE = new UInt32(TransactionFlag.FullyCanonicalSig);

    public Transaction(TransactionType type) {
        setFormat(TxFormat.formats.get(type));
        put(Field.TransactionType, type);
    }

    public SignedTransaction sign(String secret) {
        SignedTransaction signed = SignedTransaction.fromTx(this);
        signed.sign(secret);
        return signed;
    }

    public SignedTransaction sign(IKeyPair keyPair) {
        SignedTransaction signed = SignedTransaction.fromTx(this);
        signed.sign(keyPair);
        return signed;
    }

    public TransactionType transactionType() {
        return transactionType(this);
    }

    public Hash256 signingHash() {
        HalfSha512 signing = HalfSha512.prefixed256(HashPrefix.txSign);
        toBytesSink(signing, new FieldFilter() {
            @Override
            public boolean evaluate(Field a) {
                return a.isSigningField();
            }
        });
        return signing.finish();
    }

    public byte[] signingData() {
        BytesList bl = new BytesList();
        bl.add(HashPrefix.txSign.bytes);
        toBytesSink(bl, new FieldFilter() {
            @Override
            public boolean evaluate(Field a) {
                return a.isSigningField();
            }
        });
        return bl.bytes();
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

    public UInt32 flags() {return get(UInt32.Flags);}
    public UInt32 sourceTag() {return get(UInt32.SourceTag);}
    public UInt32 sequence() {return get(UInt32.Sequence);}
    public UInt32 lastLedgerSequence() {return get(UInt32.LastLedgerSequence);}
    public UInt32 operationLimit() {return get(UInt32.OperationLimit);}
    public Hash256 previousTxnID() {return get(Hash256.PreviousTxnID);}
    public Hash256 accountTxnID() {return get(Hash256.AccountTxnID);}
    public Amount fee() {return get(Amount.Fee);}
    public Blob signingPubKey() {return get(Blob.SigningPubKey);}
    public Blob txnSignature() {return get(Blob.TxnSignature);}
    public AccountID account() {return get(AccountID.Account);}
    public void transactionType(UInt16 val) {put(Field.TransactionType, val);}
    public void flags(UInt32 val) {put(Field.Flags, val);}
    public void sourceTag(UInt32 val) {put(Field.SourceTag, val);}
    public void sequence(UInt32 val) {put(Field.Sequence, val);}
    public void lastLedgerSequence(UInt32 val) {put(Field.LastLedgerSequence, val);}
    public void operationLimit(UInt32 val) {put(Field.OperationLimit, val);}
    public void previousTxnID(Hash256 val) {put(Field.PreviousTxnID, val);}
    public void accountTxnID(Hash256 val) {put(Field.AccountTxnID, val);}
    public void fee(Amount val) {put(Field.Fee, val);}
    public void signingPubKey(Blob val) {put(Field.SigningPubKey, val);}
    public void txnSignature(Blob val) {put(Field.TxnSignature, val);}
    public void account(AccountID val) {put(Field.Account, val);}

    public Hash256 hash() {
        return get(Hash256.hash);
    }

    public AccountID signingKey() {
        byte[] pubKey = HashUtils.SHA256_RIPEMD160(signingPubKey().toBytes());
        return AccountID.fromAddressBytes(pubKey);
    }
}
