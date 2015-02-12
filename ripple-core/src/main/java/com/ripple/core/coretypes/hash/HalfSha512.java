package com.ripple.core.coretypes.hash;

import com.ripple.core.coretypes.hash.prefixes.Prefix;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.SerializedType;

import java.security.MessageDigest;

public class HalfSha512 implements BytesSink {
    MessageDigest messageDigest;

    public HalfSha512() {
        try {
            messageDigest = MessageDigest.getInstance("SHA-512", "RBC");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HalfSha512 prefixed256(Prefix bytes) {
        HalfSha512 halfSha512 = new HalfSha512();
        halfSha512.update(bytes);
        return halfSha512;
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
        byte[] half = digestBytes();
        return new Hash256(half);
    }

    private byte[] digestBytes() {
        byte[] digest = messageDigest.digest();
        byte[] half = new byte[32];
        System.arraycopy(digest, 0, half, 0, 32);
        return half;
    }

    private Hash256 makeHash(byte[] half) {
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

    public HalfSha512 add(SerializedType st) {
        st.toBytesSink(this);
        return this;
    }
}
