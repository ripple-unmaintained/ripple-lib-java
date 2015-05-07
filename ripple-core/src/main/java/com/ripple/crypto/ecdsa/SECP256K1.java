package com.ripple.crypto.ecdsa;

import org.ripple.bouncycastle.asn1.sec.SECNamedCurves;
import org.ripple.bouncycastle.asn1.x9.X9ECParameters;
import org.ripple.bouncycastle.crypto.params.ECDomainParameters;
import org.ripple.bouncycastle.math.ec.ECCurve;
import org.ripple.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class SECP256K1 {
    private static final ECDomainParameters ecParams;
    private static final X9ECParameters params;

    static {

        params = SECNamedCurves.getByName("secp256k1");
        ecParams = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    public static ECDomainParameters params() {
        return ecParams;
    }

    public static BigInteger order() {
        return ecParams.getN();
    }


    public static ECCurve curve() {
        return ecParams.getCurve();
    }

    public static ECPoint basePoint() {
        return ecParams.getG();
    }

    static byte[] basePointMultipliedBy(BigInteger secret) {
        return basePoint().multiply(secret).getEncoded(true);
    }

}
