package com.ripple.core.coretypes.hash;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.hash.prefixes.LedgerSpace;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.BytesSink;

import java.security.MessageDigest;
import java.util.TreeMap;

public class Hash256 extends Hash<Hash256> {


    public static HalfSha512 prefixed256(HashPrefix bytes) {
        HalfSha512 halfSha512 = new HalfSha512();
        halfSha512.update(bytes);
        return halfSha512;
    }

    @Override
    public Object toJSON() {
        return translate.toJSON(this);
    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        translate.toBytesSink(this, to);
    }

    public static class Hash256Map<Value> extends TreeMap<Hash256, Value> {}

    public Hash256(byte[] bytes) {
        super(bytes, 32);
    }

    public static Hash256 signingHash(byte[] blob) {
        return prefixedHalfSha512(HashPrefix.txSign.bytes, blob);
    }

    public static class HalfSha512 implements BytesSink {
        MessageDigest messageDigest;

        public HalfSha512() {
            try {
                messageDigest = MessageDigest.getInstance("SHA-512", "RBC");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void update(byte[] bytes) {
            messageDigest.update(bytes);
        }

        public void update(Hash256 hash) {
            messageDigest.update(hash.bytes());
        }

        public MessageDigest digest() {
            return messageDigest;
        }

        public Hash256 finish() {
            byte[] digest = messageDigest.digest();
            byte[] half = new byte[32];
            System.arraycopy(digest, 0, half, 0, 32);

            return new Hash256(half);
        }

        @Override
        public void add(byte aByte) {
            messageDigest.update(aByte);
        }

        @Override
        public void add(byte[] bytes) {
            messageDigest.update(bytes);
        }

        public void update(Prefix prefix) {
            messageDigest.update(prefix.bytes());
        }
    }

    public static Hash256 prefixedHalfSha512(byte[] prefix, byte[] blob) {
        HalfSha512 messageDigest = new HalfSha512();
        messageDigest.update(prefix);
        messageDigest.update(blob);
        return messageDigest.finish();
    }

    public static Hash256 prefixedHalfSha512(Prefix prefix, byte[] blob) {
        HalfSha512 messageDigest = new HalfSha512();
        messageDigest.update(prefix);
        messageDigest.update(blob);
        return messageDigest.finish();
    }

    public int nibblet(int depth) {
        int byte_ix = depth > 0 ? depth / 2 : 0;
        int b = super.hash[byte_ix];
        if (depth % 2 == 0) {
            b = (b & 0xF0) >> 4;
        } else {
            b = b & 0x0F;
        }
        return b;
    }

    public static Hash256 transactionID(byte[] blob) {
        return prefixedHalfSha512(HashPrefix.transactionID, blob);
    }

    public static Hash256 accountIDLedgerIndex(AccountID accountID) {
        return prefixedHalfSha512(LedgerSpace.account, accountID.bytes());
    }

    public static class Translator extends HashTranslator<Hash256> {
        @Override
        public Hash256 newInstance(byte[] b) {
            return new Hash256(b);
        }

        @Override
        public int byteWidth() {
            return 32;
        }
    }
    public static Translator translate = new Translator();

    public static TypedFields.Hash256Field hash256Field(final Field f) {
        return new TypedFields.Hash256Field(){ @Override public Field getField() {return f;}};
    }

    static public TypedFields.Hash256Field LedgerHash = hash256Field(Field.LedgerHash);
    static public TypedFields.Hash256Field ParentHash = hash256Field(Field.ParentHash);
    static public TypedFields.Hash256Field TransactionHash = hash256Field(Field.TransactionHash);
    static public TypedFields.Hash256Field AccountHash = hash256Field(Field.AccountHash);
    static public TypedFields.Hash256Field PreviousTxnID = hash256Field(Field.PreviousTxnID);
    static public TypedFields.Hash256Field AccountTxnID = hash256Field(Field.AccountTxnID);
    static public TypedFields.Hash256Field LedgerIndex = hash256Field(Field.LedgerIndex);
    static public TypedFields.Hash256Field WalletLocator = hash256Field(Field.WalletLocator);
    static public TypedFields.Hash256Field RootIndex = hash256Field(Field.RootIndex);
    static public TypedFields.Hash256Field BookDirectory = hash256Field(Field.BookDirectory);
    static public TypedFields.Hash256Field InvoiceID = hash256Field(Field.InvoiceID);
    static public TypedFields.Hash256Field Nickname = hash256Field(Field.Nickname);
    static public TypedFields.Hash256Field Feature = hash256Field(Field.Feature);

    static public TypedFields.Hash256Field hash = hash256Field(Field.hash);
    static public TypedFields.Hash256Field index = hash256Field(Field.index);
}
