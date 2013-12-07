package com.ripple.core.types.hash;

import java.math.BigInteger;
import java.util.Arrays;

import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesTree;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;

public class HASH implements SerializedType {
    protected byte[] hash;
    protected int hashCode = -1;

    public HASH(byte[] bytes, int size) {
        setHash(bytes, size);
    }

    @Override
    public String toString() {
        return B16.toString(hash);
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            // XXX
            hashCode = new BigInteger(1, hash).hashCode();
        }
        return hashCode;
    }

    private void setHash(byte[] bytes, int size) {
        int length = bytes.length;
        if (length > size) {
            throw new RuntimeException("Hash length of " + length + "  is too wide for " + getClass().getSimpleName());
        }
        if (length == size) {
            hash = bytes;
        } else {
            hash = new byte[size];
            System.arraycopy(bytes, 0, hash, size - length, length);
        }
    }

    protected HASH(){}

    BigInteger bigInteger() {
        return new BigInteger(1, hash);
    }

    public byte[] bytes() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HASH) {
            return Arrays.equals(hash, ((HASH) obj).hash);
        }

        return super.equals(obj);
    }

    static public abstract class HashTranslator<T extends HASH> extends TypeTranslator<T> {

        public abstract T newInstance(byte[] b);
        public abstract int byteWidth();

        @Override
        public T fromParser(BinaryParser parser, Integer hint) {
            return newInstance(parser.read(byteWidth()));
        }

        @Override
        public Object toJSON(T obj) {
            return B16.toString(obj.hash);
        }

        @Override
        public T fromString(String value) {
            return newInstance(B16.decode(value));
        }

//        @Override
//        public byte[] toBytesTree(T obj) {
//            return obj.hash;
//        }

        @Override
        public void toBytesTree(T obj, BytesTree to) {
            to.add(obj.hash);
        }
    }
}
