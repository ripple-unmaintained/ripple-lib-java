package com.ripple.core.binary;

import com.ripple.core.serialized.BytesList;
import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.Hash128;
import com.ripple.core.coretypes.hash.Hash160;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;

public class BinaryWriter {
    BytesList tree;

    public BinaryWriter(BytesList tree) {
        this.tree = tree;
    }

    public void uInt8(UInt8 obj) {
        UInt8.translate.toBytesList(obj, tree);
    }
    public void uInt16(UInt16 obj) {
        UInt16.translate.toBytesList(obj, tree);
    }
    public void uInt32(UInt32 obj) {
        UInt32.translate.toBytesList(obj, tree);
    }
    public void uInt64(UInt64 obj) {
        UInt64.translate.toBytesList(obj, tree);
    }
    public void hash128(Hash128 obj) {
        Hash128.translate.toBytesList(obj, tree);
    }
    public void hash160(Hash160 obj) {
        Hash160.translate.toBytesList(obj, tree);
    }
    public void hash256(Hash256 obj) {
        Hash256.translate.toBytesList(obj, tree);
    }
    public void currency(Currency obj) {
        Currency.translate.toBytesList(obj, tree);
    }
    public void vector256(Vector256 obj) {
        Vector256.translate.toBytesList(obj, tree);
    }
    public void accountID(AccountID obj) {
        AccountID.translate.toBytesList(obj, tree);
    }
    public void pathSet(PathSet obj) {
        PathSet.translate.toBytesList(obj, tree);
    }
    public void stObject(STObject obj) {
        STObject.translate.toBytesList(obj, tree);
    }
    public void stArray(STArray obj) {
        STArray.translate.toBytesList(obj, tree);
    }
}
