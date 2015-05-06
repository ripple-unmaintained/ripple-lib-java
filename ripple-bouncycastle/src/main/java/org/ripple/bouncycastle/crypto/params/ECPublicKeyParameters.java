package org.ripple.bouncycastle.crypto.params;

import org.ripple.bouncycastle.math.ec.ECPoint;

public class ECPublicKeyParameters
    extends ECKeyParameters
{
    ECPoint Q;

    public ECPublicKeyParameters(
        ECPoint             Q,
        ECDomainParameters  params)
    {
        super(false, params);
        this.Q = Q.normalize();
    }

    public ECPoint getQ()
    {
        return Q;
    }
}
