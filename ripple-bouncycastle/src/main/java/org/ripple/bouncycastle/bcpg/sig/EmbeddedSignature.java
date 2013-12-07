package org.ripple.bouncycastle.bcpg.sig;

import org.ripple.bouncycastle.bcpg.SignatureSubpacket;
import org.ripple.bouncycastle.bcpg.SignatureSubpacketTags;

/**
 * Packet embedded signature
 */
public class EmbeddedSignature
    extends SignatureSubpacket
{
    public EmbeddedSignature(
        boolean    critical,
        byte[]     data)
    {
        super(SignatureSubpacketTags.EMBEDDED_SIGNATURE, critical, data);
    }
}