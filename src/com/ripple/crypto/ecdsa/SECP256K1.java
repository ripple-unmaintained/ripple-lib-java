package com.ripple.crypto.ecdsa;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

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

    public static void dumpCurveINFO() {
        System.out.printf("X9ECParameters.getN() %s\n", params.getN().toString(16));
        System.out.printf("SECP256K1.sjclCurveR() %s\n", sjclCurveR().toString(16));
        System.out.printf("ECDomainParameters.getN() %s\n", ecParams.getN().toString(16));

    }

    public static BigInteger sjclCurveR() {
        return ecParams.getN();
    }


    public static ECCurve getCurve() {
        return ecParams.getCurve();
    }

    public static ECPoint getG() {
        return ecParams.getG();
    }

}
