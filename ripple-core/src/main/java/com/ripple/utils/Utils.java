package com.ripple.utils;

import com.ripple.encodings.common.B16;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Utils {
    private static final MessageDigest digest;
    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }

    /**
     * See {@link Utils#doubleDigest(byte[], int, int)}.
     */
    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
     * standard procedure in Bitcoin. The resulting hash is in big endian form.
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        synchronized (digest) {
            digest.reset();
            digest.update(input, offset, length);
            byte[] first = digest.digest();
            return digest.digest(first);
        }
    }


    public static BigInteger hexBig(String hex) {
        return new BigInteger(1, Hex.decode(hex));
    }

    public static byte[] halfSha512(byte[] bytes) {
        byte[] hash = new byte[32];
        System.arraycopy(sha512(bytes), 0, hash, 0, 32);
        return hash;
    }

    public static byte[] quarterSha512(byte[] bytes) {
        byte[] hash = new byte[16];
        System.arraycopy(sha512(bytes), 0, hash, 0, 16);
        return hash;
    }

    public static byte[] sha512(byte[] byteArrays) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512", "BC");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
        messageDigest.update(byteArrays);
        return messageDigest.digest();
    }

    public static byte[] SHA256_RIPEMD160(byte[] input) {
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(input);
            RIPEMD160Digest digest = new RIPEMD160Digest();
            digest.update(sha256, 0, sha256.length);
            byte[] out = new byte[20];
            digest.doFinal(out, 0);
            return out;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public static String bigHex(BigInteger bn) {
        return B16.toString(bn.toByteArray()).toUpperCase();
    }

    public static BigInteger uBigInt(byte[] bytes) {
        return new BigInteger(1, bytes);
    }
}
