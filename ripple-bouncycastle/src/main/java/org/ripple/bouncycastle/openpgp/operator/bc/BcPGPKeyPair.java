package org.ripple.bouncycastle.openpgp.operator.bc;

import java.util.Date;

import org.ripple.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.ripple.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.ripple.bouncycastle.openpgp.PGPException;
import org.ripple.bouncycastle.openpgp.PGPKeyPair;
import org.ripple.bouncycastle.openpgp.PGPPrivateKey;
import org.ripple.bouncycastle.openpgp.PGPPublicKey;

public class BcPGPKeyPair
    extends PGPKeyPair
{
    private static PGPPublicKey getPublicKey(int algorithm, AsymmetricKeyParameter pubKey, Date date)
        throws PGPException
    {
        return new BcPGPKeyConverter().getPGPPublicKey(algorithm, pubKey, date);
    }

    private static PGPPrivateKey getPrivateKey(PGPPublicKey pub, AsymmetricKeyParameter privKey)
        throws PGPException
    {
        return new BcPGPKeyConverter().getPGPPrivateKey(pub, privKey);
    }

    public BcPGPKeyPair(int algorithm, AsymmetricCipherKeyPair keyPair, Date date)
        throws PGPException
    {
        this.pub = getPublicKey(algorithm, (AsymmetricKeyParameter)keyPair.getPublic(), date);
        this.priv = getPrivateKey(this.pub, (AsymmetricKeyParameter)keyPair.getPrivate());
    }
}
