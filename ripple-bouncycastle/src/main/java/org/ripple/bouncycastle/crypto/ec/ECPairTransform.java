package org.ripple.bouncycastle.crypto.ec;

import org.ripple.bouncycastle.crypto.CipherParameters;

public interface ECPairTransform
{
    void init(CipherParameters params);

    ECPair transform(ECPair cipherText);
}
