package org.ripple.bouncycastle.jcajce.provider.symmetric.util;

import org.ripple.bouncycastle.crypto.BlockCipher;

public interface BlockCipherProvider
{
    BlockCipher get();
}
