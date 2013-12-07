package org.ripple.bouncycastle.openpgp.operator;

import org.ripple.bouncycastle.bcpg.ContainedPacket;
import org.ripple.bouncycastle.openpgp.PGPException;

public abstract class PGPKeyEncryptionMethodGenerator
{
    public abstract ContainedPacket generate(int encAlgorithm, byte[] sessionInfo)
        throws PGPException;
}
