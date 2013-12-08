package org.ripple.bouncycastle.openpgp.operator;

import java.security.SecureRandom;

import org.ripple.bouncycastle.openpgp.PGPException;

public interface PGPDataEncryptorBuilder
{
    int getAlgorithm();

    PGPDataEncryptor build(byte[] keyBytes)
        throws PGPException;

    SecureRandom getSecureRandom();
}
