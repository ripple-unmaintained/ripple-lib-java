package com.ripple.core.serialized.enums;

import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;

import java.util.TreeMap;

public enum LedgerEntryType implements SerializedType{
    Invalid (-1),
    AccountRoot ('a'),
    DirectoryNode('d'),
    GeneratorMap ('g'),
    RippleState ('r'),
    // Nickname ('n'), // deprecated
    Offer ('o'),
    Contract ('c'),
    LedgerHashes ('h'),
    EnabledAmendments('f'),
    FeeSettings ('s'),
    Ticket('T');

    final int ord;
    LedgerEntryType(int i) {
        ord = i;
    }

    static private TreeMap<Integer, LedgerEntryType> byCode = new TreeMap<Integer, LedgerEntryType>();
    static {
        for (Object a : LedgerEntryType.values()) {
            LedgerEntryType f = (LedgerEntryType) a;
            byCode.put(f.ord, f);
        }
    }

    public static LedgerEntryType fromNumber(Number i) {
        return byCode.get(i.intValue());
    }

    public Integer asInteger() {
        return ord;
    }

    // SeralizedType interface
    @Override
    public byte[] toBytes() {
        return new byte[]{(byte) (ord >> 8), (byte) (ord & 0xFF)};
    }
    @Override
    public Object toJSON() {
        return toString();
    }
    @Override
    public String toHex() {
        return B16.toString(toBytes());
    }
    @Override
    public void toBytesSink(BytesSink to) {
        to.add(toBytes());
    }
    public static class Translator extends TypeTranslator<LedgerEntryType> {
        @Override
        public LedgerEntryType fromParser(BinaryParser parser, Integer hint) {
            byte[] read = parser.read(2);
            return fromNumber(((read[0] & 0xFF) << 8) | (read[1] & 0xFF));
        }

        @Override
        public LedgerEntryType fromInteger(int integer) {
            return fromNumber(integer);
        }

        @Override
        public LedgerEntryType fromString(String value) {
            return LedgerEntryType.valueOf(value);
        }
    }
    public static Translator translate = new Translator();
}
