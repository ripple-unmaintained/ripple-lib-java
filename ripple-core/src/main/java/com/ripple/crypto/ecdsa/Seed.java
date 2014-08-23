package com.ripple.crypto.ecdsa;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import static com.ripple.utils.Utils.halfSha512;
import static com.ripple.utils.Utils.quarterSha512;
import com.ripple.utils.Utils;

public class Seed {

    private String seedPhrase;

    public Seed(String seedPhrase) {
        this.seedPhrase = seedPhrase;
    }

    public static byte[] passPhraseToSeedBytes(String str) {
        try {
            return quarterSha512(str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public IKeyPair createKeyPair(String account) {
        int accountNumber = 0;
        BigInteger address = null;
        if (account != null) {
            if (isNumeric(account)) {
                accountNumber = Integer.parseInt(account);
            } else {
                address = new BigInteger(account.getBytes());
            }
        }

        BigInteger secret, pub, privateGen, order = SECP256K1.order();
        byte[] privateGenBytes;
        byte[] publicGenBytes;

        int i = 0;

        while (true) {
            privateGenBytes = hashedIncrement(passPhraseToSeedBytes(seedPhrase), i++);
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

            if (address == null || pub.equals(address)) {
                break;
            }
        }

        return new KeyPair(secret, pub);
    }

    private byte[] hashedIncrement(byte[] bytes, int increment) {
        return halfSha512(appendIntBytes(bytes, increment));
    }

    private byte[] appendIntBytes(byte[] in, long i) {
        byte[] out = new byte[in.length + 4];

        System.arraycopy(in, 0, out, 0, in.length);

        out[in.length] =     (byte) ((i >>> 24) & 0xFF);
        out[in.length + 1] = (byte) ((i >>> 16) & 0xFF);
        out[in.length + 2] = (byte) ((i >>> 8)  & 0xFF);
        out[in.length + 3] = (byte) ((i)       & 0xFF);

        return out;
    }

    private static boolean isNumeric(String str) {
        try {
            int b = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}