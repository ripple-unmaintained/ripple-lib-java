package com.ripple.crypto.ecdsa;

import com.ripple.config.Config;
import com.ripple.encodings.B58IdentiferCodecs;
import com.ripple.encodings.base58.B58;
import com.ripple.utils.Sha512;
import com.ripple.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

import static com.ripple.config.Config.getB58IdentiferCodecs;

public class Seed {
    public static byte[] VER_K256 = new byte[]{(byte) B58IdentiferCodecs.VER_FAMILY_SEED};
    public static byte[] VER_ED25519 = new byte[]{(byte) 0x1, (byte) 0xe1, (byte) 0x4b};

    final byte[] seedBytes;
    byte[] version;

    public Seed(byte[] seedBytes) {
        this(VER_K256, seedBytes);
    }
    public Seed(byte[] version, byte[] seedBytes) {
        this.seedBytes = seedBytes;
        this.version = version;
    }

    @Override
    public String toString() {
        return Config.getB58().encodeToStringChecked(seedBytes, version);
    }

    public byte[] bytes() {
        return seedBytes;
    }

    public byte[] version() {
        return version;
    }

    public Seed setEd25519() {
        this.version = VER_ED25519;
        return this;
    }

    public IKeyPair keyPair() {
        return keyPair(0);
    }

    public IKeyPair rootKeyPair() {
        return keyPair(-1);
    }

    public IKeyPair keyPair(int account) {
        if (Arrays.equals(version, VER_ED25519)) {
            if (account != 0) throw new AssertionError();
            return EDKeyPair.from128Seed(seedBytes);
        }  else {
            return createKeyPair(seedBytes, account);
        }

    }
    public static Seed fromBase58(String b58) {
        B58.Decoded decoded = Config.getB58().decodeMulti(b58, 16, VER_K256, VER_ED25519);
        return new Seed(decoded.version, decoded.payload);
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
        privateGen = K256KeyPair.computePrivateGen(seedBytes);
        byte[] publicGenBytes = K256KeyPair.computePublicGenerator(privateGen);

        if (accountNumber == -1) {
            // The root keyPair
            return new K256KeyPair(privateGen, Utils.uBigInt(publicGenBytes));
        } else {
            secret = K256KeyPair.computeSecretKey(privateGen, publicGenBytes, accountNumber);
            pub = K256KeyPair.computePublicKey(secret);
            return new K256KeyPair(secret, pub);
        }

    }

    public static IKeyPair getKeyPair(byte[] seedBytes) {
        return createKeyPair(seedBytes, 0);
    }

    public static IKeyPair getKeyPair(String b58) {
        return getKeyPair(getB58IdentiferCodecs().decodeFamilySeed(b58));
    }
}


