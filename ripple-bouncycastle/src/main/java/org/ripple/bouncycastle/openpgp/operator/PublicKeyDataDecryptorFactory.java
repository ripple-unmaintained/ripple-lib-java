package org.ripple.bouncycastle.openpgp.operator;

import java.math.BigInteger;

import org.ripple.bouncycastle.openpgp.PGPException;

public interface PublicKeyDataDecryptorFactory
    extends PGPDataDecryptorFactory
{
    public byte[] recoverSessionData(int keyAlgorithm, BigInteger[] secKeyData)
            throws PGPException;
}
