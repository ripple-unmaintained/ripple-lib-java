package org.ripple.bouncycastle.crypto.tls;

import org.ripple.bouncycastle.crypto.DSA;
import org.ripple.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.ripple.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.ripple.bouncycastle.crypto.signers.DSASigner;

public class TlsDSSSigner
    extends TlsDSASigner
{

    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof DSAPublicKeyParameters;
    }

    protected DSA createDSAImpl()
    {
        return new DSASigner();
    }
}
