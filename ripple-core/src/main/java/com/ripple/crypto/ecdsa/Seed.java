package com.ripple.crypto.ecdsa;

import com.ripple.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import static com.ripple.config.Config.getB58IdentiferCodecs;
import static com.ripple.utils.Utils.halfSha512;
import static com.ripple.utils.Utils.quarterSha512;

public class Seed {
    final byte[] seedBytes;

    public Seed(byte[] seedBytes) {
        this.seedBytes = seedBytes;
    }

    public static Seed fromBase58(String hex) {
        return new Seed(getB58IdentiferCodecs().decodeFamilySeed(hex));
    }

    @Override
    public String toString() {
        return getB58IdentiferCodecs().encodeFamilySeed(seedBytes);
    }

    public static Seed fromPassPhrase(String passPhrase) {
        return new Seed(passPhraseToSeedBytes(passPhrase));
    }

    public IKeyPair keyPair() {
        return createKeyPair(seedBytes, 0);
    }

    public IKeyPair keyPair(int account) {
        return createKeyPair(seedBytes, account);
    }

    public static byte[] passPhraseToSeedBytes(String seed) {
        try {
            return quarterSha512(seed.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static IKeyPair createKeyPair(byte[] seedBytes) {
        return createKeyPair(seedBytes, 0);
    }
    public static IKeyPair createKeyPair(byte[] seedBytes, int accountNumber) {
        BigInteger secret, pub, privateGen;
        privateGen = makePrivateGen(seedBytes);
        secret = makeSecretKey(privateGen, accountNumber);
        pub = makePublicKey(secret);
        return new KeyPair(secret, pub);
    }

    public static BigInteger makePublicKey(BigInteger secret) {
        return Utils.uBigInt(SECP256K1.basePointMultipliedBy(secret));
    }

    public static BigInteger makeSecretKey(BigInteger privateGen, int accountNumber) {
        byte[] publicGenBytes;
        BigInteger secret;
        int i;
        publicGenBytes = SECP256K1.basePointMultipliedBy(privateGen);

        i=0;
        while (true) {
            byte[] secretBytes = hashedIncrement(appendIntBytes(publicGenBytes, accountNumber), i++);
            secret = Utils.uBigInt(secretBytes);
            if (secret.compareTo(SECP256K1.order()) == -1) {
                break;
            }
        }

        secret = secret.add(privateGen).mod(SECP256K1.order());
        return secret;
    }

    public static BigInteger makePrivateGen(byte[] seedBytes) {
        byte[] privateGenBytes;
        BigInteger privateGen;
        int i = 0;

        while (true) {
            privateGenBytes = hashedIncrement(seedBytes, i++);
            privateGen = Utils.uBigInt(privateGenBytes);
            if (privateGen.compareTo(SECP256K1.order()) == -1) {
                break;
            }
        }
        return privateGen;
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

    public static IKeyPair getKeyPair(byte[] master_seed) {
        return createKeyPair(master_seed, 0);
    }

    public static IKeyPair getKeyPair(String master_seed) {
        return getKeyPair(getB58IdentiferCodecs().decodeFamilySeed(master_seed));
    }
}
