package com.ripple.core.binary;

import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.Hash128;
import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;

import java.util.Date;

public class BinaryReader {
    // TODO: maybe this EXTENDS or delegates ??
    BinaryParser parser;
    public BinaryReader(BinaryParser parser) {
        this.parser = parser;
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
    public VariableLength variableLength() {
        int hint = parser.readVLLength();
        return VariableLength.translate.fromParser(parser, hint);
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
    public STArray stArray() {
        return STArray.translate.fromParser(parser);
    }
    public Date rippleDate() {
        return RippleDate.fromParser(parser);
    }
}
