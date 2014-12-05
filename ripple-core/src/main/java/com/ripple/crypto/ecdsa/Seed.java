package com.ripple.crypto.ecdsa;

import com.ripple.utils.Sha512;
import com.ripple.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import static com.ripple.config.Config.getB58IdentiferCodecs;

public class Seed {
    // See https://wiki.ripple.com/Account_Family
    final byte[] seedBytes;

    public Seed(byte[] seedBytes) {
        this.seedBytes = seedBytes;
    }

    @Override
    public String toString() {
        return getB58IdentiferCodecs().encodeFamilySeed(seedBytes);
    }

    public byte[] getBytes() {
        return seedBytes;
    }

    public IKeyPair keyPair() {
        return createKeyPair(seedBytes, 0);
    }
    public IKeyPair rootKeyPair() {
        return createKeyPair(seedBytes, -1);
    }

    public IKeyPair keyPair(int account) {
        return createKeyPair(seedBytes, account);
    }

    public static Seed fromBase58(String b58) {
        return new Seed(getB58IdentiferCodecs().decodeFamilySeed(b58));
    }

    public static Seed fromPassPhrase(String passPhrase) {
        return new Seed(passPhraseToSeedBytes(passPhrase));
    }

    public static byte[] passPhraseToSeedBytes(String phrase) {
        try {
            return new Sha512(phrase.getBytes("utf-8")).finish128();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static IKeyPair createKeyPair(byte[] seedBytes) {
        return createKeyPair(seedBytes, 0);
    }

    public static IKeyPair createKeyPair(byte[] seedBytes, int accountNumber) {
        BigInteger secret, pub, privateGen;
        // The private generator (aka root private key, master private key)
        privateGen = computePrivateGen(seedBytes);
        byte[] publicGenBytes = computePublicGenerator(privateGen);

        if (accountNumber == -1) {
            // The root keyPair
            return new KeyPair(privateGen, Utils.uBigInt(publicGenBytes));
        }
        else {
            secret = computeSecretKey(privateGen, publicGenBytes, accountNumber);
            pub = computePublicKey(secret);
            return new KeyPair(secret, pub);
        }

    }

    /**
     *
     * @param secretKey secret point on the curve as BigInteger
     * @return corresponding public point
     */
    public static byte[] getPublic(BigInteger secretKey) {
        return SECP256K1.basePointMultipliedBy(secretKey);
    }

    /**
     *
     * @param privateGen secret point on the curve as BigInteger
     * @return the corresponding public key is the public generator
     *         (aka public root key, master public key).
     *         return as byte[] for convenience.
     */
    public static byte[] computePublicGenerator(BigInteger privateGen) {
        return getPublic(privateGen);
    }

    public static BigInteger computePublicKey(BigInteger secret) {
        return Utils.uBigInt(getPublic(secret));
    }

    public static BigInteger computePrivateGen(byte[] seedBytes) {
        byte[] privateGenBytes;
        BigInteger privateGen;
        int i = 0;

        while (true) {
            privateGenBytes = new Sha512().add(seedBytes)
                                          .add32(i++)
                                          .finish256();
            privateGen = Utils.uBigInt(privateGenBytes);
            if (privateGen.compareTo(SECP256K1.order()) == -1) {
                break;
            }
        }
        return privateGen;
    }

    public static BigInteger computeSecretKey(BigInteger privateGen, byte[] publicGenBytes, int accountNumber) {
        BigInteger secret;
        int i;

        i=0;
        while (true) {
            byte[] secretBytes = new Sha512().add(publicGenBytes)
                                             .add32(accountNumber)
                                             .add32(i++)
                                             .finish256();
            secret = Utils.uBigInt(secretBytes);
            if (secret.compareTo(SECP256K1.order()) == -1) {
                break;
            }
        }

        secret = secret.add(privateGen).mod(SECP256K1.order());
        return secret;
    }

    public static IKeyPair getKeyPair(byte[] seedBytes) {
        return createKeyPair(seedBytes, 0);
    }

    public static IKeyPair getKeyPair(String b58) {
        return getKeyPair(getB58IdentiferCodecs().decodeFamilySeed(b58));
    }
}


