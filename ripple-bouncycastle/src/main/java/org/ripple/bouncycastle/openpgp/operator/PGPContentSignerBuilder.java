package org.ripple.bouncycastle.openpgp.operator;

import org.ripple.bouncycastle.openpgp.PGPException;
import org.ripple.bouncycastle.openpgp.PGPPrivateKey;

public interface PGPContentSignerBuilder
{
    public PGPContentSigner build(final int signatureType, final PGPPrivateKey privateKey)
        throws PGPException;
}
