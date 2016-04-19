package com.ripple.core.coretypes.hash;

import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.fields.Field;
import com.ripple.core.fields.Hash256Field;
import com.ripple.core.fields.Type;
import com.ripple.core.serialized.BytesSink;

import java.math.BigInteger;
import java.util.TreeMap;

public class Hash256 extends Hash<Hash256> {

    public static final BigInteger bookBaseSize = new BigInteger("10000000000000000", 16);

    public int divergenceDepth(Hash256 other) {
        return divergenceDepth(0, other);
    }
    public int divergenceDepth(int i, Hash256 other) {
        for (; i < 64; i++) {
            if (nibblet(i) != other.nibblet(i)) {
                break;
            }
        }
        return i;
    }

    public static class Hash256Map<Value> extends TreeMap<Hash256, Value> {
        public Hash256Map(Hash256Map<Value> cache) {
            super(cache);
        }
        public Hash256Map() {

        }
    }
    public static final Hash256 ZERO_256 = new Hash256(new byte[32]);

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

    @Override
    public Type type() {
        return Type.Hash256;
    }

    public boolean isZero() {
        return this == Hash256.ZERO_256 || equals(Hash256.ZERO_256);
    }

    public boolean isNonZero() {
        return !isZero();
    }

    public static Hash256 fromHex(String s) {
        return translate.fromHex(s);
    }

    public Hash256(byte[] bytes) {
        super(bytes, 32);
    }

    public static Hash256 signingHash(byte[] blob) {
        return prefixedHalfSha512(HashPrefix.txSign, blob);
    }

    public static Hash256 prefixedHalfSha512(Prefix prefix, byte[] blob) {
        HalfSha512 messageDigest = HalfSha512.prefixed256(prefix);
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

    public static Hash256Field hash256Field(final Field f) {
        return new Hash256Field(){ @Override public Field getField() {return f;}};
    }

    static public Hash256Field LedgerHash = hash256Field(Field.LedgerHash);
    static public Hash256Field ParentHash = hash256Field(Field.ParentHash);
    static public Hash256Field TransactionHash = hash256Field(Field.TransactionHash);
    static public Hash256Field AccountHash = hash256Field(Field.AccountHash);
    static public Hash256Field PreviousTxnID = hash256Field(Field.PreviousTxnID);
    static public Hash256Field AccountTxnID = hash256Field(Field.AccountTxnID);
    static public Hash256Field LedgerIndex = hash256Field(Field.LedgerIndex);
    static public Hash256Field WalletLocator = hash256Field(Field.WalletLocator);
    static public Hash256Field RootIndex = hash256Field(Field.RootIndex);
    static public Hash256Field BookDirectory = hash256Field(Field.BookDirectory);
    static public Hash256Field InvoiceID = hash256Field(Field.InvoiceID);
    static public Hash256Field Nickname = hash256Field(Field.Nickname);
    static public Hash256Field Amendment = hash256Field(Field.Amendment);
    static public Hash256Field TicketID = hash256Field(Field.TicketID);

    static public Hash256Field hash = hash256Field(Field.hash);
    static public Hash256Field index = hash256Field(Field.index);
}
