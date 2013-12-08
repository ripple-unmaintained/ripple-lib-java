package org.ripple.bouncycastle.crypto.prng;

import org.ripple.bouncycastle.crypto.prng.drbg.SP80090DRBG;

interface DRBGProvider
{
    SP80090DRBG get(EntropySource entropySource);
}
