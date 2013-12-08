package org.ripple.bouncycastle.crypto.ec;

import org.ripple.bouncycastle.crypto.CipherParameters;
import org.ripple.bouncycastle.math.ec.ECPoint;

public interface ECDecryptor
{
    void init(CipherParameters params);

    ECPoint decrypt(ECPair cipherText);
}
