package org.ripple.bouncycastle.crypto.tls;

import org.ripple.bouncycastle.crypto.DSA;
import org.ripple.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.ripple.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.ripple.bouncycastle.crypto.signers.ECDSASigner;

public class TlsECDSASigner
    extends TlsDSASigner
{

    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof ECPublicKeyParameters;
    }

    protected DSA createDSAImpl()
    {
        return new ECDSASigner();
    }
}
