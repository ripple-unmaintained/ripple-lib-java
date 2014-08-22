package com.ripple.crypto.ecdsa;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

import static com.ripple.utils.Utils.halfSha512;
import static com.ripple.utils.Utils.quarterSha512;
import com.ripple.utils.Utils;

public class Seed {
    public static byte[] passPhraseToSeedBytes(String seed) {
        try {
            return quarterSha512(seed.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static IKeyPair createKeyPair(Object account) {
        int accountNumber = 0;
        byte[] address = null;
        if (account instanceof Integer) {
            accountNumber = (Integer) account;
        } else {
            address = (byte[]) account;
        }

        BigInteger secret, pub, privateGen, order = SECP256K1.order();
        byte[] privateGenBytes;
        byte[] publicGenBytes;

        int i = 0;

        while (true) {
            privateGenBytes = hashedIncrement(address, i++);
            privateGen = Utils.uBigInt(privateGenBytes);
            if (privateGen.compareTo(order) == -1) {
                break;
            }
        }
        publicGenBytes = SECP256K1.basePointMultipliedBy(privateGen);

        i = 0;
        int maxLoops = 1000;
        while (true) {
            while (true) {
                byte[] secretBytes = hashedIncrement(appendIntBytes(publicGenBytes, accountNumber), i++);
                secret = Utils.uBigInt(secretBytes);
                if (secret.compareTo(order) == -1) {
                    break;
                }
            }

            accountNumber += 1;
            secret = secret.add(privateGen).mod(order);
            pub = Utils.uBigInt(SECP256K1.basePointMultipliedBy(secret));

            if (--maxLoops <= 0) {
                throw new RuntimeException("Too many loops looking for KeyPair yielding: " + address);
            }

            if (address == null || Arrays.equals(pub.toByteArray(), address)) {
                break;
            }
        }

        return new KeyPair(secret, pub);
    }

    private static byte[] hashedIncrement(byte[] bytes, int increment) {
        return halfSha512(appendIntBytes(bytes, increment));
    }

    public static byte[] appendIntBytes(byte[] in, long i) {
        byte[] out = new byte[in.length + 4];

        System.arraycopy(in, 0, out, 0, in.length);

        out[in.length] =     (byte) ((i >>> 24) & 0xFF);
        out[in.length + 1] = (byte) ((i >>> 16) & 0xFF);
        out[in.length + 2] = (byte) ((i >>> 8)  & 0xFF);
        out[in.length + 3] = (byte) ((i)       & 0xFF);

        return out;
    }
}
