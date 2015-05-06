package org.ripple.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;

import org.ripple.bouncycastle.crypto.Digest;

class DigestInputBuffer extends ByteArrayOutputStream
{
    void updateDigest(Digest d)
    {
        d.update(this.buf, 0, count);
    }
}
