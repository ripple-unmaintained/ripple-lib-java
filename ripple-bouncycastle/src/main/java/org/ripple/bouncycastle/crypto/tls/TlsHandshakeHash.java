package org.ripple.bouncycastle.crypto.tls;

import org.ripple.bouncycastle.crypto.Digest;

interface TlsHandshakeHash
    extends Digest
{

    void init(TlsContext context);

    TlsHandshakeHash commit();

    TlsHandshakeHash fork();
}
