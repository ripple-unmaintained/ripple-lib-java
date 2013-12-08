package org.ripple.bouncycastle.pqc.crypto.gmss;

import org.ripple.bouncycastle.crypto.Digest;

public interface GMSSDigestProvider
{
    Digest get();
}
