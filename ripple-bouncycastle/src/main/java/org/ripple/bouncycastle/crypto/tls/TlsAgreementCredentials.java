package org.ripple.bouncycastle.crypto.tls;

import java.io.IOException;

import org.ripple.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface TlsAgreementCredentials
    extends TlsCredentials
{
    byte[] generateAgreement(AsymmetricKeyParameter peerPublicKey)
        throws IOException;
}
