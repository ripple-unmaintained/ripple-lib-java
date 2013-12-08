package org.ripple.bouncycastle.openpgp.operator;

import org.ripple.bouncycastle.openpgp.PGPException;
import org.ripple.bouncycastle.openpgp.PGPPublicKey;

public interface PGPContentVerifierBuilder
{
    public PGPContentVerifier build(final PGPPublicKey publicKey)
        throws PGPException;
}
