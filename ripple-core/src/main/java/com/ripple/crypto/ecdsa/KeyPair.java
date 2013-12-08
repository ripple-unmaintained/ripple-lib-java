package com.ripple.crypto.ecdsa;

import java.math.BigInteger;

import org.ripple.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.ripple.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.ripple.bouncycastle.crypto.signers.ECDSASigner;
import org.ripple.bouncycastle.math.ec.ECPoint;

import com.ripple.utils.Utils;

public class KeyPair implements IKeyPair {
    BigInteger priv, pub;
    byte[] pubBytes;

    @Override
    public BigInteger pub() {
        return pub;
    }

    @Override
    public byte[] pubBytes() {
        return pubBytes;
    }

    public KeyPair(BigInteger priv, BigInteger pub) {
        this.priv = priv;
        this.pub = pub;
        this.pubBytes = pub.toByteArray();
    }

    @Override
    public BigInteger priv() {
        return priv;
    }

    @Override
    public boolean verify(byte[] data, byte[] sigBytes) {
        return verify(data, sigBytes, pub);
    }

    @Override
    public byte[] sign(byte[] bytes) {
        return sign(bytes, priv);
    }

    @Override
    public String pubHex() {
        return Utils.bigHex(pub);
    }

    @Override
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
