package org.ripple.bouncycastle.crypto;

import org.ripple.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface KeyEncoder
{
    byte[] getEncoded(AsymmetricKeyParameter keyParameter);
}
