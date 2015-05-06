package org.ripple.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;

import org.ripple.bouncycastle.crypto.Signer;

class SignerInputBuffer extends ByteArrayOutputStream
{
    void updateSigner(Signer s)
    {
        s.update(this.buf, 0, count);
    }
}