package com.ripple.crypto.ecdsa;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import static com.ripple.utils.Utils.halfSha512;
import static com.ripple.utils.Utils.quarterSha512;
import com.ripple.core.coretypes.AccountID;
import com.ripple.utils.Utils;

public class Seed {

    private static final BigInteger ORDER = SECP256K1.order();
    private static final int DEFAULT_SEQ = 0;

    public static byte[] passPhraseToSeedBytes(String seed) {
        try {
            return quarterSha512(seed.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static IKeyPair createKeyPairFromAccountNumber(String seedStr, int accountNumber) {
        BigInteger privateGen = createPrivateGen(passPhraseToSeedBytes(seedStr));
        byte[] publicGenBytes = SECP256K1.basePointMultipliedBy(privateGen);

        return createKeyPair(publicGenBytes, accountNumber, privateGen);
    }

    public static IKeyPair createKeyPairFromAddress(String seedStr, String address) {
        IKeyPair keyPair;
        int maxLoops = 1000;
        BigInteger privateGen = createPrivateGen(passPhraseToSeedBytes(seedStr));
        byte[] publicGenBytes = SECP256K1.basePointMultipliedBy(privateGen);
        AccountID accountID = AccountID.fromAddress(address);

        int i = 0;
        while (true) {
            keyPair = createKeyPair(publicGenBytes, i++, privateGen);

            if (--maxLoops == 0) {
                throw new RuntimeException("Too many loops looking for KeyPair yielding: " + address);
            }
            if (Utils.uBigInt(Utils.SHA256_RIPEMD160(keyPair.pubBytes())).equals(Utils.uBigInt(accountID.bytes()))) {
                break;
            }
        }
        return keyPair;
    }

    public static IKeyPair createKeyPairFromSeedBytes(byte[] seedBytes) {
        BigInteger privateGen = createPrivateGen(seedBytes);
        byte[] publicGenBytes = SECP256K1.basePointMultipliedBy(privateGen);

        return createKeyPair(publicGenBytes, DEFAULT_SEQ, privateGen);
    }

    private static IKeyPair createKeyPair(byte[] publicGenBytes, int seq, BigInteger privateGen) {
        BigInteger secret;

        int i = 0;
        while (true) {
            byte[] secretBytes = hashedIncrement(appendIntBytes(publicGenBytes, seq), i++);
            secret = Utils.uBigInt(secretBytes);
            if (secret.compareTo(ORDER) == -1) {
                break;
            }
        }

        secret = secret.add(privateGen).mod(ORDER);
        BigInteger pub = Utils.uBigInt(SECP256K1.basePointMultipliedBy(secret));

        return new KeyPair(secret, pub);
    }

    private static BigInteger createPrivateGen(byte[] seedBytes) {
        BigInteger privateGen;
        byte[] privateGenBytes;

        int i = 0;
        while (true) {
            privateGenBytes = hashedIncrement(seedBytes, i++);
            privateGen = Utils.uBigInt(privateGenBytes);
            if (privateGen.compareTo(ORDER) == -1) {
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
}
