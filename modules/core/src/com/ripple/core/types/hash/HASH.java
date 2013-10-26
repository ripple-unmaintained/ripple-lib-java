package com.ripple.core.types.hash;

import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.encodings.common.B16;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

public class HASH implements SerializedType {
    protected byte[] hash;
    protected int hashCode = -1;

    public HASH(byte[] bytes, int size) {
        setHash(bytes, size);
    }

    @Override
    public String toString() {
        return B16.toString(hash).toUpperCase();
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

    public byte[] getBytes() {
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
    public TypeTranslator translator() {
        return null;
    }

    static public abstract class HashTranslator<T extends HASH> extends TypeTranslator<T> {

        public abstract T newInstance(byte[] b);

        @Override
        public T fromWireBytes(byte[] bytes) {
            return newInstance(bytes);
        }

        @Override
        public Object toJSON(T obj) {
            return B16.toString(obj.hash);
        }

        @Override
        public T fromString(String value) {
            return fromWireBytes(Hex.decode(value));
        }

        @Override
        public byte[] toWireBytes(T obj) {
            return obj.hash;
        }
    }
}
