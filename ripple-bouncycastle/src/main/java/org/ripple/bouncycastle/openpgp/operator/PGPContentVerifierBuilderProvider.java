package org.ripple.bouncycastle.openpgp.operator;

import org.ripple.bouncycastle.openpgp.PGPException;

public interface PGPContentVerifierBuilderProvider
{
    public PGPContentVerifierBuilder get(int keyAlgorithm, int hashAlgorithm)
        throws PGPException;
}
