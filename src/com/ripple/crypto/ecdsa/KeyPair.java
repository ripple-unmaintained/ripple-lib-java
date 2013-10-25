package com.ripple.crypto.ecdsa;

import com.ripple.utils.Utils;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class KeyPair {
    BigInteger priv, pub;
    byte[] pubBytes;

    public BigInteger getPub() {
        return pub;
    }

    public byte[] getPubBytes() {
        return pubBytes;
    }

    public KeyPair(BigInteger priv, BigInteger pub) {
        this.priv = priv;
        this.pub = pub;
        this.pubBytes = pub.toByteArray();
    }

    public BigInteger getPriv() {
        return priv;
    }

    public boolean verify(byte[] data, byte[] sigBytes) {
        return verify(data, sigBytes, pub);
    }

    public byte[] sign(byte[] bytes) {
        return sign(bytes, priv);
    }

    public String pubHex() {
        return Utils.bigHex(pub);
    }

    public String privHex() {
        return Utils.bigHex(priv);
    }

    public static boolean verify(byte[] data, byte[] sigBytes, BigInteger pub) {
        ECDSASignature signature = ECDSASignature.decodeFromDER(sigBytes);
        ECDSASigner signer = new ECDSASigner();
        ECPoint pubPoint = SECP256K1.getCurve().decodePoint(pub.toByteArray());
        ECPublicKeyParameters params = new ECPublicKeyParameters(pubPoint, SECP256K1.getParams());
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] sign(byte[] bytes, BigInteger secret) {
        ECDSASignature sig = createECDSASignature(bytes, secret);
        return sig.encodeToDER();
    }

    private static ECDSASignature createECDSASignature(byte[] bytes, BigInteger secret) {
        ECDSASigner signer = new ECDSASigner();
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(secret, SECP256K1.getParams());
        signer.init(true, privKey);
        BigInteger[] sigs = signer.generateSignature(bytes);
        return new ECDSASignature(sigs[0], sigs[1]);
    }
}
