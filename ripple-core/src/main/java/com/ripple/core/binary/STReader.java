package com.ripple.core.binary;

import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.Hash128;
import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.StreamBinaryParser;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.result.TransactionMeta;
import com.ripple.core.types.known.tx.result.TransactionResult;

import java.util.Arrays;
import java.util.Date;

public class STReader {
    protected BinaryParser parser;
    public STReader(BinaryParser parser) {
        this.parser = parser;
    }
    public STReader(String hex) {
        this.parser = new BinaryParser(hex);
    }

    public static STReader fromFile(String arg) {
        return new STReader(StreamBinaryParser.fromFile(arg));
    }

    public UInt8 uInt8() {
        return UInt8.translate.fromParser(parser);
    }
    public UInt16 uInt16() {
        return UInt16.translate.fromParser(parser);
    }
    public UInt32 uInt32() {
        return UInt32.translate.fromParser(parser);
    }
    public UInt64 uInt64() {
        return UInt64.translate.fromParser(parser);
    }
    public Hash128 hash128() {
        return Hash128.translate.fromParser(parser);
    }
    public Hash160 hash160() {
        return Hash160.translate.fromParser(parser);
    }
    public Currency currency() {
        return Currency.translate.fromParser(parser);
    }
    public Hash256 hash256() {
        return Hash256.translate.fromParser(parser);
    }
    public Vector256 vector256() {
        return Vector256.translate.fromParser(parser);
    }
    public AccountID accountID() {
        return AccountID.translate.fromParser(parser);
    }
    public Blob variableLength() {
        int hint = parser.readVLLength();
        return Blob.translate.fromParser(parser, hint);
    }
    public Amount amount() {
        return Amount.translate.fromParser(parser);
    }
    public PathSet pathSet() {
        return PathSet.translate.fromParser(parser);
    }

    public STObject stObject() {
        return STObject.translate.fromParser(parser);
    }
    public STObject vlStObject() {
        return STObject.translate.fromParser(parser, parser.readVLLength());
    }

    public HashPrefix hashPrefix() {
        byte[] read = parser.read(4);
        for (HashPrefix hashPrefix : HashPrefix.values()) {
            if (Arrays.equals(read, hashPrefix.bytes)) {
                return hashPrefix;
            }
        }
        return null;
    }

    public STArray stArray() {
        return STArray.translate.fromParser(parser);
    }
    public Date rippleDate() {
        return RippleDate.fromParser(parser);
    }

    public BinaryParser parser() {
        return parser;
    }

    public TransactionResult readTransactionResult(UInt32 ledgerIndex) {
        Hash256 hash = hash256();
        Transaction txn = (Transaction) vlStObject();
        TransactionMeta meta = (TransactionMeta) vlStObject();
        return new TransactionResult(ledgerIndex.longValue(), hash, txn, meta);
    }

    public LedgerEntry readLE() {
        Hash256 index = hash256();
        STObject object = vlStObject();
        LedgerEntry le = (LedgerEntry) object;
        le.index(index);
        return le;
    }

    public int readOneInt() {
        return parser.readOneInt();
    }

    public boolean end() {
        return parser.end();
    }
}
