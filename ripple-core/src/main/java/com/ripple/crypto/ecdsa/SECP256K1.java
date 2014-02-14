package com.ripple.crypto.ecdsa;

import java.math.BigInteger;

import org.ripple.bouncycastle.asn1.sec.SECNamedCurves;
import org.ripple.bouncycastle.asn1.x9.X9ECParameters;
import org.ripple.bouncycastle.crypto.params.ECDomainParameters;
import org.ripple.bouncycastle.math.ec.ECCurve;
import org.ripple.bouncycastle.math.ec.ECPoint;

public class SECP256K1 {
    private static final ECDomainParameters ecParams;
    private static final X9ECParameters params;

    static {

        params = SECNamedCurves.getByName("secp256k1");
        ecParams = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    public static ECDomainParameters getParams() {
        return ecParams;
    }

    public static BigInteger getOrder() {
        return ecParams.getN();
    }


    public static ECCurve getCurve() {
        return ecParams.getCurve();
    }

    public static ECPoint getG() {
        return ecParams.getG();
    }

    static byte[] gMultBy(BigInteger secret) {
        return getG().multiply(secret).getEncoded(true);
    }
}
