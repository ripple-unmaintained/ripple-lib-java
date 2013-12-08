package org.ripple.bouncycastle.crypto.prng;

public interface EntropySourceProvider
{
    EntropySource get(final int bitsRequired);
}
