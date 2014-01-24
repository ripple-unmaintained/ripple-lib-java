package com.ripple.core.coretypes.hash;

import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;

import java.math.BigInteger;
import java.util.Arrays;

abstract public class HASH<Subclass extends HASH> implements SerializedType, Comparable<Subclass> {
    protected final byte[] hash;
    protected int hashCode = -1;

    public HASH(byte[] bytes, int size) {
        hash = normalizeAndCheckHash(bytes, size);
    }

    @Override
    public String toString() {
        return B16.toString(hash);
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = new BigInteger(1, hash).hashCode();
        }
        return hashCode;
    }

    private byte[] normalizeAndCheckHash(byte[] bytes, int size) {
        int length = bytes.length;
        if (length > size) {
            String simpleName = "";

            throw new RuntimeException("Hash length of " + length + "  is too wide for " + simpleName);
        }
        if (length == size) {
            return bytes;
        } else {
            byte[] hash = new byte[size];
            System.arraycopy(bytes, 0, hash, size - length, length);
            return hash;
        }
    }

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

    @Override
    public int compareTo(Subclass another) {
        int thisLength = bytes().length;
        byte[] bytes = another.bytes();

        for (int i = 0; i < thisLength; i++) {
            int cmp = hash[i] - bytes[i];
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    public byte[] slice(int start) {
        return slice(start, 0);
    }

    public byte get(int i) {
        if (i < 0) i += hash.length;
        return hash[i];
    }

    public byte[] slice(int start, int end) {
        if (start < 0)  start += hash.length;
        if (end  <= 0)  end   += hash.length;

        int length = end - start;
        byte[] slice = new byte[length];

        System.arraycopy(hash, start, slice, 0, length);
        return slice;
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

        @Override
        public void toBytesList(T obj, BytesList to) {
            to.add(obj.hash);
        }
    }
}
