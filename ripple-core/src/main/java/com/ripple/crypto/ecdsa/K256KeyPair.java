package com.ripple.crypto.ecdsa;

import com.ripple.utils.HashUtils;
import com.ripple.utils.Sha512;
import com.ripple.utils.Utils;
import org.ripple.bouncycastle.crypto.digests.SHA256Digest;
import org.ripple.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.ripple.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.ripple.bouncycastle.crypto.signers.ECDSASigner;
import org.ripple.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.ripple.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class K256KeyPair implements IKeyPair {
    BigInteger priv, pub;
    byte[] pubBytes;

    // See https://wiki.ripple.com/Account_Family
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
        return generateKey(seedBytes, null);
    }

    public static byte[] computePublicKey(byte[] publicGenBytes,
                                          int accountNumber) {
        ECPoint rootPubPoint = SECP256K1.curve().decodePoint(publicGenBytes);
        BigInteger scalar = generateKey(publicGenBytes, accountNumber);
        ECPoint point = SECP256K1.basePoint().multiply(scalar);
        ECPoint offset = rootPubPoint.add(point);
        return offset.getEncoded(true);
    }

    public static BigInteger computeSecretKey(BigInteger privateGen,
                                              byte[] publicGenBytes,
                                              int accountNumber) {
        return generateKey(publicGenBytes, accountNumber)
                        .add(privateGen).mod(SECP256K1.order());
    }

    /**
     * @param seedBytes - a bytes sequence of arbitrary length which will be hashed
     * @param discriminator - nullable optional uint32 to hash
     * @return a number between [1, order -1] suitable as a private key
     *
     */
    public static BigInteger generateKey(byte[] seedBytes, Integer discriminator) {
        BigInteger key = null;
        for (long i = 0; i <= 0xFFFFFFFFL; i++) {
            Sha512 sha512 = new Sha512().add(seedBytes);
            if (discriminator != null) {
                sha512.addU32(discriminator);
            }
            sha512.addU32((int) i);
            byte[] keyBytes = sha512.finish256();
            key = Utils.uBigInt(keyBytes);
            if (key.compareTo(BigInteger.ZERO) == 1 &&
                key.compareTo(SECP256K1.order()) == -1) {
                break;
            }
        }
        return key;
    }

    @Override
    public BigInteger pub() {
        return pub;
    }

    @Override
    public byte[] canonicalPubBytes() {
        return pubBytes;
    }

    public K256KeyPair(BigInteger priv, BigInteger pub) {
        this.priv = priv;
        this.pub = pub;
        this.pubBytes = pub.toByteArray();
    }

    @Override
    public BigInteger priv() {
        return priv;
    }

    public boolean verifyHash(byte[] hash, byte[] sigBytes) {
        return verify(hash, sigBytes, pub);
    }

    public byte[] signHash(byte[] bytes) {
        return signHash(bytes, priv);
    }

    @Override
    public boolean verifySignature(byte[] message, byte[] sigBytes) {
        byte[] hash = HashUtils.halfSha512(message);
        return verifyHash(hash, sigBytes);
    }

    @Override
    public byte[] signMessage(byte[] message) {
        byte[] hash = HashUtils.halfSha512(message);
        return signHash(hash);
    }

    @Override
    public byte[] pub160Hash() {
        return HashUtils.SHA256_RIPEMD160(pubBytes);
    }

    @Override
    public String canonicalPubHex() {
        return Utils.bigHex(pub);
    }

    @Override
    public String privHex() {
        return Utils.bigHex(priv);
    }

    public static boolean verify(byte[] data, byte[] sigBytes, BigInteger pub) {
        ECDSASignature signature = ECDSASignature.decodeFromDER(sigBytes);
        if (signature == null) {
            return false;
        }
        ECDSASigner signer = new ECDSASigner();
        ECPoint pubPoint = SECP256K1.curve().decodePoint(pub.toByteArray());
        ECPublicKeyParameters params = new ECPublicKeyParameters(pubPoint, SECP256K1.params());
        signer.init(false, params);
        return signer.verifySignature(data, signature.r, signature.s);
    }

    public static byte[] signHash(byte[] bytes, BigInteger secret) {
        ECDSASignature sig = createECDSASignature(bytes, secret);
        byte[] der = sig.encodeToDER();
        if (!ECDSASignature.isStrictlyCanonical(der)) {
            throw new IllegalStateException("Signature is not strictly canonical");
        }
        return der;
    }

    private static ECDSASignature createECDSASignature(byte[] hash, BigInteger secret) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(secret, SECP256K1.params());
        signer.init(true, privKey);
        BigInteger[] sigs = signer.generateSignature(hash);
        BigInteger r = sigs[0], s = sigs[1];

        BigInteger otherS = SECP256K1.order().subtract(s);
        if (s.compareTo(otherS) == 1) {
            s = otherS;
        }

        return new ECDSASignature(r, s);
    }
}
