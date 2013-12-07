package org.ripple.bouncycastle.crypto.ec;

import org.ripple.bouncycastle.crypto.CipherParameters;
import org.ripple.bouncycastle.math.ec.ECPoint;

public interface ECEncryptor
{
    void init(CipherParameters params);

    ECPair encrypt(ECPoint point);
}
