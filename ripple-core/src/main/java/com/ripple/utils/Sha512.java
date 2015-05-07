package com.ripple.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Sha512 {
    MessageDigest messageDigest;

    public Sha512() {
        try {
            messageDigest = MessageDigest.getInstance("SHA-512", "RBC");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public Sha512(byte[] start) {
        this();
        add(start);
    }

    public Sha512 add(byte[] bytes) {
        messageDigest.update(bytes);
        return this;
    }

    public Sha512 addU32(int i) {
        messageDigest.update((byte) ((i >>> 24) & 0xFF));
        messageDigest.update((byte) ((i >>> 16) & 0xFF));
        messageDigest.update((byte) ((i >>> 8)  & 0xFF));
        messageDigest.update((byte) ((i)        & 0xFF));
        return this;
    }

    private byte[] finishTaking(int size) {
        byte[] hash = new byte[size];
        System.arraycopy(messageDigest.digest(), 0, hash, 0, size);
        return hash;
    }

    public byte[] finish128() {
        return finishTaking(16);
    }

    public byte[] finish256() {
        return finishTaking(32);
    }

    public byte[] finish() {
        return messageDigest.digest();
    }
}
